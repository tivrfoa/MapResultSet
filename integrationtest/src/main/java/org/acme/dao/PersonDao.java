package org.acme.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.acme.domain.Person;

import io.github.tivrfoa.mapresultset.api.Query;

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
            select p.id, p.name, c.id, c.name, c.someBigNumber, c.evenBigger,
                   c.phone_code as PhoneCode,
                   concat('+', phone_code) as plus_sign_phone_code,
                   s.name
            from person as p join country as c on
              p.country_id = c.id join state as s on
              s.country_id = c.id
            order by c.id, s.name
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
            order by p.id, phone.number
            """;
    
    @Query
    private static final String listPersonPhonesAndCountry = """
            select p.id, p.name, phone.number, c.name,
                   p.born_timestamp, p.wakeup_time
            from person as p join Phone as phone on
              p.id = phone.person_id join
                 country as c on
              p.country_id = c.id
            order by p.id, phone.number
            """;
    
    @Query
    private static final String listPersonAddresses_with_no_address_id = """
            select p.id, p.name, a.street
            from person as p join person_address as pa on
              p.id = pa.person_id join address as a on
              a.id = pa.address_id
            """;
            
    public static List<Person> listPersonAddressesWithNoAddressId() {
        try {
            var list = MapResultSet.listPersonAddresses(executeQuery(listPersonAddresses_with_no_address_id));
            return list.groupedByPerson();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Query
    private static final String listPersonAddresses = """
            select p.id, p.name, a.id, a.street
            from person as p join person_address as pa on
              p.id = pa.person_id join address as a on
              a.id = pa.address_id
            order by p.id, a.id
            """;
            
    public static ListPersonAddressesRecords listPersonAddresses() {
        try {
            return MapResultSet.listPersonAddresses(executeQuery(listPersonAddresses));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static ListPersonCountryRecords listPersonCountry() {
        try {
            return MapResultSet.listPersonCountry(executeQuery(listPersonCountry));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static ListPersonPhonesRecords listPersonPhones() {
        try {
            return MapResultSet.listPersonPhones(executeQuery(listPersonPhones));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static ListPersonPhonesAndCountryRecords listPersonPhonesAndCountry() {
        try {
            return MapResultSet.listPersonPhonesAndCountry(executeQuery(listPersonPhonesAndCountry));
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