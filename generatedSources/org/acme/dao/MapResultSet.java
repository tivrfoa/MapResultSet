package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Notebook;


public class MapResultSet {
	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
		ListNotebooksRecords records = new ListNotebooksRecords();

		while (rs.next()) {
			{
				Notebook obj = new Notebook();
				obj.setReleaseDate(rs.getDate("release_date"));
				obj.setName(rs.getString("name"));
				obj.setId(rs.getInt("id"));
				obj.setValue(rs.getBigDecimal("value"));
				records.listNotebook.add(obj);
			}
			{
				ListNotebooksGeneratedColumns obj = new ListNotebooksGeneratedColumns();
				obj.setFour(rs.getObject("four"));
				records.getGeneratedColumns().add(obj);
			}
		}

		return records;

	}
}

