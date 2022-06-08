package org.acme.domain;

import com.github.mapresultset.api.Id;
import com.github.mapresultset.api.ManyToOne;
import com.github.mapresultset.api.Table;

@Table(name = "person")
public class Person {
	@Id
	private int id;
	private String name;
	@ManyToOne
	private Country country;

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

	@Override
	public String toString() {
		return "Person [country=" + country + ", id=" + id + ", name=" + name + "]";
	}

}