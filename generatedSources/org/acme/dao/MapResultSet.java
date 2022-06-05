package org.acme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.acme.domain.Notebook;
import org.acme.domain.Company;


public class MapResultSet {
	
	public static ListNotebooksRecords listNotebooks(ResultSet rs) throws SQLException {
		ListNotebooksRecords records = new ListNotebooksRecords();

		while (rs.next()) {
			{
				Notebook obj = new Notebook();
				obj.setSSD(rs.getBoolean("isSSD"));
				obj.setReleaseDate(rs.getDate("release_date"));
				obj.setName(rs.getString("name"));
				obj.setId(rs.getInt("id"));
				obj.setHasWifi(rs.getBoolean("has_wifi"));
				obj.setValue(rs.getBigDecimal("value"));
				var str = rs.getString("is_available");
				if (str != null)
					obj.setIsAvailable(str.charAt(0));
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


	public static SumValuesGroupedByCompanyRecords sumValuesGroupedByCompany(ResultSet rs) throws SQLException {
		SumValuesGroupedByCompanyRecords records = new SumValuesGroupedByCompanyRecords();

		while (rs.next()) {
			{
				Company obj = new Company();
				obj.setName(rs.getString("name"));
				obj.setId(rs.getInt("id"));
				records.listCompany.add(obj);
			}
			{
				SumValuesGroupedByCompanyGeneratedColumns obj = new SumValuesGroupedByCompanyGeneratedColumns();
				obj.setSum(rs.getObject("sum"));
				records.getGeneratedColumns().add(obj);
			}
		}

		return records;

	}


}

