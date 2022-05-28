## MapResultSet Query Restrictions

MapResultSet has some restrictions regarding your queries.
These restrictions could be handled, but I think they make
your query more readable too. xD

1. Join must be done using JOIN, not in WHERE clause;
2. Values returned from SELECT that are not a simple column must
   have an alias and be preceded with `AS`, eg: select 1 as one; select age + 18 as something;
3. The clauses must be in this order: select, from, [where], [group by], [having], [order by]
4. The variable annotated with `@Query` must be final, eg:
```java
@Query
final String listPeople;
```

Current *known* limitations (ps: please open an issue if you find others =))
 - it doesn't handle 'USING' in joins. MySQL only?


## Databases Java Connectors


### MySQL

https://dev.mysql.com/downloads/connector/j/


Sakila sample database: https://dev.mysql.com/doc/sakila/en/sakila-installation.html


#### Installing MySQL on Ubuntu

```sh
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql.service
sudo systemctl status mysql.service
```

#### Creating user and granting access to database

```sh
sudo mysql
Welcome to the MySQL monitor.  Commands end with ; or \g.
# ...

mysql> CREATE USER 'lesco'@'localhost' IDENTIFIED BY '123';
Query OK, 0 rows affected (0,02 sec)

mysql> create database d1;
Query OK, 1 row affected (0,02 sec)

mysql> grant all on d1.* to 'lesco'@'localhost';
Query OK, 0 rows affected (0,02 sec)

mysql> ^DBye
lesco@$ mysql -p

mysql> use d1
Database changed
```

#### Creating table and records

```sql
create table Person (
	id int primary key,
	name varchar(30)
);

insert into Person values
(1, 'Bob'),
(2, 'Any');
```
