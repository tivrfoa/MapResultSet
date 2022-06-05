package com.github.mapresultset;

public enum ResultSetTypes {
    BIG_DECIMAL,
    BOOLEAN,
    CHAR,
    DATE,
    INT,
    OBJECT,
    SHORT,
    STRING,
    TIME,
    TIMESTAMP;

    ResultSetTypes() {}

    public static ResultSetTypes fromString(String strType) {
        return switch (strType) {
            case "java.math.BigDecimal" -> BIG_DECIMAL;
            case "java.sql.Date" -> DATE;
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "int" -> INT;
            case "Object" -> OBJECT;
            case "String", "java.lang.String" -> STRING;
            default -> throw new RuntimeException("Type '" + strType + "' is not supported.");
        };
    }

    public String getResultSetGetMethod() {
        return switch (this) {
            case BIG_DECIMAL -> "getBigDecimal";
            case BOOLEAN -> "getBoolean";
            case DATE -> "getDate";
            case INT -> "getInt";
            case OBJECT -> "getObject";
            case STRING -> "getString";
            default -> throw new RuntimeException("get method not mapped for:" + this);
        };
    }
}
