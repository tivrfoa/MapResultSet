# MapResultSet

The goal is to automate the manual process of setting the object's properties from a ResultSet.

MapResultSet is not a query validator, so make sure your
query actually works before you use it in your Java project.

**TODO**
  - show a picture comparing side-by-side

## Annotations

MapResultSet uses two annotations: `Table` and `Query`

The variable annotated with `@Query` must be final, eg:
```java
@Query
final String listPeople;
```

And due to [Java Annotation Processor limitation](https://stackoverflow.com/questions/3285652/how-can-i-create-an-annotation-processor-that-processes-a-local-variable), the queries must not
be local variables.

**TODO** explain how to use these annotations

## MapResultSet Query Restrictions

MapResultSet has some restrictions regarding your queries.
These restrictions could be handled, but I think they make
your query more readable too. xD

1. Join must be done using JOIN, not in WHERE clause;
2. Values returned from SELECT that are not a simple column name must
   have an alias and be preceded with `AS`, eg: select 1 as one; select age + 18 as something;
3. Columns in `select` must be preceded by the table name (or alias) if the `from` clause contains
more than one table;
4. Table alias must be preceded by `AS`

Current *known* limitations (ps: please open an issue if you find others =))
 - it doesn't handle 'USING' in joins. MySQL only?


## Generated Classes Structure

### Package

Generated classes will be in the same package that contains the `@Query`.

### Class Name

There will be one MapResultSet class created per package that contains a `@Query`.
Each `@Query` also creates a class with the name of the query with the first letter in uppercase,
followed by Records, eg: the query below will create a class called ListPeopleRecords.

```java
@Query
final String listPeople;
```

**ps:** if the query contains only one table and there's no temporary columns,
then this class is not created and MapResultSet returns a list of the only class
in the query.


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
