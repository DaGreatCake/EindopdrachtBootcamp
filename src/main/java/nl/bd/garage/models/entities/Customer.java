package nl.bd.garage.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private Set<Repair> repairs = new HashSet<>();

    @Column
    private String name;

    @Column
    private String telephoneNumber;

    @Column
    private String licensePlate;

    public Customer(String name, String telephoneNumber, String licensePlate) {
        this.name = name;
        this.telephoneNumber = telephoneNumber;
        this.licensePlate = licensePlate;
    }
}
