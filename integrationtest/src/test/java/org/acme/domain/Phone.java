package org.acme.domain;

import com.github.tivrfoa.mapresultset.api.Table;

@Table
public class Phone {
	private int id;
	private int number;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Phone [id=" + id + ", number=" + number + "]";
	}
}