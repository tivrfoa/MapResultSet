package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Person;
import org.acme.domain.Notebook;
import org.acme.domain.Company;
import org.acme.domain.Country;
import org.acme.domain.Phone;
import org.acme.domain.Address;


public class MapResultSet {
	
	public static ListPersonCountryPhoneCodeSubQueryRecords listPersonCountryPhoneCodeSubQuery(ResultSet rs) throws SQLException {
		ListPersonCountryPhoneCodeSubQueryRecords records = new ListPersonCountryPhoneCodeSubQueryRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			ListPersonCountryPhoneCodeSubQueryGeneratedColumns obj2 = new ListPersonCountryPhoneCodeSubQueryGeneratedColumns();
			obj2.setTable_plus_phone_code(rs.getObject("table_plus_phone_code"));
			records.getGeneratedColumns().add(obj2);

			java.lang.String name = rs.getString("c.name");
			int id = rs.getInt("c.id");
			int phoneCode = rs.getInt("c.PhoneCode");
			float density = 0.0f;
			double squareMeters = 0.0;
			long someBigNumber = 0L;
			java.util.List<org.acme.domain.Person> listPerson = null;

			Country obj3 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber, listPerson);
			records.getListCountry().add(obj3);

			obj1.setCountry(obj3);
		}

		return records;

	}


	public static ListPersonCountryRecords listPersonCountry(ResultSet rs) throws SQLException {
		ListPersonCountryRecords records = new ListPersonCountryRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			ListPersonCountryGeneratedColumns obj2 = new ListPersonCountryGeneratedColumns();
			obj2.setPlus_sign_phone_code(rs.getObject("plus_sign_phone_code"));
			records.getGeneratedColumns().add(obj2);

			java.lang.String name = rs.getString("c.name");
			int id = rs.getInt("c.id");
			int phoneCode = rs.getInt("c.PhoneCode");
			float density = 0.0f;
			double squareMeters = 0.0;
			long someBigNumber = 0L;
			java.util.List<org.acme.domain.Person> listPerson = null;

			Country obj3 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber, listPerson);
			records.getListCountry().add(obj3);

			obj1.setCountry(obj3);
		}

		return records;

	}


	public static ListPersonNameCountryNameRecords listPersonNameCountryName(ResultSet rs) throws SQLException {
		ListPersonNameCountryNameRecords records = new ListPersonNameCountryNameRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			records.getListPerson().add(obj1);

			java.lang.String name = rs.getString("c.name");
			int phoneCode = rs.getInt("c.PhoneCode");
			int id = 0;
			float density = 0.0f;
			double squareMeters = 0.0;
			long someBigNumber = 0L;
			java.util.List<org.acme.domain.Person> listPerson = null;

			Country obj2 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber, listPerson);
			records.getListCountry().add(obj2);

			obj1.setCountry(obj2);
		}

		return records;

	}


	public static ListPersonPhonesRecords listPersonPhones(ResultSet rs) throws SQLException {
		ListPersonPhonesRecords records = new ListPersonPhonesRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			Phone obj2 = new Phone();
			obj2.setNumber(rs.getInt("phone.number"));
			records.getListPhone().add(obj2);

		}

		return records;

	}


	public static ListPersonPhonesAndCountryRecords listPersonPhonesAndCountry(ResultSet rs) throws SQLException {
		ListPersonPhonesAndCountryRecords records = new ListPersonPhonesAndCountryRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			Phone obj2 = new Phone();
			obj2.setNumber(rs.getInt("phone.number"));
			records.getListPhone().add(obj2);

			java.lang.String name = rs.getString("c.name");
			int id = 0;
			float density = 0.0f;
			double squareMeters = 0.0;
			int phoneCode = 0;
			long someBigNumber = 0L;
			java.util.List<org.acme.domain.Person> listPerson = null;

			Country obj3 = new Country(id, density, name, squareMeters, phoneCode, someBigNumber, listPerson);
			records.getListCountry().add(obj3);

			obj1.setCountry(obj3);
		}

		return records;

	}


	public static ListPersonAddresses_with_no_address_idRecords listPersonAddresses_with_no_address_id(ResultSet rs) throws SQLException {
		ListPersonAddresses_with_no_address_idRecords records = new ListPersonAddresses_with_no_address_idRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			Address obj2 = new Address();
			obj2.setStreet(rs.getString("a.street"));
			records.getListAddress().add(obj2);

		}

		return records;

	}


	public static ListPersonAddressesRecords listPersonAddresses(ResultSet rs) throws SQLException {
		ListPersonAddressesRecords records = new ListPersonAddressesRecords();

		while (rs.next()) {
			Person obj1 = new Person();
			obj1.setName(rs.getString("p.name"));
			obj1.setId(rs.getInt("p.id"));
			records.getListPerson().add(obj1);

			Address obj2 = new Address();
			obj2.setStreet(rs.getString("a.street"));
			obj2.setId(rs.getInt("a.id"));
			records.getListAddress().add(obj2);

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
			obj1.setName(rs.getString("c.name"));
			obj1.setId(rs.getInt("c.id"));
			records.getListCompany().add(obj1);

			SumValuesGroupedByCompanyGeneratedColumns obj2 = new SumValuesGroupedByCompanyGeneratedColumns();
			obj2.setSum(rs.getObject("sum"));
			records.getGeneratedColumns().add(obj2);

		}

		return records;

	}


}

