package nl.bd.garage.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.bd.garage.models.enums.RepairStatus;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "repairs")
public class Repair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repairId;

    @ManyToOne
    @JoinColumn(
            name = "customerId",
            referencedColumnName = "customerId")
    private Customer customer;

    @Column
    private java.sql.Date examinationDate;

    @Column
    private String foundProblems;

    @Column
    private java.sql.Date repairDate;

    @Column
    private Boolean customerAgreed;

    @Column
    private String partsUsed;

    @Column
    private double otherActionsPrice;

    @Column
    private RepairStatus completed;

    @Column
    private Boolean called;

    @Column
    private Boolean paid;

    public Repair(Customer customer, java.sql.Date examinationDate) {
        this.customer = customer;
        this.examinationDate = examinationDate;
        this.customerAgreed = null;
        this.completed = RepairStatus.UNCOMPLETED;
        this.called = false;
        this.paid = false;
    }
}