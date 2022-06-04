package com.github.mapresultset;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class Structure {
    public static enum Type {
        CLASS,
        RECORD,
    }

    String fullName;
    Type type;
    Map<String, String> fields;

    public Structure(String fullName, String type) {
        this(fullName, type, new HashMap<>());
    }

    public Structure(String fullName, Type type) {
        this(fullName, type, new HashMap<>());
    }

    public Structure(String fullName, String type, Map<String, String> fields) {
        this(fullName, getType(type), fields);
    }

    public Structure(String fullName, Type type, Map<String, String> fields) {
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
