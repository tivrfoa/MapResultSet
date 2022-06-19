package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Person;
import org.acme.domain.Country;
import org.acme.domain.State;


public class ListPersonCountryRecords {


	private List<Person> listPerson = new ArrayList<>();
	public List<Person> getListPerson() {
		return listPerson;
	}
	private List<Country> listCountry = new ArrayList<>();
	public List<Country> getListCountry() {
		return listCountry;
	}
	private List<State> listState = new ArrayList<>();
	public List<State> getListState() {
		return listState;
	}
	private List<ListPersonCountryGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<ListPersonCountryGeneratedColumns> getGeneratedColumns() {
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
				// copying record and initilializing lists
								int id = curr.id();
				float density = curr.density();
				java.lang.String name = curr.name();
				double squareMeters = curr.squareMeters();
				int phoneCode = curr.phoneCode();
				long someBigNumber = curr.someBigNumber();
				java.math.BigInteger evenBigger = curr.evenBigger();
				java.util.List<org.acme.domain.Person> listPerson = new ArrayList<>();
				java.util.List<org.acme.domain.State> states = new ArrayList<>();

				obj = new Country(id, density, name, squareMeters, phoneCode, someBigNumber, evenBigger, listPerson, states);

				map.put(key, obj);
				join.add(obj);
			}
			obj.listPerson().add(getListPerson().get(i));
obj.states().add(getListState().get(i));

		}
		return join;
	}
}
