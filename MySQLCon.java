import java.sql.*;

class MysqlCon {
	public static void main(String args[]) throws Exception {

		Class.forName("com.mysql.cj.jdbc.Driver");

		try (Connection con = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/d1","lesco","123");
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

	public static void pl(Object o) {
		System.out.println(o);
	}
}
