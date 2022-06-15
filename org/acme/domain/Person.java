package org.acme.domain;

import java.util.List;

import com.github.mapresultset.api.Id;
import com.github.mapresultset.api.ManyToMany;
import com.github.mapresultset.api.ManyToOne;
import com.github.mapresultset.api.OneToMany;
import com.github.mapresultset.api.Table;

@Table(name = "person")
public class Person {
	@Id
	private int id;
	private String name;
	@OneToMany
	private List<Phone> phones;
	@ManyToOne
	private Country country;
	@ManyToMany
	private List<Address> addresses;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public List<Phone> getPhones() {
		return phones;
	}

	public void setPhones(List<Phone> phones) {
		this.phones = phones;
	}

	@Override
	public String toString() {
		return "Person [addresses=" + addresses + ", country=" + country + ", id=" + id + ", name=" + name + ", phones="
				+ phones + "]";
	}

}