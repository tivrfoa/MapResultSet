package org.acme.dao;

import java.sql.*;

public class MySQLCon {

	private Connection con;

	public static void main(String args[]) throws Exception {
		testMySQLConnection();
		testResultSetLabelWithTableBeforeLabel();
	}

	private static void testMySQLConnection() throws Exception {
		var mysqlCon = new MySQLCon();

		try (Connection con = mysqlCon.getConnection();
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery("select id, name from Person");
			while(rs.next())
				System.out.println(rs.getInt(1) + "  " + rs.getString(2));

			pl("----------------------------------");
			pl("Test Prepared Statement with final string");
			// https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
			final String query2 = """
				select id, name
				from Person
				where id = ?
			""";

			PreparedStatement psId2 = con.prepareStatement(query2);
			psId2.setInt(1, 2);
			rs = psId2.executeQuery();
			while(rs.next())
				System.out.println(rs.getInt(1) + "  " + rs.getString(2));
		}
	}

	private static void testResultSetLabelWithTableBeforeLabel() throws Exception {
		var mysqlCon = new MySQLCon();

		final String query = """
            select p.name, c.name, c.phone_code as PhoneCode
            from person as p join country as c on
              p.country_id = c.id
            """;

		try (Connection con = mysqlCon.getConnection();
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println("Person name: " + rs.getString("p.name"));
				System.out.println("Country name: " + rs.getString("c.name"));
			}
		}
	}

	public Connection getConnection() {
		if (con == null) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/d1","lesco","123");
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return con;
	}

	public static void pl(Object o) {
		System.out.println(o);
	}

	
}
