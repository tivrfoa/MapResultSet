package tests;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import tests.TestProcessor.Phone;
import org.acme.domain.Company;


public class ListPhonesRecords {


	private List<Phone> listPhone = new ArrayList<>();
	public List<Phone> getListPhone() {
		return listPhone;
	}
	private List<Company> listCompany = new ArrayList<>();
	public List<Company> getListCompany() {
		return listCompany;
	}

	//##end##
}
