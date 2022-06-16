package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

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

	
	private static record CountryId(int id) {}
	public List<Country> groupedByCountry() {
		Map<CountryId, Country> map = new HashMap<>();
		List<Country> join = new ArrayList<>();
		int len = getListCountry().size();
		for (int i = 0; i < len; i++) {
			var curr = getListCountry().get(i);
			var key0 = curr.id();

			var key = new CountryId(key0);
			var obj = map.get(key);
			if (obj == null) {
				map.put(key, curr);
				obj = curr;
				join.add(obj);
				
			}
			obj.listPerson().add(getListPerson().get(i));

		}
		return join;
	}
}
