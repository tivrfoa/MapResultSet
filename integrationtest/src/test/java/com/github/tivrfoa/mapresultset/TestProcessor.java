package com.github.tivrfoa.mapresultset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.acme.dao.BookDao;
import org.acme.dao.ListBooksAndBookstoresRecords;
import org.acme.dao.ListNotebooksGeneratedColumns;
import org.acme.dao.ListNotebooksRecords;
import org.acme.dao.NotebookDao;
import org.acme.dao.PersonDao;
import org.acme.dao.SumValuesGroupedByCompanyGeneratedColumns;
import org.acme.dao.SumValuesGroupedByCompanyRecords;
import org.acme.domain.Book;
import org.acme.domain.Bookstore;
import org.acme.domain.Company;
import org.acme.domain.Notebook;
import org.junit.jupiter.api.Test;

public class TestProcessor {

	@Test
	public void testBookBookstoreQuery() {
		ListBooksAndBookstoresRecords records = BookDao.listBooksAndBookstores();

		List<Book> books = records.groupedByBook();
		assertEquals(2, books.size());
		assertEquals(books.get(0).getAuthorName(), "Dan Brown");
		assertEquals(books.get(0).getName(), "Angels & Demons");
		assertEquals(books.get(1).getAuthorName(), "George R.R. Martin");
		assertEquals(books.get(1).getName(), "A Game of Thrones");

		assertEquals(3, books.get(0).getBookstores().size());
		assertEquals(2, books.get(1).getBookstores().size());

		List<Bookstore> bookstores = books.get(0).getBookstores();
		assertEquals("Bookstore 1", bookstores.get(0).getName());
		assertEquals("Bookstore 2", bookstores.get(1).getName());
		assertEquals("Bookstore 3", bookstores.get(2).getName());
		
		bookstores = records.getListBookstore();
		assertEquals("Bookstore 1", bookstores.get(0).getName());
		assertEquals("Bookstore 2", bookstores.get(1).getName());
		assertEquals("Bookstore 3", bookstores.get(2).getName());
	}

	@Test
	public void testListBooksOnly() {
		List<Book> books = BookDao.listBooksOnly();

		assertEquals(2, books.size());
		assertEquals(books.get(0).getAuthorName(), "Dan Brown");
		assertEquals(books.get(0).getName(), "Angels & Demons");
		assertEquals(books.get(1).getAuthorName(), "George R.R. Martin");
		assertEquals(books.get(1).getName(), "A Game of Thrones");

		assertNull(books.get(0).getBookstores());
		assertNull(books.get(1).getBookstores());
	}

	@Test
	public void testListNotebooksWithGeneratedColumns() {
		ListNotebooksRecords records = NotebookDao.listNotebooks();

		List<Notebook> listNotebook = records.getListNotebook();
		assertEquals(3, listNotebook.size());

		assertEquals("ideapad", listNotebook.get(0).getName());
		assertEquals(new BigDecimal("9581.20"), listNotebook.get(0).getValue());
		assertEquals(Date.valueOf("2016-01-02"), listNotebook.get(0).getReleaseDate());
		assertEquals('S', listNotebook.get(0).getIsAvailable());
		assertEquals(true, listNotebook.get(0).isSSD());
		assertEquals(3000.10009765625, listNotebook.get(0).getCpuSpeed()); // don't use float kids =)
		assertEquals(586.52, listNotebook.get(0).getProductionCost());

		assertEquals("aspire 3", listNotebook.get(1).getName());
		assertEquals('N', listNotebook.get(1).getIsAvailable());
		assertEquals(false, listNotebook.get(1).isSSD());
		assertEquals(3000.1, listNotebook.get(1).getProductionCost());

		assertEquals("aspire 5", listNotebook.get(2).getName());

		List<ListNotebooksGeneratedColumns> generatedColumns = records.getGeneratedColumns();
		assertEquals(3, generatedColumns.size());
		assertEquals(4L, generatedColumns.get(0).getFour());
	}

	@Test
	public void testSumValuesGroupedByCompany() {
		SumValuesGroupedByCompanyRecords records = NotebookDao.sumValuesGroupedByCompany();

		List<Company> listCompany = records.getListCompany();
		List<SumValuesGroupedByCompanyGeneratedColumns> generatedColumns = records.getGeneratedColumns();
		assertEquals(2, generatedColumns.size());

		assertEquals("Lenovo", listCompany.get(0).getName());
		assertEquals(new BigDecimal("9581.20"), generatedColumns.get(0).getSum());

		assertEquals("Acer", listCompany.get(1).getName());
		assertEquals(new BigDecimal("3000.00"), generatedColumns.get(1).getSum());
	}

	public static void main(String[] args) {
		TestProcessor test = new TestProcessor();

		test.testBookBookstoreQuery();
		test.testListBooksOnly();
		test.testListNotebooksWithGeneratedColumns();
		test.testSumValuesGroupedByCompany();

		System.out.println("\n--------- listPersonCountry ----------\n");
		System.out.println(PersonDao.listPersonCountry());
		System.out.println("\n--------- listPersonPhones ----------\n");
		System.out.println(PersonDao.listPersonPhones());
		System.out.println("\n--------- listPersonPhonesAndCountry ----------\n");
		System.out.println(PersonDao.listPersonPhonesAndCountry());
		System.out.println("\n--------- listPersonAddresses ----------\n");
		System.out.println(PersonDao.listPersonAddresses());

		
	}
}

