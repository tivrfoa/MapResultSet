package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.github.tivrfoa.mapresultset.api.Query;

import org.acme.domain.Company;

public class NotebookDao {
	
	@Query
	private static final String listNotebooks = """
            select id, name, 2 + 2 as four, value as value, release_date,
                   is_available, is_ssd as isSSD, has_wifi, cpu_speed as clock,
                   production_cost
            from notebook
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
            // System.out.println(list.getGeneratedColumns().get(0).getFour());
            // return list.getListNotebook();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static List<Company> sumValuesGroupedByCompany() {
        try {
            var rs = executeQuery(sumValuesGroupedByCompany);
            var list = MapResultSet.sumValuesGroupedByCompany(rs);
            for (var v : list.getGeneratedColumns())
                System.out.println(v.getSum());
            return list.getListCompany();
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