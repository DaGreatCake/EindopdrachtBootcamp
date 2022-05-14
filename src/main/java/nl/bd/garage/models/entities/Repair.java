package nl.bd.garage.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.bd.garage.models.enums.RepairStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne
    @JoinColumn(
            name = "fileId",
            referencedColumnName = "fileId"
    )
    private File file;

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

    // partsUsed is stored as a string so it can be stored in one row of a database table.
    public void setPartsUsed(List<Long> partsList) {
        String parts = "";

        for (int i = 0; i < partsList.size(); i++) {
            parts += partsList.get(i);

            if (i != partsList.size() - 1) {
                parts += ",";
            }
        }

        this.partsUsed = parts;
    }

    public List<Long> getPartsUsed() {
        if (partsUsed != null) {
            String[] partsSplit = this.partsUsed.split(",");
            List<Long> partsUsedList = new ArrayList<Long>();

            for (String s : partsSplit) {
                partsUsedList.add(Long.parseLong(s));
            }

            return partsUsedList;
        } else {
            return null;
        }
    }
}