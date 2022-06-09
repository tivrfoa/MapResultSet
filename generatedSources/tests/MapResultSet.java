package tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Company;
import tests.TestProcessor.Phone;


public class MapResultSet {
	
	public static ListPhonesRecords listPhones(ResultSet rs) throws SQLException {
		ListPhonesRecords records = new ListPhonesRecords();

		while (rs.next()) {
			Company obj1 = new Company();
			obj1.setName(rs.getString("name"));
			records.getListCompany().add(obj1);

			Phone obj2 = new Phone();
			obj2.setId(rs.getInt("id"));
			records.getListPhone().add(obj2);

		}

		return records;

	}


}

