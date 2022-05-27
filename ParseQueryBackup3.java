import java.util.*;

/**
* MapResultSet has some restrictions regarding your queries.
* These restrictions could be handled, but I think they make
* your query more readable too. xD
*
* 1. Must start with a SELECT
* 2. Must contain FROM (only the first FROM matters for MapResultSet)
* 3. Join must be done using JOIN
* 4. Values returned from SELECT that are not a simple column must
*    have an alias, eg: select 1 as one; select age + 18 as something

* Current *known* limitations (ps: please open an issue if you find others =))
*  - it doesn't handle cases where column is a sql identifier, eg "from"
*    (I think the user deserves the current status, but I might eventually handle it)
*  - it doesn't handle 'USING' in joins. MySQL only?
*  - joins must use 'on' with a join column, eg: from a join b on a.id = b.id
*  - you must use 'as' when using alias.
*  - The clauses must be in this order: select, from, [where], [group by], [having], [order by]
*
*/
class ParseQuery {

	private String query;
	private String cleanedQuery; // maybe a better name here ... =)
	private List<String> columns = new ArrayList<>();
	private List<String> tables = new ArrayList<>();
	private int fromIndex = -1;
	private int lastIndex = -1;
	private static final int MINIMUM_QUERY_SIZE = 15; // SELECT 1 FROM A
	private static final int INDEX_AFTER_SELECT = 7; // 'SELECT '
	private static final String[] OPTIONAL_CLAUSES = new String[]{
		" WHERE ", " GROUP BY ", "ORDER BY", " LIMIT ", " FETCH ", " OFFSET "
	};
	
	public List<String> getTables() {
		return tables;
	}
	
	ParseQuery(String query) {
		if (query == null || query.length() < MINIMUM_QUERY_SIZE)
			throw new RuntimeException("Invalid query: " + query);
		this.query = query;
		cleanedQuery = cleanQuery(query);
		dbg("cleaned query: " + cleanedQuery);
		if (!cleanedQuery.substring(0, INDEX_AFTER_SELECT).equals("SELECT "))
			throw new RuntimeException("Query must start with 'select'.");
		fromIndex = cleanedQuery.indexOf(" FROM ");
		if (fromIndex == -1)
			throw new RuntimeException("Query must contain ' from ' clause.");
	}
	
	void parse() {
		parseTables();
	}
	
	void parseTables() {
		final String fc = getFromContent();
		final var fromContent = fc.toCharArray();
		dbg("fromContent: " + new String(fromContent));
		if (fromContent.length == 0) throw new RuntimeException("Query does not contain any table.");
		String currTable = "";
		boolean findJoin = false;
		//int i = 0;
		//while (i < fromContent.length) {
		for (int i = 0; i < fromContent.length; i++) {
			if (findJoin) { // find join table and ignore join columns
				dbg("1: " + fc.substring(i));
				// other possible values before 'join': inner, outer, full, left, right, cross
				// as none of the them contains 'j', it makes our life easier xD
				while (fromContent[i] != 'j' && fromContent[i] != 'J') ++i;
				
				i += 5; // Skip 'oin '
				dbg(fromContent[i]);
				// It might be joining with a temporary table
				if (fromContent[i] == '(') {
					// find matching ')'
					int qt = 1;
					while (qt > 0) {
						++i;
						if (i == fromContent.length) throw new RuntimeException("It didn't find matching ')'.");
						if (fromContent[i] == ')') --qt;
						else if (fromContent[i] == '(') ++qt;
					}
					i += 5; // skip ') AS '
				}
				dbg("2: " + fc.substring(i));
				String joinTable = "";
				while (fromContent[i] != ' ') joinTable += fromContent[i++];
				// check if it has an alias
				if (fromContent[i + 1] == 'a' || fromContent[i + 1] == 'A') {
					i += 4; // skip ' as '
					joinTable = "";
					while (fromContent[i] != ' ') joinTable += fromContent[i++];
				}
				dbg("joinTable: " + joinTable);
				tables.add(joinTable);
				
				while (true) {
					// find '=' and skip string after that
					while (i < fromContent.length && fromContent[i++] != '=');
					if (i == fromContent.length)
						throw new RuntimeException("'=' not found in join. Joins must use 'on' with a join column, eg: from a join b on a.id = b.id");
					++i; // skip space after '='
					while (i < fromContent.length && fromContent[i++] != ' ');
					
					dbg("before check 'and'. Substring: " + fc.substring(i));
					// check if it's joining with more than one column
					if (i + 4 >= fromContent.length || !fc.substring(i, i + 4).equals("AND ")) break;
					i += 4;
					dbg("'and' found. Substring: " + fc.substring(i));
				}
				
				--i; // to avoid double increment in the 'for' loop
			} else if (fromContent[i] == '(') {
				if (!currTable.isEmpty()) throw new RuntimeException("Invalid query? " + query);
				// find matching ')'
				int qt = 1;
				while (qt > 0) {
					++i;
					if (i == fromContent.length) throw new RuntimeException("It didn't find matching ')'.");
					if (fromContent[i] == ')') --qt;
					else if (fromContent[i] == '(') ++qt;
				}
				++i;
				// must be followed by ' as '
				if (!fc.substring(i, i + 4).equals(" AS "))
					throw new RuntimeException("Temporary tables must declare an alias using 'AS'.");
				i += 4;
				String tableName = "";
				while (i < fromContent.length && fromContent[i] != ' ') {
					tableName += fromContent[i++];
				}
				if (tableName.isEmpty()) throw new RuntimeException("alias not found");
				tables.add(tableName);
				findJoin = true;
			} else if (fromContent[i] == ' ') {
				// check if it has an alias
				if (fromContent[i + 1] == 'a' || fromContent[i + 1] == 'A') {
					i += 4; // skip ' as '
					currTable = "";
					while (fromContent[i] != ' ') currTable += fromContent[i++];
				}
				tables.add(currTable);
				currTable = "";
				findJoin = true;
			} else {
				currTable += fromContent[i];
			}
		}
		if (!currTable.isEmpty()) tables.add(currTable);
		
		dbg(tables);
	}
	
	/**
	* It stops at the first match of: WHERE | GROUP BY | ORDER BY | LIMIT | FETCH
	*
	*/
	String getFromContent() {
		final int len = cleanedQuery.length();
		dbg("len = " + len);
		final int startIndex = fromIndex + 5; // let it start with a space
		int lastIndexExclusive = len;
		outer:
		for (int i = startIndex; i < len; i++) {
			if (cleanedQuery.charAt(i) == '(') {
				// find matching ')'
				int qt = 1;
				while (qt > 0) {
					++i;
					if (i == len) throw new RuntimeException("It didn't find matching ')'.");
					if (cleanedQuery.charAt(i) == ')') --qt;
					else if (cleanedQuery.charAt(i) == '(') ++qt;
				}
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
		dbg("lastIndexExclusive = " + lastIndexExclusive);
		return cleanedQuery.substring(startIndex, lastIndexExclusive).trim();	
	}
	
	/**
		1. Remove line breaks
		2. Trim
		3. replace tabs with spaces
		4. replace more than one space with one space 
		5. change keywords to uppercase
	*/
	static String cleanQuery(String query) {
		query = query.replaceAll("\\R", " ")
				.trim()
				.replaceAll("\\t", " ")
				.replaceAll(" +", " ")
				.replaceFirst("(?i)select", "SELECT")
				.replaceAll(" (?i)from ", " FROM ")
				.replaceAll(" (?i)where ", " WHERE ")
				.replaceAll(" (?i)group by ", " GROUP BY ")
				.replaceAll(" (?i)order by ", " ORDER BY ")
				.replaceAll(" (?i)as ", " AS ")
				.replaceAll(" (?i)limit ", " LIMIT ")
				.replaceAll(" (?i)fetch ", " FETCH ")
				.replaceAll(" (?i)and ", " AND ");
		
		
		return query;
	}
	
	static void pl(Object o) {
		System.out.println(o);
	}
	
	public static void main(String[] args) {
		testCleanQuery();
		testNullQuery();
		testBelowMinimumQuerySize();
		testNoSelect();
		testNoFrom();
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
		testJoin4Tables();
		
		pl("============== ALL TESTS PASSED!!!!! =D ==============");
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
	
	static void testGetFromContent() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		
		// assertEq(ParseQuery.getFromContent(ParseQuery.cleanQuery(sql)), "client");
		assertEq(new ParseQuery(sql).getFromContent(), "client");
	}

	static void testOneTable() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		
		new ParseQuery(sql).parse();
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
		
			select *
		  from   client join address on 
		  
		""";
		assertThrows(() -> new ParseQuery(sql).parse(),
				RuntimeException.class,
				"'=' not found in join. Joins must use 'on' with a join column, eg: from a join b on a.id = b.id"
		);
	}
	
	static void testTwoTables() {
		String sql = """
		
			select *
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
		
		new ParseQuery(sql).parse();
	}
	
	static void testWithWhereClause() {
		String sql = """
		  SELECT *
		  from   club
		  WHERE club.pool_temperature = 'is warm'
		""";
		
		new ParseQuery(sql).parse();
	}
	
	static void testTableFromSelect() {
		String sql = """
		  SELECT *
		  from   (
			select city, count(*)
			from club
			group by city) as A
		  WHERE it does matter here
		""";
		
		new ParseQuery(sql).parse();
	}
	
	static void testJoinTemporaryTable() {
		String sql = """
		  SELECT id, A.city
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
		assertListEq(p.getTables(), List.of("A", "club"));
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
		assertListEq(p.getTables(), List.of("A", "c2"));
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
		  limit 3
		""";
		
		var p = new ParseQuery(sql);
		p.parse();
		assertListEq(p.getTables(), List.of("A", "B"));
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
		assertListEq(p.getTables(), List.of("ABC", "A", "B", "u3"));
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

*/