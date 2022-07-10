package org.acme.domain;

import java.util.List;

import io.github.tivrfoa.mapresultset.api.Column;
import io.github.tivrfoa.mapresultset.api.Id;
import io.github.tivrfoa.mapresultset.api.ManyToMany;
import io.github.tivrfoa.mapresultset.api.Table;

@Table(name = "book")
public class Book {
	@Column (name = "author_name")
	@Id
    private String authorName;
	@Id
	private String name;
	@ManyToMany
	private List<Bookstore> bookstores;
	
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<Bookstore> getBookstores() {
		return bookstores;
	}
	public void setBookstores(List<Bookstore> bookstores) {
		this.bookstores = bookstores;
	}
	
	@Override
	public String toString() {
		return "Book [authorName=" + authorName + ", name=" + name + ", bookstores=" + bookstores + "]";
	}
	
}
