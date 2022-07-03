package org.acme.domain;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import com.github.tivrfoa.mapresultset.api.Column;
import com.github.tivrfoa.mapresultset.api.Id;
import com.github.tivrfoa.mapresultset.api.ManyToMany;
import com.github.tivrfoa.mapresultset.api.OneToMany;
import com.github.tivrfoa.mapresultset.api.Table;

@Table (name = "country")
public record Country(@Id int id, float density, String name,
        double squareMeters, @Column (name = "phone_code") int phoneCode,
        long someBigNumber, BigInteger evenBigger,
        // It doesn't make sense for Country to have a list of Person ...
        // It's just for testing.
        @OneToMany (createWith = "newLinkedList()", addWith = "add") List<Person> listPerson,
        
        // Just for tests. This is actually a OneToMany
        @ManyToMany List<State> states) {
    
        public static List<Person> newLinkedList() {
                return new LinkedList<Person>();
        }
}
