package com.github.tivrfoa.mapresultset;

public record ParsedTable(
    String tableName,
    String tableAlias,
    boolean isTemporaryTable) implements Comparable<ParsedTable> {

    @Override
    public int compareTo(ParsedTable other) {
        int comp1 = this.tableName.compareTo(other.tableName);
        if (comp1 != 0) return comp1;
        // return this.tableAlias.compareTo(other.tableAlias);
        throw new RuntimeException("Is it possible to get here?");
    }
}
