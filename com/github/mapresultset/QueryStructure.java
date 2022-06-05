package com.github.mapresultset;

import java.util.HashMap;
import java.util.Map;

import com.github.mapresultset.JavaStructure.Type;

/**
 * 
 */
public class QueryStructure {
    
    FullClassName fullClassName;
    Type type;
    Map<ColumnName, ColumnField> fields = new HashMap<>();

    public QueryStructure(FullClassName fullClassName, Type type) {
        this.fullClassName = fullClassName;
        this.type = type;
    }
}
