package org.acme.domain;

import io.github.tivrfoa.mapresultset.api.Table;

//@Table(name = "Notebook") // throws exception, because this table is already mapped in another class
@Table
public class OtherNotebook {
	private int id;

	public int getId() {
		return id;
	}
}

