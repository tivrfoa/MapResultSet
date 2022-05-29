package org.acme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.github.mapresultset.Query;

import org.acme.domain.Notebook;

/**
 * NotebookDao
 * 
 * https://www.baeldung.com/java-dao-pattern
 */
public class NotebookDao {

    @Query
    private static final String listNotebooks = """
            select id, name
            from notebook
            """;
    
    public static List<Notebook> listNotebooks() {
        var mysqlCon = new MySQLCon();

        Connection con = mysqlCon.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(listNotebooks);

        return MapResultSet.listNotebooks(rs);
    }
}