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
		}
	}
}
