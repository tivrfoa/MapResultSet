package org.acme.domain;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import io.github.tivrfoa.mapresultset.api.Column;
import io.github.tivrfoa.mapresultset.api.Id;
import io.github.tivrfoa.mapresultset.api.ManyToMany;
import io.github.tivrfoa.mapresultset.api.ManyToOne;
import io.github.tivrfoa.mapresultset.api.OneToMany;
import io.github.tivrfoa.mapresultset.api.Table;

@Table(name = "person")
public class Person {
	@Id
	private int id;
	private String name;
	@Column (name = "born_timestamp")
	private Timestamp bornTimestamp;
	@Column (name = "wakeup_time")
	private Time wakeUpTime;
	@OneToMany (createWith = "newLL()", addWith = "add")
	private List<Phone> phones;
	@ManyToOne
	private Country country;
	@ManyToMany
	private List<Address> addresses;

	public static LinkedList<Phone> newLL() {
		return new LinkedList<>();
	}

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

	public Timestamp getBornTimestamp() {
		return bornTimestamp;
	}

	public void setBornTimestamp(Timestamp bornTimestamp) {
		this.bornTimestamp = bornTimestamp;
	}

	public Time getWakeUpTime() {
		return wakeUpTime;
	}

	public void setWakeUpTime(Time wakeUpTime) {
		this.wakeUpTime = wakeUpTime;
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

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", bornTimestamp=" + bornTimestamp +
				", wakeUpTime=" + wakeUpTime + ", country=" + country +
				", addresses=" + addresses + ", phones=" + phones +  "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (id != other.id)
			return false;
		return true;
	}

	

}