package com.github.mapresultset;

import java.util.*;

/**
* MapResultSet has some restrictions regarding your queries.
* These restrictions could be handled, but I think they make
* your query more readable too. xD
*
* 1. Join must be done using JOIN, not in WHERE clause;
* 2. Values returned from SELECT that are not a simple column name must
*    have an alias and be preceded with `AS`, eg: select 1 as one; select age + 18 as something;
* 3. Columns in `select` must be preceded by the table name if the `from` clause contains
more than one table; ps: this is currently being handled in MappingProcessor. TODO: maybe do this validation in this class
* 4. The clauses must be in this order: select, from, [where], [group by], [having], [order by]

Current *known* limitations (ps: please open an issue if you find others =))
 - it doesn't handle 'USING' in joins. MySQL only?
*
*/
public class ParseQuery {

	private String query;
	private String cleanedQuery; // for easier parsing. Maybe choose a better name =)
	private String fromContent;
	private List<ColumnRecord> columns = new ArrayList<>();
	/**
	 * key: alias, value: table name
	 * 
	 * If there's no alias, then table name will be the alias too.
	 */
	private Map<String, String> tables = new HashMap<>();
	private int fromIndex = -1;
	private static final int MINIMUM_QUERY_SIZE = 15; // SELECT 1 FROM A
	private static final int INDEX_AFTER_SELECT = 7; // 'SELECT '
	private static final String[] OPTIONAL_CLAUSES = {
		" WHERE ", " GROUP BY ", "ORDER BY", " LIMIT ", " FETCH ", " OFFSET "
	};
	private static final Map<Character, Character> MATCHING_CHARS = Map.of(
		'(', ')',
		'"', '"',
		'\'', '\''
	);

	public String getFromContent() {
		return fromContent;
	}

	public List<ColumnRecord> getColumns() {
		return columns;
	}

	public Map<String, String> getTables() {
		return tables;
	}

	ParseQuery(String query) {
		if (query == null || query.length() < MINIMUM_QUERY_SIZE)
			throw new RuntimeException("Invalid query: " + query);
		this.query = query;
		cleanedQuery = cleanQuery(query);
		dbg("--------------------------------------------------------------");
		dbg("cleaned query: " + cleanedQuery);
		if (!cleanedQuery.substring(0, INDEX_AFTER_SELECT).equals("SELECT "))
			throw new RuntimeException("Query must start with 'select'.");
		fromIndex = cleanedQuery.indexOf(" FROM ");
		if (fromIndex == -1)
			throw new RuntimeException("Query must contain ' from ' clause.");
	}

	static int getClosingCharIndex(final String str, final int startIndex, final char openChar, final char closeChar) {
		final int len = str.length();
		int i = startIndex;
		int qt = 1;
		while (qt > 0) {
			++i;
			if (i == len) throw new RuntimeException("It didn't find matching '" + closeChar + "'.");
			if (str.charAt(i) == closeChar) --qt;
			else if (str.charAt(i) == openChar) ++qt;
		}

		return i;
	}

	void parse() {
		parseColumns();
		parseTables();
	}

	void parseColumns() {
		final String sc = cleanedQuery.substring(INDEX_AFTER_SELECT, fromIndex);
		if (sc.equals("*")) {
			throw new RuntimeException("'select *' is not a valid query for MapResultSet.");
		}
		// dbg("Select content: " + sc);
		final int len = sc.length();

		// It just need to find a ',' that is not inside: "", '' and ()
		// If it finds one of the chars below, then it needs to have an alias
		// and it means it's a generated value and not just a table column:
		//   ", ', (, +, -

		boolean mustHaveAlias = false;
		for (int i = 0; i < len; i++) {
			if (MATCHING_CHARS.containsKey(sc.charAt(i))) {
				i = getClosingCharIndex(sc, i, sc.charAt(i), MATCHING_CHARS.get(sc.charAt(i)));
				mustHaveAlias = true;
			} else if (sc.charAt(i) == '+' || sc.charAt(i) == '-') {
				mustHaveAlias = true;
			} else if (sc.charAt(i) == ',') {
				columns.add(createColumnRecord(sc, i, mustHaveAlias));
				mustHaveAlias = false;
			} else {
				// do nothing
			}
		}

		// add last column
		columns.add(createColumnRecord(sc, len, mustHaveAlias));

		dbg("columns: " + columns);
	}

	private static ColumnRecord createColumnRecord(String sc, int idx, boolean mustHaveAlias) {
		String columnName = getColumnBeforeIdx(sc, idx, mustHaveAlias);
		String alias = null;
		String tableName = null;
		int columnLen = columnName.length();
		// check if there's AS before it
		int j = idx - columnLen;
		if (j - 4 >= 0 && sc.substring(j - 4, j).equals(" AS ")) {
			alias = columnName;
			if (mustHaveAlias) {
				columnName = null;
			} else {
				j -= 5;
				int begin = j;
				int endExclusive = j + 1;

				while (sc.charAt(begin - 1) != ' ') --begin;
				columnName = sc.substring(begin, endExclusive);
			}
		}

		if (columnName != null) {
			int lastDotIdx = columnName.lastIndexOf(".");
			if (lastDotIdx != -1) {
				// check if there's a dot before that
				int previousDotIdx = -1;
				for (int i = lastDotIdx - 1; i >= 0; i--) {
					if (columnName.charAt(i) == '.') {
						previousDotIdx = i;
						break;
					}
				}
				if (previousDotIdx == -1) {
					tableName = columnName.substring(0, lastDotIdx);
				} else {
					tableName = columnName.substring(previousDotIdx + 1, lastDotIdx);
				}
				columnName = columnName.substring(lastDotIdx + 1);
			}
		}

		return new ColumnRecord(tableName, columnName, alias, mustHaveAlias);
	}

	static String getColumnBeforeIdx(final String str, final int idx, final boolean mustHaveAlias) {
		int left = idx - 1;
		while (left - 1 >= 0 && str.charAt(left - 1) != ' ' && str.charAt(left - 1) != ',') --left;

		if (mustHaveAlias && (left - 4 < 0 || !str.substring(left - 4, left).equals(" AS ")))
			throw new RuntimeException("Column must declare alias using ' AS '");

		return str.substring(left, idx);
	}

	void parseTables() {
		parseFromContent();
		final int len = fromContent.length();
		if (len == 0) throw new RuntimeException("Query does not contain any table.");
		String currTable = "";
		boolean findJoin = false;

		for (int i = 0; i < len; i++) {
			if (findJoin) { // find join table and ignore join columns
				// other possible values before 'join': inner, outer, full, left, right, cross
				// as none of the them contains 'j', it makes our life easier xD
				while (fromContent.charAt(i) != 'j' && fromContent.charAt(i) != 'J') ++i;

				i += 5; // Skip 'oin '

				// It might be joining with a temporary table
				if (fromContent.charAt(i) == '(') {
					i = getClosingCharIndex(fromContent, i, '(', ')') + 5; // skip ') AS '
				}

				String joinTable = "";
				while (fromContent.charAt(i) != ' ') joinTable += fromContent.charAt(i++);
				String tableName = joinTable;
				String alias = joinTable;
				// check if it has an alias
				if (i + 4 <= len && fromContent.substring(i, i + 4).equals(" AS ")) {
					i += 4; // skip ' as '
					alias = "";
					while (fromContent.charAt(i) != ' ') alias += fromContent.charAt(i++);
				}
				// dbg("joinTable: " + joinTable);
				tables.put(alias, tableName);

				// skip join columns
				while (true) {
					// find '='
					while (i < len && fromContent.charAt(i++) != '=');
					if (i == len)
						throw new RuntimeException("'=' not found in join. Joins must use 'on' with a join column, eg: from a join b on a.id = b.id");
					++i; // skip space after '='
					while (i < len && fromContent.charAt(i++) != ' ');

					// check if it's joining with more than one column
					if (i + 4 >= len || !fromContent.substring(i, i + 4).equals("AND ")) break;
					i += 4;
					// dbg("'and' found. Substring: " + fromContent.substring(i));
				}

				--i; // to avoid double increment in the 'for' loop
			} else if (fromContent.charAt(i) == '(') {
				if (!currTable.isEmpty()) throw new RuntimeException("Invalid query? " + query);
				i = getClosingCharIndex(fromContent, i, '(', ')') + 1;

				// must be followed by ' as '
				if (!fromContent.substring(i, i + 4).equals(" AS "))
					throw new RuntimeException("Temporary tables must declare an alias using 'AS'.");
				i += 4;
				String tableName = "";
				while (i < len && fromContent.charAt(i) != ' ') {
					tableName += fromContent.charAt(i++);
				}
				if (tableName.isEmpty()) throw new RuntimeException("alias not found");
				tables.put(tableName, tableName);
				findJoin = true;
			} else if (fromContent.charAt(i) == ' ') {
				String tableName = currTable;
				String alias = currTable;
				// check if it has an alias
				if (fromContent.substring(i, i + 4).equals(" AS ")) {
					i += 4; // skip ' as '
					alias = "";
					while (i < len && fromContent.charAt(i) != ' ') alias += fromContent.charAt(i++);
				}
				tables.put(alias, tableName);
				currTable = "";
				findJoin = true;
			} else {
				currTable += fromContent.charAt(i);
			}
		}
		if (!currTable.isEmpty()) tables.put(currTable, currTable);

		dbg("tables.: " + tables);
	}

	/**
	* It stops at the first match of: WHERE | GROUP BY | ORDER BY | LIMIT | FETCH
	*
	*/
	void parseFromContent() {
		final int len = cleanedQuery.length();
		final int startIndex = fromIndex + 5; // let it start with a space
		int lastIndexExclusive = len;
		outer:
		for (int i = startIndex; i < len; i++) {
			if (cleanedQuery.charAt(i) == '(') {
				i = getClosingCharIndex(cleanedQuery, i, '(', ')');
				continue;
			}

			// FIXME LIMIT and FETCH might be valid column names? ... =(
			for (var oc : OPTIONAL_CLAUSES) {
				if (i + oc.length() <= len && cleanedQuery.substring(i, i + oc.length()).equals(oc)) {
					lastIndexExclusive = i;
					break outer;
				}
			}
		}
		// dbg("lastIndexExclusive = " + lastIndexExclusive);
		this.fromContent = cleanedQuery.substring(startIndex, lastIndexExclusive).trim();	
	}

	/**
		1. Remove line breaks
		2. Trim
		3. replace tabs with spaces
		4. replace more than one space with one space
		5. remove spaces before ','
		6. change keywords to uppercase
	*/
	static String cleanQuery(String query) {
		query = query.replaceAll("\\R", " ")
				.trim()
				.replaceAll("\\t", " ")
				.replaceAll(" +", " ")
				.replaceAll(" +,", ",")
				.replaceFirst("(?i)select", "SELECT")
				.replaceFirst(" (?i)from ", " FROM ")
				.replaceAll(" (?i)as ", " AS ")
				.replaceAll(" (?i)and ", " AND ");

		for (var oc : OPTIONAL_CLAUSES)
			query = query.replaceAll("(?i)" + oc, oc);

		return query;
	}

	static void pl(Object o) {
		System.out.println(o);
	}

	public static void main(String[] args) {
		checkAssertionEnabled();

		testCleanQuery();
		testNullQuery();
		testBelowMinimumQuerySize();
		testNoSelect();
		testNoFrom();
		testSelectStartThrows();
		testGetFromContent();
		testOneTable();
		testNoTable();
		testInvalidJoin();
		testTwoTables();
		testThreeTables();
		testWithWhereClause();
		testTableFromSelect();
		testJoinTemporaryTable();
		testJoinTemporaryTableUsingAS();
		testJoinTwoTemporaryTables();
		testJoinTwoTemporaryTablesWithLimit();
		testJoin3Tables();
		testJoin4Tables();
		testColumnParsingNoAliasThrows();
		testColumnParsing1();

		pl("============== ALL TESTS PASSED!!!!! =D ==============");
	}

	static void checkAssertionEnabled() {
		boolean[] isEnabled = {false};
		assert enableAssertion(isEnabled);
		if (!isEnabled[0]) throw new RuntimeException("You need to enable assertions to run the tests: java -ea ParseQuery");
	}

	static boolean enableAssertion(boolean[] isEnabled) {
		isEnabled[0] = true;
		return true;
	}

	static void testCleanQuery() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		assertEq(ParseQuery.cleanQuery(sql), "SELECT * FROM client");

		sql = """
		select *
		FRoM client aS c
		where id > 10
		ordEr By name
		""";
		assertEq(ParseQuery.cleanQuery(sql), "SELECT * FROM client AS c WHERE id > 10 ORDER BY name");
	}

	static void testSelectStartThrows() {
		final String sql = """
		
			select *
		  from   client
		  
		""";

		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"'select *' is not a valid query for MapResultSet."
		);
	}

	static void testGetFromContent() {
		String sql = """
		
			select age
		  from   client
		  
		""";

		var p = new ParseQuery(sql);
		p.parseFromContent();
		assertEq(p.getFromContent(), "client");
	}

	static void testOneTableAS() {
		String sql = """
			select id, name
			from person as Person
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertEq(p.getFromContent(), "Person");
	}

	static void testOneTable() {
		String sql = """
		
			select nick_name
		  from   client
		  
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertEq(p.getFromContent(), "client");
	}

	static void testNullQuery() {
		assertThrows(() -> new ParseQuery(null).parse(),
				RuntimeException.class,
				"Invalid query: null"
		);
	}

	static void testBelowMinimumQuerySize() {
		String sql = " from xyz";
		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"Invalid query: " + sql
		);
	}

	static void testNoSelect() {
		String sql = " from xyz where xxxxxxxxxxx";
		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"Query must start with 'select'."
		);
	}

	static void testNoFrom() {
		assertThrows(() -> new ParseQuery("select xyz, aaa, bbb, ccc").parse(),
				RuntimeException.class,
				"Query must contain ' from ' clause."
		);
	}

	static void testNoTable() {
		String sql = "select xyz from where a > 2";
		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"Query does not contain any table."
		);
	}

	static void testInvalidJoin() {
		String sql = """
		
			select name
		  from   client join address on 
		  
		""";
		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"'=' not found in join. Joins must use 'on' with a join column, eg: from a join b on a.id = b.id"
		);
	}

	static void testTwoTables() {
		String sql = """
		
			select name
		  from   client join address on client.id = address.client_id
		  
		""";

		new ParseQuery(sql).parse();
	}

	static void testThreeTables() {
		String sql = """
		  SELECT client.name
		  from   client join address on
		           client.id = address.client_id
		    join club on
			       client.club_id = club.id
		  WHERE club.pool_temperature = 'is warm'
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertEq(p.getFromContent(), "client join address on client.id = address.client_id join club on client.club_id = club.id");
	}

	static void testWithWhereClause() {
		String sql = """
		  SELECT name
		  from   club
		  WHERE club.pool_temperature = 'is warm'
		""";
		
		new ParseQuery(sql).parse();
	}

	static void testTableFromSelect() {
		String sql = """
		  SELECT A.id, age + 2000 as other_age, (id as does_matter_here) as new_id
		  from   (
			select city, count(*)
			from club
			group by city) as A
		  WHERE it does matter here
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertEq(p.getFromContent(), "( select city, count(*) from club GROUP BY city) AS A");
	}

	static void testJoinTemporaryTable() {
		String sql = """
		  SELECT id, A.city as GreatCity
		  from   (
			select city, count(*)
			from club
			group by city
		  ) as A join club on
		      a.city = club.city
			
		  order by id
		""";

		var p = new ParseQuery(sql);
		p.parse();
		// assertListEq(p.getTables(), List.of("A", "club"));
		pl(p.getTables());
		assertEq(p.getFromContent(), "( select city, count(*) from club GROUP BY city ) AS A join club on a.city = club.city");
	}

	static void testJoinTemporaryTableUsingAS() {
		String sql = """
		  SELECT id, A.city
		  from   (
			select city, count(*)
			from club
			group by city
		  ) as A lefT join club as c2 on
		      a.city = c2.city
			
		  order by id
		""";

		var p = new ParseQuery(sql);
		p.parse();
		// assertListEq(p.getTables(), List.of("A", "c2"));
		pl(p.getTables());
		assertEq(p.getFromContent(), "( select city, count(*) from club GROUP BY city ) AS A lefT join club AS c2 on a.city = c2.city");
	}

	static void testJoinTwoTemporaryTables() {
		String sql = """
		  SELECT id, A.city
		  from   (
			select city, count(*)
			from club
			group by city
		  ) as A inner join (
			select xyz
			from xxx
			where yyy
		  ) as B on
		    a.xxx = b.iii and
			a.ttt = b.ttt
		""";

		var p = new ParseQuery(sql);
		p.parse();
		// assertListEq(p.getTables(), List.of("A", "B"));
		pl(p.getTables());
		assertEq(p.getFromContent(), "( select city, count(*) from club GROUP BY city ) AS A inner join ( select xyz from xxx WHERE yyy ) AS B on a.xxx = b.iii AND a.ttt = b.ttt");
	}

	static void testJoinTwoTemporaryTablesWithLimit() {
		String sql = """
		  SELECT id, A.city
		  from   (
			select city, count(*)
			from club
			group by city
		  ) as A inner join (
			select xyz
			from xxx
			where yyy
		  ) as B on
		    a.xxx = b.iii and
			a.ttt = b.ttt
		  limit 3
		""";

		var p = new ParseQuery(sql);
		p.parse();
		// assertListEq(p.getTables(), List.of("A", "B"));
		pl(p.getTables());
	}

	// FIXME missing 'join' before 'on' must throw exception
	static void testJoin3Tables() {
		String sql = """
		  SELECT city
		  from (
			select city, count(*)
			from club
			group by city
		  ) as A ON
			ABC.xx = A.city and abc.oo = a.oo
		  inner join (
			select xyz
			from xxx
			where yyy
		  ) as B on
		    a.xxx = b.iii and
			a.ttt = b.ttt
			cross join tablespace.uu           on
			  b.id = uu.id
		  offSet 3 rows
		""";

		var p = new ParseQuery(sql);
		p.parse();
		// assertListEq(p.getTables(), List.of("A", "B", "tablespace.uu"));
	}

	static void testJoin4Tables() {
		String sql = """
		  SELECT id, A.city
		  from  ABC RIGHT JOIN (
			select city, count(*)
			from club
			group by city
		  ) as A ON
			ABC.xx = A.city
		  inner join (
			select xyz
			from xxx
			where yyy
		  ) as B on
		    a.xxx = b.iii and
			a.ttt = b.ttt
			cross join uu as u3 on
			  b.id = uu.id
		  FeTCh first 1 row only
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertMapEq(p.getTables(), Map.of("ABC", "ABC", "A", "A", "B", "B", "u3", "uu"));
		pl(p.getTables());
	}

	static void testColumnParsingNoAliasThrows() {
		String sql = """
		  SELECT id + 200 - 30, name, MOD(29,9),
				1+'1', CONCAT(2,' test')
		  from  x
		""";

		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"Column must declare alias using ' AS '"
		);
	}

	static void testColumnParsing1() {
		String sql = """
		  SELECT id + 200 - 30 as strange_id, name as nice_name, MOD(29,9) as some_mod,
				1+'1' as guess_what, CONCAT(2,' test') as test2,
				"from" as origin
		  from  x as Y
		""";

		var p = new ParseQuery(sql);
		p.parse();
		assertMapEq(p.getTables(), Map.of("Y", "x"));
		assertListEq(p.getColumns(), List.of(
			new ColumnRecord(null, null, "strange_id", true),
			new ColumnRecord(null, "name", "nice_name", false),
			new ColumnRecord(null, null, "some_mod", true),
			new ColumnRecord(null, null, "guess_what", true),
			new ColumnRecord(null, null, "test2", true),
			new ColumnRecord(null, null, "origin", true)));
	}

	static List<String> collectionToSortedList(Collection<String> set) {
		var list = new ArrayList<String>(set);
		Collections.sort(list);
		return list;
	}

	static void assertMapEq(Map<String, String> actual, Map<String, String> expected) {
		var actualKeys = collectionToSortedList(actual.keySet());
		var expectedKeys = collectionToSortedList(expected.keySet());
		assertListEq(actualKeys, expectedKeys);

		var actualValues = collectionToSortedList(actual.values());
		var expectedValues = collectionToSortedList(expected.values());
		assertListEq(actualValues, expectedValues);
	}

	static <T> void assertListEq(List<T> actual, List<T> expected) {
		if (actual == null && expected == null) return;
		if ((actual == null && expected != null) ||
				(actual != null && expected == null) ||
				actual.size() != expected.size())
			throw new RuntimeException("Actual: '" + actual + "', expected: '" + expected + "'");
		for (int i = 0; i < actual.size(); i++)
			assert actual.get(i).equals(expected.get(i)) : "Actual: '" + actual + "', expected: '" + expected + "'";
	}

	static void assertEq(String actual, String expected) {
		assert actual.equals(expected) : "Actual: '" + actual + "', expected: '" + expected + "'";
	}

	static void assertThrows(Runnable run, Class exceptionClass, String errorMsg) {
		try {
			run.run();
			new Exception("Did not throw: " + exceptionClass + "\nExpected error message: " + errorMsg).printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			if (e.getClass() != exceptionClass) {
				e.printStackTrace();
				assertEq(e.getClass().toString(), exceptionClass.toString()); // just to have more debug info ...
			}
			assertEq(e.getMessage(), errorMsg);
		}
	}

	static void dbg(Object o) {
		assert dbg1(o);
	}

	static boolean dbg1(Object o) {
		pl(o); return true;
	}

}

/*

Rereferences:

https://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm

https://stackoverflow.com/questions/285775/how-to-deal-with-sql-column-names-that-look-like-sql-keywords

https://www.ibm.com/docs/en/psfa/7.2.1?topic=documentation-sql-reserved-words-keywords

https://www.sqlite.org/optoverview.html

https://stackoverflow.com/questions/16470942/how-to-use-mysql-join-without-on-condition

https://dev.mysql.com/doc/refman/8.0/en/join.html

https://stackoverflow.com/questions/53601879/using-alias-without-as

https://stackoverflow.com/questions/470542/how-do-i-limit-the-number-of-rows-returned-by-an-oracle-query-after-ordering

https://www.oracletutorial.com/oracle-administration/oracle-tablespace/

https://dev.mysql.com/doc/refman/5.7/en/functions.html

*/
