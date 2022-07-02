package com.github.tivrfoa.mapresultset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.acme.dao.BookDao;
import org.acme.dao.ListBooksRecords;
import org.acme.dao.ListNotebooksRecords;
import org.acme.dao.NotebookDao;
import org.acme.dao.PersonDao;
import org.acme.domain.Book;
import org.junit.jupiter.api.Test;

public class TestProcessor {

	@Test
	public void testMain() {
		main(null);
	}

	@Test
	public void testBookBookstoreQuery() {
		ListBooksRecords listBooks = BookDao.listBooks();
		List<Book> books = listBooks.groupedByBook();
		assertEquals(2, books.size());
		assertEquals(books.get(0).getAuthorName(), "Dan Brown");
		assertEquals(books.get(0).getName(), "Angels & Demons");
		assertEquals(books.get(1).getAuthorName(), "George R.R. Martin");
		assertEquals(books.get(1).getName(), "A Game of Thrones");
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

		new TestProcessor().testBookBookstoreQuery();

		System.out.println("\n--------- listBooksAuthorNameOnly ----------\n");
		System.out.println(BookDao.listBooksAuthorNameOnly());
	}
}

