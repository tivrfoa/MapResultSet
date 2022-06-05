package tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Company;
import tests.TestProcessor.Phone;


public class MapResultSet {
	
	public static ListPhonesRecords listPhones(ResultSet rs) throws SQLException {
		ListPhonesRecords records = new ListPhonesRecords();

		while (rs.next()) {
			{
				Company obj = new Company();
				obj.setName(rs.getString("name"));
				records.listCompany.add(obj);
			}
			{
				Phone obj = new Phone();
				obj.setId(rs.getInt("id"));
				records.listPhone.add(obj);
			}
		}

		return records;

	}


}

