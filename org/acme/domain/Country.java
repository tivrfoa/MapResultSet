package org.acme.domain;

import com.github.mapresultset.Table;

@Table (name = "country")
public record Country(int id, String name) {
    
}
