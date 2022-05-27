

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