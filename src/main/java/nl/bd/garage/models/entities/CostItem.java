package nl.bd.garage.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.bd.garage.models.enums.CostType;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "costitems")
public class CostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long costItemId;

    @Column
    private String name;

    @Column
    private double cost;

    @Column
    private int stock;

    @Column
    private CostType costType;

    public CostItem(String name, double cost, CostType costType) {
        this.name = name;
        this.cost = cost;
        this.costType = costType;

        if (costType == CostType.PART) {
            this.stock = 0;
        } else {
            this.stock = -1;
        }
    }
}
