package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Person;
import org.acme.domain.Country;


public class ListPersonCountryRecords {


	private List<Person> listPerson = new ArrayList<>();
	public List<Person> getListPerson() {
		return listPerson;
	}
	private List<Country> listCountry = new ArrayList<>();
	public List<Country> getListCountry() {
		return listCountry;
	}
	private List<ListPersonCountryGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<ListPersonCountryGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}

	private static record PersonId() {}
	public List<Person> groupedByPerson() {
		// Map<PersonId, Person> map = new HashMap<>();
		
		return null;
	}
}
