package com.github.tivrfoa.mapresultset;

import org.acme.dao.BookDao;
import org.acme.dao.ListNotebooksRecords;
import org.acme.dao.NotebookDao;
import org.acme.dao.PersonDao;
import org.junit.jupiter.api.Test;

public class TestProcessor {

	@Test
	public void testMain() {
		main(null);
	}

	public static void main(String[] args) {
		System.out.println("\n--------- listNotebooks ----------\n");
		ListNotebooksRecords listNotebooks = NotebookDao.listNotebooks();
		System.out.println(listNotebooks);
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
}

