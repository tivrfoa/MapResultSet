package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
// TODO the two imports below should be added conditionally
// when there's a groupBy method
import java.util.Map;
import java.util.HashMap;

import org.acme.domain.Notebook;


public class ListNotebooksRecords {


	private List<Notebook> listNotebook = new ArrayList<>();
	public List<Notebook> getListNotebook() {
		return listNotebook;
	}
	private List<ListNotebooksGeneratedColumns> generatedColumns = new ArrayList<>();
	public List<ListNotebooksGeneratedColumns> getGeneratedColumns() {
		return generatedColumns;
	}

	//##end##
}
