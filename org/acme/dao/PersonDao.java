package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.acme.domain.Person;

import com.github.mapresultset.api.Query;

public class PersonDao {

    @Query
    private static final String listPersonCountry = """
            select p.name, c.name
            from person as p join country as c on
              p.country_id = c.id
            """;
    
    public static List<Person> listPersonCountry() {
        try {
            var list = MapResultSet.listPersonCountry(executeQuery(listPersonCountry));
            System.out.println(list.getListCountry());
            return list.getListPerson();
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