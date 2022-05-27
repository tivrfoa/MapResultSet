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
*    (for the moment, I think the user deserves it xD, but I'll eventually handle it)
*
*/
class ParseQuery {

	private String query;
	private String cleanedQuery;
	private String queryUpperCase;
	private List<String> columns = new ArrayList<>();
	private List<String> tables = new ArrayList<>();
	
	ParseQuery(String query) {
		cleanedQuery = cleanQuery(query);
		pl("cleaned query: " + cleanedQuery);
		queryUpperCase = cleanedQuery.toUpperCase();
	}
	
	void parse() {
		pl("cleaned query: " + cleanedQuery);
		
		if (!cleanedQuery.substring(0, 7).toUpperCase().equals("SELECT "))
			throw new RuntimeException("Query must start with 'select'.");
		
		// let's find the tables
		
	}
	
	List<String> parseTables() {
		final var fromContent = getFromContent().toCharArray();
		if (fromContent.length == 0) throw new RuntimeException("Query does not contain any table.");
		String currTable = "";
		boolean findJoin = false;
		//int i = 0;
		//while (i < fromContent.length) {
		for (int i = 0; i < fromContent.length; i++) {
			if (findJoin) { // find JOIN and ignore join columns
				
				// other possible values before 'join': inner, outer, full, left, right, cross
				// as none of the them contains 'j', it makes our life easier xD
				while (fromContent[i] != 'j' && fromContent[i] != 'J') ++i;
				
				i += 4; // Skip 'oin '
				
				String joinColumn = "";
				while (fromContent[i] != ' ') joinColumn += fromContent[i++];
				// check if it has an alias
				if (fromContent[i + 1] == 'a' || fromContent[i + 1] == 'A') {
					i += 4; // skip ' as '
					joinColumn = "";
					while (fromContent[i] != ' ') joinColumn += fromContent[i++];
				}
				columns.add(joinColumn);
				
				// skip
				
				findJoin = false;
			} else if (fromContent[i] == '(') {
				if (!currTable.isEmpty()) throw new RuntimeException("Invalid query? " + query);
				// find matching ')'
				int qt = 1;
				++i;
				while (qt > 0) {
					if (fromContent[i] == ')') --qt;
					else if (fromContent[i] == '(') ++qt;
					++i;
					if (i > fromContent.length) throw new RuntimeException("It didn't find matching ')'.");
				}
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
			++i;
		}
		if (!currTable.isEmpty()) tables.add(currTable);
		
		pl(tables);
	}
	
	String getFromContent() {
		int fromIndex = queryUpperCase.indexOf(" FROM ");
		if (fromIndex == -1)
			throw new RuntimeException("Query must contain ' from ' clause.");
		int whereIndex = queryUpperCase.indexOf(" WHERE ");
		if (whereIndex == -1) whereIndex = Integer.MAX_VALUE;
		int groupIndex = queryUpperCase.indexOf(" GROUP BY");
		if (groupIndex == -1) groupIndex = Integer.MAX_VALUE;
		int orderIndex = queryUpperCase.indexOf(" ORDER BY");
		if (orderIndex == -1) orderIndex = Integer.MAX_VALUE;
		
		int startIndex = fromIndex + 6;
		int lastIndexExclusive = Math.min(queryUpperCase.length(), Math.min(whereIndex, Math.min(groupIndex, orderIndex)));
		if (lastIndexExclusive <= startIndex) throw new RuntimeException("From clause does not have any table.");

		return cleanedQuery.substring(startIndex, lastIndexExclusive);	
	}
	
	/**
		1. Remove line breaks
		2. Trim
		3. replace tabs with spaces
		4. replace more than one space with one space 
	*/
	static String cleanQuery(String query) {
		return query.replaceAll("\\R", " ")
				.trim()
				.replaceAll("\\t", " ")
				.replaceAll(" +", " ");
	}
	
	static void pl(Object o) {
		System.out.println(o);
	}
	
	public static void main(String[] args) {
		testCleanQuery();
		testNoSelect();
		testNoFrom();
		testGetFromContent();
		testOneTable();
		testNoTable();
		testTwoTables();
		testThreeTables();
		testWithWhereClause();
	}
	
	static void testCleanQuery() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		
		assertEq(ParseQuery.cleanQuery(sql), "select * from client");
	}
	
	static void testGetFromContent() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		
		assertEq(ParseQuery.getFromContent(ParseQuery.cleanQuery(sql)), "client");
	}

	static void testOneTable() {
		String sql = """
		
			select *
		  from   client
		  
		""";
		
		new ParseQuery().parse(sql);
	}
	
	static void testNoSelect() {
		String sql = " from xyz";
		assertThrows(() -> new ParseQuery().parse(sql),
				RuntimeException.class,
				"Query must start with 'select'."
		);
	}

	static void testNoFrom() {
		assertThrows(() -> new ParseQuery().parse("select xyz"),
				RuntimeException.class,
				"Query must contain ' from ' clause."
		);
		
		assertThrows(() -> new ParseQuery().parse("select xyz from"),
				RuntimeException.class,
				"Query must contain ' from ' clause."
		);
	}
		
	static void testNoTable() {
		String sql = "select xyz from where a > 2";
		assertThrows(() -> new ParseQuery().parse(sql),
				RuntimeException.class,
				"From clause does not have any table."
		);
	}
	
	static void testTwoTables() {
		String sql = """
		
			select *
		  from   client join address on
		  
		""";
		
		new ParseQuery().parse(sql);
	}
	
	static void testThreeTables() {
		String sql = """
		
		""";
		
		assert "TODO".equals("");
	}
	
	static void testWithWhereClause() {
		String sql = """
		
		""";
		
		assert "TODO".equals("");
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
	
}

/*

Rereferences:

https://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm

https://stackoverflow.com/questions/285775/how-to-deal-with-sql-column-names-that-look-like-sql-keywords

https://www.ibm.com/docs/en/psfa/7.2.1?topic=documentation-sql-reserved-words-keywords

https://www.sqlite.org/optoverview.html

https://stackoverflow.com/questions/16470942/how-to-use-mysql-join-without-on-condition

https://dev.mysql.com/doc/refman/8.0/en/join.html

*/