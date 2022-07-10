package org.acme.domain;

import java.util.List;

import io.github.tivrfoa.mapresultset.api.Id;
import io.github.tivrfoa.mapresultset.api.ManyToMany;
import io.github.tivrfoa.mapresultset.api.Table;

@Table (name = "address")
public class Address {
	@Id
	private int id;
	private String street;
	@ManyToMany
	private List<Person> listPerson;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public List<Person> getListPerson() {
		return listPerson;
	}
	public void setListPerson(List<Person> listPerson) {
		this.listPerson = listPerson;
	}
	@Override
	public String toString() {
		var names = listPerson == null ? List.of() : listPerson.stream().map(p -> p.getName()).toList();
		return "Address [id=" + id + ", street=" + street + ", listPerson names=" + names + "]";
	}

}