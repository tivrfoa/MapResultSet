package com.github.mapresultset;

public record Relationship (
    FullClassName javaStructWithMappingAnnotation,
    FullClassName partner, // change this variable name?! xD
    Type type) {

    public static enum Type {
        OneToMany,
        ManyToOne
    }
}
