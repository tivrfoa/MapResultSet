package org.acme.dao;

import java.util.ArrayList;
import java.util.List;

import org.acme.domain.Notebook;


public class ListNotebooksRecords {


	public List<Notebook> listNotebook = new ArrayList<>();
	private List<ListNotebooksGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<ListNotebooksGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}
}
