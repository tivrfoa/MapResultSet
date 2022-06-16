package com.github.mapresultset;

public enum ResultSetType {
    BIG_DECIMAL,
    BIG_INTEGER,
    BOOLEAN,
    CHAR,
    DATE,
    DOUBLE,
    FLOAT,
    INT,
    LONG,
    OBJECT,
    SHORT,
    STRING,
    TIME,
    TIMESTAMP;

    ResultSetType() {}

    public static ResultSetType fromString(String strType) {
        return switch (strType) {
            case "java.math.BigDecimal" -> BIG_DECIMAL;
            case "java.math.BigInteger" -> BIG_INTEGER;
            case "java.sql.Date" -> DATE;
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "double" -> DOUBLE;
            case "float" -> FLOAT;
            case "int" -> INT;
            case "long" -> LONG;
            case "Object" -> OBJECT;
            case "String", "java.lang.String" -> STRING;
            case "java.sql.Time" -> TIME;
            case "java.sql.Timestamp" -> TIMESTAMP;
            default -> throw new RuntimeException("Type '" + strType + "' is not supported.");
        };
    }

    public String getResultSetGetMethod() {
        return switch (this) {
            case BIG_DECIMAL -> "getBigDecimal";
            // case BIG_INTEGER -> needs special handling, bc there's no getBigInteger
            case BOOLEAN -> "getBoolean";
            case DATE -> "getDate";
            case DOUBLE -> "getDouble";
            case FLOAT -> "getFloat";
            case INT -> "getInt";
            case LONG -> "getLong";
            case OBJECT -> "getObject";
            case STRING -> "getString";
            case TIME -> "getTime";
            case TIMESTAMP -> "getTimestamp";
            default -> throw new RuntimeException("get method not mapped for:" + this);
        };
    }
}

/*
 * 
 * https://dev.mysql.com/doc/refman/8.0/en/integer-types.html
 * 
 * https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
 * 
 * https://stackoverflow.com/questions/31656929/can-javas-long-hold-a-sql-bigint20-value
 * 
 */