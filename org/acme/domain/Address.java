package org.acme.domain;

import java.util.List;

import com.github.mapresultset.api.Id;
import com.github.mapresultset.api.ManyToMany;
import com.github.mapresultset.api.Table;

@Table
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

}