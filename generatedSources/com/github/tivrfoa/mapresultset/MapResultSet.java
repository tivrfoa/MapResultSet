package com.github.tivrfoa.mapresultset;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Company;
import com.github.tivrfoa.mapresultset.TestProcessor.Phone;


public class MapResultSet {
	
	public static ListPhonesRecords listPhones(ResultSet rs) throws SQLException {
		ListPhonesRecords records = new ListPhonesRecords();

		while (rs.next()) {
			Company obj1 = new Company();
			obj1.setName(rs.getString("c.name"));
			records.getListCompany().add(obj1);

			Phone obj2 = new Phone();
			obj2.setId(rs.getInt("phone.id"));
			records.getListPhone().add(obj2);

		}

		return records;

	}


}

