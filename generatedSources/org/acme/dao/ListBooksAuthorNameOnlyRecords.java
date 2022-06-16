package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Book;
import org.acme.domain.Bookstore;


public class ListBooksAuthorNameOnlyRecords {


	private List<Book> listBook = new ArrayList<>();
	public List<Book> getListBook() {
		return listBook;
	}
	private List<Bookstore> listBookstore = new ArrayList<>();
	public List<Bookstore> getListBookstore() {
		return listBookstore;
	}

	}
