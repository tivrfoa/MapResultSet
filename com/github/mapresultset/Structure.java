package com.github.mapresultset;

import java.util.ArrayList;
import java.util.List;

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
    List<Field> fields = new ArrayList<>();

    public Structure(String fullName, String type, List<Field> fields) {
        this.fullName = fullName;
        this.type = switch (type) {
            case "CLASS" -> Type.CLASS;
            case "RECORD" -> Type.RECORD;
            default -> throw new RuntimeException("Invalid structure type.");
        };
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Structure [ fullName=" + fullName + ", type=" + type + ", fields=" + fields + "]";
    }

}
