package nl.bd.garage.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column
    private String name;

    @Column
    private String telephoneNumber;

    @Column
    private String licensePlate;

    public Customer() {

    }

    public Customer(String name, String telephoneNumber, String licensePlate) {
        this.name = name;
        this.telephoneNumber = telephoneNumber;
        this.licensePlate = licensePlate;
    }
}
