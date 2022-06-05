package org.acme.dao;

import java.util.ArrayList;
import java.util.List;

import org.acme.domain.Company;


public class SumValuesGroupedByCompanyRecords {


	public List<Company> listCompany = new ArrayList<>();
	private List<SumValuesGroupedByCompanyGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<SumValuesGroupedByCompanyGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}
}
