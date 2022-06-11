package org.acme.dao;

import java.util.ArrayList;
import java.util.List;

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
