package org.acme.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.github.mapresultset.api.Query;

import org.acme.domain.Notebook;

public class NotebookDao {

    @Query
    private static final String listNotebooks = """
            select id, name, 2 + 2 as four, value as value, release_date,
                   is_available, is_ssd as isSSD, has_wifi
            from notebook
            """;
    
    public static List<Notebook> listNotebooks() {
        try {
            var mysqlCon = new MySQLCon();

            Connection con = mysqlCon.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(listNotebooks);

            var list = MapResultSet.listNotebooks(rs);
            System.out.println(list.getGeneratedColumns().get(0).getFour());
            return list.listNotebook;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}