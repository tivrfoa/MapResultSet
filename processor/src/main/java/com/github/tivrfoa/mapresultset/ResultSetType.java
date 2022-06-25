package com.github.tivrfoa.mapresultset;

public enum ResultSetType {
    BIG_DECIMAL,
    BIG_INTEGER,
    BOOLEAN,
    CHAR,
    DATE,
    DOUBLE,
    DOUBLE_OBJ,
    FLOAT,
    FLOAT_OBJ,
    INT,
    INTEGER,
    LONG,
    LONG_OBJ,
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
            case "java.lang.double" -> DOUBLE_OBJ;
            case "float" -> FLOAT;
            case "java.lang.float" -> FLOAT_OBJ;
            case "int" -> INT;
            case "java.lang.Integer" -> INTEGER;
            case "long" -> LONG;
            case "java.lang.Long" -> LONG_OBJ;
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
            case DOUBLE, DOUBLE_OBJ -> "getDouble";
            case FLOAT, FLOAT_OBJ -> "getFloat";
            case INT, INTEGER -> "getInt";
            case LONG, LONG_OBJ -> "getLong";
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