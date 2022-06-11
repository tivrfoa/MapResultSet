package org.acme.dao;

import java.util.ArrayList;
import java.util.List;

import org.acme.domain.Person;
import org.acme.domain.Country;


public class ListPersonCountryPhoneCodeSubQueryRecords {


	private List<Person> listPerson = new ArrayList<>();
	public List<Person> getListPerson() {
		return listPerson;
	}
	private List<Country> listCountry = new ArrayList<>();
	public List<Country> getListCountry() {
		return listCountry;
	}
	private List<ListPersonCountryPhoneCodeSubQueryGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<ListPersonCountryPhoneCodeSubQueryGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}
}