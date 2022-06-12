package com.github.mapresultset;

import java.util.HashMap;
import java.util.Map;

import com.github.mapresultset.JavaStructure.Type;

/**
 * 
 */
public class QueryClassStructure {
    
    FullClassName fullClassName;
    Type type;
    Map<ColumnName, ColumnField> fields = new HashMap<>();

    public QueryClassStructure(FullClassName fullClassName, Type type) {
        this.fullClassName = fullClassName;
        this.type = type;
    }

    @Override
    public String toString() {
        return "QueryClassStructure [fields=" + fields + ", fullClassName=" + fullClassName + ", type=" + type + "]";
    }
    
}
