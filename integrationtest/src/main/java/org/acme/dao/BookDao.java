package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.acme.domain.Book;

import io.github.tivrfoa.mapresultset.api.Query;

public class BookDao {

    @Query
    private static final String listBooksAndBookstores = """
            select b.name, b.author_name, bs.name
            from book as b join book_bookstore as bbs on
              b.author_name = bbs.author_name and
              b.name        = bbs.book_name join
              bookstore as bs on
              bs.id = bbs.bookstore_id
            order by b.author_name
            """;
    
    public static ListBooksAndBookstoresRecords listBooksAndBookstores() {
        try {
            return MapResultSet.listBooksAndBookstores(executeQuery(listBooksAndBookstores));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Query
    private static final String listBooksOnly = """
            select b.author_name, b.name
            from book as b
            order by b.author_name
            """;
    
    public static List<Book> listBooksOnly() {
        try {
            return MapResultSet.listBooksOnly(executeQuery(listBooksOnly));
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