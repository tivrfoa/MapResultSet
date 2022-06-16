package com.github.mapresultset;

public enum ResultSetType {
    BIG_DECIMAL,
    BOOLEAN,
    CHAR,
    DATE,
    DOUBLE,
    FLOAT,
    INT,
    OBJECT,
    SHORT,
    STRING,
    TIME,
    TIMESTAMP;

    ResultSetType() {}

    public static ResultSetType fromString(String strType) {
        return switch (strType) {
            case "java.math.BigDecimal" -> BIG_DECIMAL;
            case "java.sql.Date" -> DATE;
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "double" -> DOUBLE;
            case "float" -> FLOAT;
            case "int" -> INT;
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
            case BOOLEAN -> "getBoolean";
            case DATE -> "getDate";
            case DOUBLE -> "getDouble";
            case FLOAT -> "getFloat";
            case INT -> "getInt";
            case OBJECT -> "getObject";
            case STRING -> "getString";
            case TIME -> "getTime";
            case TIMESTAMP -> "getTimestamp";
            default -> throw new RuntimeException("get method not mapped for:" + this);
        };
    }
}
