package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Notebook;


public class MapResultSet {
	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
				ListNotebooksRecords records = new ListNotebooksRecords();

		while (rs.next()) {
			Notebook n = new Notebook();
			n.setId(rs.getInt("id"));
			n.setName(rs.getString("name"));

			records.listNotebook.add(n);

			ListNotebooksGeneratedColumns c = new ListNotebooksGeneratedColumns();
			c.four = rs.getInt("four");

			records.generatedColumns.add(c);
		}

		return records;

	}
}

