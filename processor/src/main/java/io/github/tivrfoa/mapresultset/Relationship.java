package io.github.tivrfoa.mapresultset;

public record Relationship (
    FullClassName owner,
    FullClassName partner, // change this variable name?! xD
    FieldName partnerFieldName,
    Type type) {

    public static enum Type {
        OneToOne,
        OneToMany,
        ManyToOne,
        ManyToMany
    }
}
