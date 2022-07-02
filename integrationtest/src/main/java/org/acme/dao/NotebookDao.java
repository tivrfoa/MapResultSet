package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.github.tivrfoa.mapresultset.api.Query;

public class NotebookDao {
	
	@Query
	private static final String listNotebooks = """
            select id, name, 2 + 2 as four, value as value, release_date,
                   is_available, is_ssd as isSSD, has_wifi, cpu_speed as clock,
                   production_cost
            from notebook
            order by id
            """;

    @Query
    private static final String listNotebooksName = """
            select id, name
            from notebook
            """;

    @Query
    private static final String sumValuesGroupedByCompany = """
            select c.id, c.name, sum(n.value) as sum
            from notebook as n join company as c on
              n.company_id = c.id
            group by c.id, c.name
            """;
    
    public static ListNotebooksRecords listNotebooks() {
        try {
            return MapResultSet.listNotebooks(executeQuery(listNotebooks));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static SumValuesGroupedByCompanyRecords sumValuesGroupedByCompany() {
        try {
            var rs = executeQuery(sumValuesGroupedByCompany);
            return MapResultSet.sumValuesGroupedByCompany(rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static ResultSet executeQuery(final String query) {
        try {
            var mysqlCon = new MySQLCon();

            Connection con = mysqlCon.getConnection();
            Statement stmt = con.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}