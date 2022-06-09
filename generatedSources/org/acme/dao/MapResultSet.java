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
			Person obj1 = new Person();
			obj1.setName(rs.getString("name"));
			records.getListPerson().add(obj1);

			var name = rs.getString("name");
			var phoneCode = rs.getInt("PhoneCode");
			var id = 0;
			var density = 0.0f;
			var squareMeters = 0.0;
			var someBigNumber = 0L;

			Country obj2 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber);
			records.getListCountry().add(obj2);

		}

		return records;

	}


	public static ListPersonNameCountryNameRecords listPersonNameCountryName(ResultSet rs) throws SQLException {
		ListPersonNameCountryNameRecords records = new ListPersonNameCountryNameRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("name"));
			records.getListPerson().add(obj1);

			var name = rs.getString("name");
			var phoneCode = rs.getInt("PhoneCode");
			var id = 0;
			var density = 0.0f;
			var squareMeters = 0.0;
			var someBigNumber = 0L;

			Country obj2 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber);
			records.getListCountry().add(obj2);

		}

		return records;

	}


	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
		ListNotebooksRecords records = new ListNotebooksRecords();

		while (rs.next()) {
			Notebook obj1 = new Notebook();
			obj1.setProductionCost(rs.getDouble("production_cost"));
			obj1.setSSD(rs.getBoolean("isSSD"));
			obj1.setReleaseDate(rs.getDate("release_date"));
			obj1.setName(rs.getString("name"));
			obj1.setCpuSpeed(rs.getFloat("clock"));
			obj1.setId(rs.getInt("id"));
			obj1.setHasWifi(rs.getBoolean("has_wifi"));
			obj1.setValue(rs.getBigDecimal("value"));
			var str = rs.getString("is_available");
			if (str != null && str.length() >= 1)
				obj1.setIsAvailable(str.charAt(0));
			records.getListNotebook().add(obj1);

			ListNotebooksGeneratedColumns obj2 = new ListNotebooksGeneratedColumns();
			obj2.setFour(rs.getObject("four"));
			records.getGeneratedColumns().add(obj2);

		}

		return records;

	}


	public static SumValuesGroupedByCompanyRecords sumValuesGroupedByCompany(ResultSet rs) throws SQLException {
		SumValuesGroupedByCompanyRecords records = new SumValuesGroupedByCompanyRecords();

		while (rs.next()) {
			Company obj1 = new Company();
			obj1.setName(rs.getString("name"));
			obj1.setId(rs.getInt("id"));
			records.getListCompany().add(obj1);

			SumValuesGroupedByCompanyGeneratedColumns obj2 = new SumValuesGroupedByCompanyGeneratedColumns();
			obj2.setSum(rs.getObject("sum"));
			records.getGeneratedColumns().add(obj2);

		}

		return records;

	}


}

