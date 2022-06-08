package org.acme.domain;

import com.github.mapresultset.api.Column;
import com.github.mapresultset.api.Table;

@Table (name = "country")
public record Country(int id, String name, @Column (name = "phone_code") int phoneCode) {
    
}
