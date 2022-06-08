package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Person;
import org.acme.domain.Notebook;
import org.acme.domain.Company;
import org.acme.domain.Country;


public class MapResultSet {
	
	public static ListPersonCountryRecords listPersonCountry(ResultSet rs) throws SQLException {
		ListPersonCountryRecords records = new ListPersonCountryRecords();

		while (rs.next()) {
			{
				Person obj = new Person();
				obj.setName(rs.getString("name"));
				records.getListPerson().add(obj);
			}
			{
				var name = rs.getString("name");
				var id = 0;

				Country obj = new Country(id, name);
				records.getListCountry().add(obj);
			}
		}

		return records;

	}


	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
		ListNotebooksRecords records = new ListNotebooksRecords();

		while (rs.next()) {
			{
				Notebook obj = new Notebook();
				obj.setSSD(rs.getBoolean("isSSD"));
				obj.setReleaseDate(rs.getDate("release_date"));
				obj.setName(rs.getString("name"));
				obj.setId(rs.getInt("id"));
				obj.setHasWifi(rs.getBoolean("has_wifi"));
				obj.setValue(rs.getBigDecimal("value"));
				var str = rs.getString("is_available");
				if (str != null && str.length() >= 1)
					obj.setIsAvailable(str.charAt(0));
				records.getListNotebook().add(obj);
			}
			{
				ListNotebooksGeneratedColumns obj = new ListNotebooksGeneratedColumns();
				obj.setFour(rs.getObject("four"));
				records.getGeneratedColumns().add(obj);
			}
		}

		return records;

	}


	public static SumValuesGroupedByCompanyRecords sumValuesGroupedByCompany(ResultSet rs) throws SQLException {
		SumValuesGroupedByCompanyRecords records = new SumValuesGroupedByCompanyRecords();

		while (rs.next()) {
			{
				Company obj = new Company();
				obj.setName(rs.getString("name"));
				obj.setId(rs.getInt("id"));
				records.getListCompany().add(obj);
			}
			{
				SumValuesGroupedByCompanyGeneratedColumns obj = new SumValuesGroupedByCompanyGeneratedColumns();
				obj.setSum(rs.getObject("sum"));
				records.getGeneratedColumns().add(obj);
			}
		}

		return records;

	}


}

