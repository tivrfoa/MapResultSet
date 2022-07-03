package com.github.tivrfoa.mapresultset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.acme.dao.BookDao;
import org.acme.dao.ListBooksAndBookstoresRecords;
import org.acme.dao.ListNotebooksGeneratedColumns;
import org.acme.dao.ListNotebooksRecords;
import org.acme.dao.ListPersonAddressesRecords;
import org.acme.dao.ListPersonCountryGeneratedColumns;
import org.acme.dao.ListPersonCountryRecords;
import org.acme.dao.ListPersonPhonesAndCountryRecords;
import org.acme.dao.ListPersonPhonesRecords;
import org.acme.dao.NotebookDao;
import org.acme.dao.PersonDao;
import org.acme.dao.SumValuesGroupedByCompanyGeneratedColumns;
import org.acme.dao.SumValuesGroupedByCompanyRecords;
import org.acme.domain.Address;
import org.acme.domain.Book;
import org.acme.domain.Bookstore;
import org.acme.domain.Company;
import org.acme.domain.Country;
import org.acme.domain.Notebook;
import org.acme.domain.Person;
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

	@Test
	public void testListPersonCountry() {
		ListPersonCountryRecords records = PersonDao.listPersonCountry();

		List<ListPersonCountryGeneratedColumns> generatedColumns = records.getGeneratedColumns();

		assertEquals(4, generatedColumns.size());
		assertEquals("+84", generatedColumns.get(2).getPlus_sign_phone_code());


		List<Country> groupedByCountry = records.groupedByCountry();
		assertEquals(3, groupedByCountry.size());

		assertEquals("Brazil", groupedByCountry.get(0).name());
		assertEquals(11111111111112L, groupedByCountry.get(0).someBigNumber());

		// FIXME there should be just one person
		//   This is only fixable if person's id is in the query
		//   I'm not sure if this should be "fixed", like:
		//     - would these types of results happen often?
	    //         I think it's like the 80/20 rule: it would cause performance penalty
		//         (mapping check) for all cases, when 80% of the queries probably don't need this.
		//     - shouldn't the user declare the type as a HashSet instead of
		//       a list in those cases?
		assertEquals(2, groupedByCountry.get(0).listPerson().size());
		assertEquals("Marcos", groupedByCountry.get(0).listPerson().get(0).getName());
		assertEquals("Marcos", groupedByCountry.get(0).listPerson().get(1).getName());

		assertEquals(2, groupedByCountry.get(0).states().size());
		assertEquals("Acre", groupedByCountry.get(0).states().get(0).getName());
		assertEquals("Minas Gerais", groupedByCountry.get(0).states().get(1).getName());

		assertEquals(1, groupedByCountry.get(1).listPerson().size());
		assertEquals("Vietnam", groupedByCountry.get(1).name());
		assertEquals(new BigInteger("1111111111111311111111111113"), groupedByCountry.get(1).evenBigger());

		assertEquals(1, groupedByCountry.get(2).listPerson().size());
		assertEquals("Germany", groupedByCountry.get(2).name());
		assertEquals(49, groupedByCountry.get(2).phoneCode());
		assertEquals("Prost", groupedByCountry.get(2).states().get(0).getName());
	}

	@Test
	public void testListPersonPhones() {
		ListPersonPhonesRecords records = PersonDao.listPersonPhones();

		assertEquals(4, records.getListPerson().size());

		List<Person> groupedByPerson = records.groupedByPerson();
		assertEquals(3, groupedByPerson.size());

		var leandro =  groupedByPerson.get(0);
		assertEquals("Leandro", leandro.getName());
		assertEquals(2, leandro.getPhones().size());
		assertEquals(1111, leandro.getPhones().get(0).getNumber());
		assertEquals(1112, leandro.getPhones().get(1).getNumber());

		assertEquals("Guilherme", groupedByPerson.get(1).getName());
		assertEquals(1, groupedByPerson.get(1).getPhones().size());
		assertEquals(2222, groupedByPerson.get(1).getPhones().get(0).getNumber());

		assertEquals("Marcos", groupedByPerson.get(2).getName());
		assertEquals(1, groupedByPerson.get(2).getPhones().size());
		assertEquals(3333, groupedByPerson.get(2).getPhones().get(0).getNumber());
	}

	@Test
	public void testListPersonPhonesAndCountry() {
		ListPersonPhonesAndCountryRecords records = PersonDao.listPersonPhonesAndCountry();

		assertEquals(4, records.getListPerson().size());

		List<Person> groupedByPerson = records.groupedByPerson();
		assertEquals(3, groupedByPerson.size());

		assertEquals(Timestamp.valueOf("2022-06-16 10:29:20"), groupedByPerson.get(0).getBornTimestamp());
		assertEquals(Time.valueOf("10:29:23"), groupedByPerson.get(0).getWakeUpTime());

		assertEquals(Timestamp.valueOf("1989-07-03 08:09:10"), groupedByPerson.get(1).getBornTimestamp());
		assertEquals(Time.valueOf("10:29:21"), groupedByPerson.get(1).getWakeUpTime());

		assertEquals("Brazil", groupedByPerson.get(2).getCountry().name());
	}

	/**
	 * Testing 2 groupedBy: person and addresses
	 */
	@Test
	public void testListPersonAddresses() {
		ListPersonAddressesRecords records = PersonDao.listPersonAddresses();

		List<Person> groupedByPerson = records.groupedByPerson();
		assertEquals(3, groupedByPerson.size());
		
		assertEquals(3, groupedByPerson.get(0).getAddresses().size());
		assertEquals(1, groupedByPerson.get(1).getAddresses().size());
		assertEquals(2, groupedByPerson.get(2).getAddresses().size());

		assertEquals("Halong Bay", groupedByPerson.get(0).getAddresses().get(0).getStreet());
		assertEquals("Sapa", groupedByPerson.get(0).getAddresses().get(1).getStreet());
		assertEquals("Kyoto", groupedByPerson.get(0).getAddresses().get(2).getStreet());

		List<Address> groupedByAddress = records.groupedByAddress();
		assertEquals(5, groupedByAddress.size());

		var kyoto = groupedByAddress.get(2);
		assertEquals(2, kyoto.getListPerson().size());
		assertEquals("Leandro", kyoto.getListPerson().get(0).getName());
		assertEquals("Marcos", kyoto.getListPerson().get(1).getName());
	}

	public static void main(String[] args) {
		TestProcessor test = new TestProcessor();

		test.testBookBookstoreQuery();
		test.testListBooksOnly();
		test.testListNotebooksWithGeneratedColumns();
		test.testSumValuesGroupedByCompany();
		test.testListPersonCountry();
		test.testListPersonPhones();
		test.testListPersonAddresses();

		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("------------------------ Integration tests finished successfully. -----------------------");
		System.out.println("-----------------------------------------------------------------------------------------");
	}
}

