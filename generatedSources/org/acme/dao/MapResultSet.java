package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Notebook;


public class MapResultSet {
	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
				ListNotebooksRecords records = new ListNotebooksRecords();
		System.out.println(records);
		while (rs.next()) {
			Notebook n = new Notebook();
			n.setId(rs.getInt(1));
			n.setName(rs.getString(2));
			System.out.println(n);

			records.listNotebook.add(n);
			System.out.println("added notebook");
			ListNotebooksGeneratedColumns c = new ListNotebooksGeneratedColumns();
			c.four = rs.getString(3);
			System.out.println(c.four);
			records.generatedColumns.add(c);
		}

		return records;

	}
}

