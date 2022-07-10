package io.github.tivrfoa.mapresultset;

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
    RecordComponent recordComponents;

    public JavaStructure(String fullName, String type, Map<FieldName, FieldType> fields,
            RecordComponent recordComponents) {
        this(fullName, getType(type), fields, recordComponents);
    }

    public JavaStructure(String fullName, Type type, Map<FieldName, FieldType> fields, RecordComponent recordComponents) {
        this.fullName = fullName;
        this.type = type;
        this.fields = fields;
        if (type == Type.RECORD && recordComponents == null) {
            throw new RuntimeException("Must pass record components (record parameters) when the type is a 'record'.");
        }
        this.recordComponents = recordComponents;
    }

    public static Type getType(String type) {
        return switch (type) {
            case "CLASS" -> Type.CLASS;
            case "RECORD" -> Type.RECORD;
            default -> throw new RuntimeException("Invalid structure type.");
        };
    }

    @Override
    public String toString() {
        return "\nStructure [ fullName=" + fullName + ", type=" + type + ", fields=" + fields +
                ", recordComponents=" + recordComponents + "]\n";
    }

}
