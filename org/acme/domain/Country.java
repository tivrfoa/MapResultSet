package org.acme.domain;

import java.util.LinkedList;
import java.util.List;

import com.github.mapresultset.api.Column;
import com.github.mapresultset.api.OneToMany;
import com.github.mapresultset.api.Table;

@Table (name = "country")
public record Country(int id, float density, String name,
        double squareMeters, @Column (name = "phone_code") int phoneCode,
        long someBigNumber,
        // It doesn't make sense for Country to have a list of Person ...
        // It's just for testing.
        @OneToMany (createWith = "newLinkedList", addWith = "add") List<Person> listPerson) {
    
                public static List<Person> newLinkedList() {
                        return new LinkedList<Person>();
                }
}
