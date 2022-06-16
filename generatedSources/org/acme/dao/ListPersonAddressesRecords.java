package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Person;
import org.acme.domain.Address;


public class ListPersonAddressesRecords {


	private List<Person> listPerson = new ArrayList<>();
	public List<Person> getListPerson() {
		return listPerson;
	}
	private List<Address> listAddress = new ArrayList<>();
	public List<Address> getListAddress() {
		return listAddress;
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
				map.put(key, curr);
				obj = curr;
				join.add(obj);
				obj.setAddresses(new ArrayList<>());

			}
			obj.getAddresses().add(getListAddress().get(i));

		}
		return join;
	}

	private static record AddressId(int id) {}
	public List<Address> groupedByAddress() {
		Map<AddressId, Address> map = new HashMap<>();
		List<Address> join = new ArrayList<>();
		int len = getListAddress().size();
		for (int i = 0; i < len; i++) {
			var curr = getListAddress().get(i);
			var key0 = curr.getId();

			var key = new AddressId(key0);
			var obj = map.get(key);
			if (obj == null) {
				map.put(key, curr);
				obj = curr;
				join.add(obj);
				obj.setListPerson(new ArrayList<>());

			}
			obj.getListPerson().add(getListPerson().get(i));

		}
		return join;
	}
}
