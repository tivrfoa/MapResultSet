package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Company;


public class SumValuesGroupedByCompanyRecords {


	private List<Company> listCompany = new ArrayList<>();
	public List<Company> getListCompany() {
		return listCompany;
	}
	private List<SumValuesGroupedByCompanyGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<SumValuesGroupedByCompanyGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}

	//##end##
}
