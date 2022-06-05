package com.github.mapresultset;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class JavaStructure {
    public static enum Type {
        CLASS,
        RECORD,
    }

    String fullName;
    Type type;
    Map<FieldName, FieldType> fields;

    public JavaStructure(String fullName, String type) {
        this(fullName, type, new HashMap<>());
    }

    public JavaStructure(String fullName, Type type) {
        this(fullName, type, new HashMap<>());
    }

    public JavaStructure(String fullName, String type, Map<FieldName, FieldType>fields) {
        this(fullName, getType(type), fields);
    }

    public JavaStructure(String fullName, Type type, Map<FieldName, FieldType> fields) {
        this.fullName = fullName;
        this.type = type;
        this.fields = fields;
    }

    private static Type getType(String type) {
        return switch (type) {
            case "CLASS" -> Type.CLASS;
            case "RECORD" -> Type.RECORD;
            default -> throw new RuntimeException("Invalid structure type.");
        };
    }

    @Override
    public String toString() {
        return "Structure [ fullName=" + fullName + ", type=" + type + ", fields=" + fields + "]";
    }

}
