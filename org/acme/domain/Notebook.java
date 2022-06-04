package org.acme.domain;

import java.math.BigDecimal;

import com.github.mapresultset.api.Table;

@Table(name = "notebook")
public class Notebook {
	private int id;
	private String name;
	private BigDecimal value;

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

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
		return "Notebook [id=" + id + ", name=" + name + ", value=" + value + "]";
	}
	
}

