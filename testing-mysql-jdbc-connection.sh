#!/bin/sh

## Testing MySQL JDBC connection

javac -d classes/ MySQLCon.java &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar MySQLCon
