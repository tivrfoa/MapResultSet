package com.github.tivrfoa.mapresultset;

import java.util.HashSet;
import java.util.Set;

public class TableColumns {
    private String tableName;
    private Set<String> columns = new HashSet<>();
    
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public Set<String> getColumns() {
        return columns;
    }
    public void setColumns(Set<String> columns) {
        this.columns = columns;
    }
    @Override
    public String toString() {
        return "TableColumns [columns=" + columns + ", tableName=" + tableName + "]";
    }
}
