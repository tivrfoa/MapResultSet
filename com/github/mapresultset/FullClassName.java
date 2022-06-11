package com.github.mapresultset;

public record FullClassName(String name) {
    public String getClassName() {
        int lastDot = name.lastIndexOf(".");
        if (lastDot == -1) return name;
        return name.substring(lastDot + 1);
    }
}
