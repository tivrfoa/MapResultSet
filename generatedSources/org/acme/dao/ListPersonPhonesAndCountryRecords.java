package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Person;
import org.acme.domain.Phone;
import org.acme.domain.Country;


public class ListPersonPhonesAndCountryRecords {


	private List<Person> listPerson = new ArrayList<>();
	public List<Person> getListPerson() {
		return listPerson;
	}
	private List<Phone> listPhone = new ArrayList<>();
	public List<Phone> getListPhone() {
		return listPhone;
	}
	private List<Country> listCountry = new ArrayList<>();
	public List<Country> getListCountry() {
		return listCountry;
	}

	
	private static record PersonId(int id) {}
	public List<Person> groupedByPerson() {
		Map<PersonId, Person> map = new HashMap<>();
		List<Person> join = new ArrayList<>();
		int len = getListPerson().size();
		for (int i = 0; i < len; i++) {
			var curr = getListPerson().get(i);
			var key0 = curr.getId();

			var key = new PersonId(key0);
			var obj = map.get(key);
			if (obj == null) {
				obj = curr;
				obj.setPhones(Person.newLL());

				map.put(key, obj);
				join.add(obj);
			}
			obj.getPhones().add(getListPhone().get(i));

		}
		return join;
	}
}
