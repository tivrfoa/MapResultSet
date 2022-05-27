#!/bin/sh

## Testing MySQL JDBC connection

javac MySQLCon.java && java -cp .:classes/mysql-connector-java-8.0.29.jar MysqlCon
