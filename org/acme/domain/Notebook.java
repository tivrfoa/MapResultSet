package org.acme.domain;

import com.github.mapresultset.Table;

@Table(name = "notebook")
public class Notebook {
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
		return "Notebook [id=" + id + ", name=" + name + "]";
	}
	
}

