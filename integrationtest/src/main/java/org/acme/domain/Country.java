package org.acme.domain;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.tivrfoa.mapresultset.api.Column;
import io.github.tivrfoa.mapresultset.api.Id;
import io.github.tivrfoa.mapresultset.api.ManyToMany;
import io.github.tivrfoa.mapresultset.api.OneToMany;
import io.github.tivrfoa.mapresultset.api.Table;

@Table (name = "country")
public record Country(@Id int id, float density, String name,
        double squareMeters, @Column (name = "phone_code") int phoneCode,
        long someBigNumber, BigInteger evenBigger,
        // It doesn't make sense for Country to have a list of Person ...
        // It's just for testing.
        @OneToMany (createWith = "newHashSet()", addWith = "add") Set<Person> listPerson,
        
        // Just for tests. This is actually a OneToMany
        @ManyToMany (createWith = "newLinkedList()", addWith = "add") List<State> states) {
    
        public static List<State> newLinkedList() {
                return new LinkedList<State>();
        }
    
        public static Set<Person> newHashSet() {
                return new HashSet<Person>();
        }
}
