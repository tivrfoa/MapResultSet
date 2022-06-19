package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Book;
import org.acme.domain.Bookstore;


public class ListBooksRecords {


	private List<Book> listBook = new ArrayList<>();
	public List<Book> getListBook() {
		return listBook;
	}
	private List<Bookstore> listBookstore = new ArrayList<>();
	public List<Bookstore> getListBookstore() {
		return listBookstore;
	}

	
	private static record BookId(java.lang.String authorName, java.lang.String name) {}
	public List<Book> groupedByBook() {
		Map<BookId, Book> map = new HashMap<>();
		List<Book> join = new ArrayList<>();
		int len = getListBook().size();
		for (int i = 0; i < len; i++) {
			var curr = getListBook().get(i);
			var key0 = curr.getAuthorName();
var key1 = curr.getName();

			var key = new BookId(key0, key1);
			var obj = map.get(key);
			if (obj == null) {
				obj = curr;
				obj.setBookstores(new ArrayList<>());

				map.put(key, obj);
				join.add(obj);
			}
			obj.getBookstores().add(getListBookstore().get(i));

		}
		return join;
	}
}
