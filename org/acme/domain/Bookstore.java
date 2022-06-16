package org.acme.domain;

import com.github.mapresultset.api.Table;

@Table (name = "bookstore")
public class Bookstore {
    private int id;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Bookstore [id=" + id + ", name=" + name + "]";
	}
}
