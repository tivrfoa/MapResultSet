package com.github.tivrfoa.mapresultset;

import com.github.tivrfoa.mapresultset.api.Query;
import com.github.tivrfoa.mapresultset.api.Table;

import org.acme.dao.BookDao;
import org.acme.dao.NotebookDao;
import org.acme.dao.PersonDao;

public class TestProcessor {

	@Query
	final String listPhones = """
		select phone.id, c.name
		from phone join company as c on
		  phone.company_id = c.id
		""";

	// TODO create a test making sure that the lines
	//   below throw exception
	//
	// final String sql = null;
	// final String listPhones = "select id, phone.id from phone";
	/*final String listPhones = """
		select phone, model
		from phone join company as c on
		  phone.company_id = c.id
		""";
		*/

	// TODO it needs to create a method like:
	// List<Phone> phones = MapResultSet.listPhones(rs);

	public void testMain() {
		main(null);
	}

	public static void main(String[] args) {
		System.out.println("\n--------- listNotebooks ----------\n");
		System.out.println(NotebookDao.listNotebooks());
		System.out.println("\n--------- sumValuesGroupedByCompany ----------\n");
		System.out.println(NotebookDao.sumValuesGroupedByCompany());
		System.out.println("\n--------- listPersonCountry ----------\n");
		System.out.println(PersonDao.listPersonCountry());
		System.out.println("\n--------- listPersonPhones ----------\n");
		System.out.println(PersonDao.listPersonPhones());
		System.out.println("\n--------- listPersonPhonesAndCountry ----------\n");
		System.out.println(PersonDao.listPersonPhonesAndCountry());
		System.out.println("\n--------- listPersonAddresses ----------\n");
		System.out.println(PersonDao.listPersonAddresses());
		System.out.println("\n--------- listBooks ----------\n");
		System.out.println(BookDao.listBooks());
		System.out.println("\n--------- listBooksAuthorNameOnly ----------\n");
		System.out.println(BookDao.listBooksAuthorNameOnly());
	}

	@Table(name = "phone")
	public static class Phone {
		private int id;

		public int getId() {
			return this.id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
	}
}

