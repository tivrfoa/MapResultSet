package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.acme.domain.Person;

import com.github.mapresultset.api.Query;

public class PersonDao {

    @Query
    private static final String listPersonCountryPhoneCodeSubQuery = """
            select p.id, p.name, c.id, c.name, c.phone_code as PhoneCode,
                   table_plus_phone_code.plus_sign_phone_code
            from person as p join country as c on
              p.country_id = c.id join (
                  select id, concat('+', phone_code) as plus_sign_phone_code
                  from country
              ) as table_plus_phone_code on
              table_plus_phone_code.id = c.id
            """;

    @Query
    private static final String listPersonCountry = """
            select p.id, p.name, c.id, c.name, c.phone_code as PhoneCode,
                   concat('+', phone_code) as plus_sign_phone_code
            from person as p join country as c on
              p.country_id = c.id
            """;

    @Query
    private static final String listPersonNameCountryName = """
            select p.name, c.name, c.phone_code as PhoneCode
            from person as p join country as c on
              p.country_id = c.id
            """;
    
    @Query
    private static final String listPersonPhones = """
            select p.id, p.name, phone.number
            from person as p join Phone as phone on
              p.id = phone.person_id
            """;
    
    @Query
    private static final String listPersonPhonesAndCountry = """
            select p.id, p.name, phone.number, c.name
            from person as p join Phone as phone on
              p.id = phone.person_id join
                 country as c on
              p.country_id = c.id
            """;
    
    @Query
    private static final String listPersonAdresses = """
            select p.id, p.name, a.street
            from person as p join person_address as pa on
              p.id = pa.person_id join address as a on
              a.id = pa.address_id
            """;
            
    public static List<Person> listPersonAdresses() {
        try {
            var list = MapResultSet.listPersonAdresses(executeQuery(listPersonAdresses));
            System.out.println(list.groupedByAdress());
            return list.groupedByPerson();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

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
    
    public static List<Person> listPersonPhones() {
        try {
            var list = MapResultSet.listPersonPhones(executeQuery(listPersonPhones));
            System.out.println(list.getListPhone());
            System.out.println(list.groupedByPerson());
            return list.getListPerson();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static List<Person> listPersonPhonesAndCountry() {
        try {
            return MapResultSet.listPersonPhonesAndCountry(executeQuery(listPersonPhonesAndCountry))
                    .groupedByPerson();
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