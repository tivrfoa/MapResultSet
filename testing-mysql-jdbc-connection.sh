#!/bin/sh

## Testing MySQL JDBC connection

javac -d classes/ org/acme/dao/MySQLCon.java &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar org.acme.dao.MySQLCon
