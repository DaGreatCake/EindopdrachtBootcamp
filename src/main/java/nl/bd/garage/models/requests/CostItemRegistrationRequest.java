package nl.bd.garage.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.bd.garage.models.enums.CostType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CostItemRegistrationRequest {
    private String name;
    private double cost;
    private CostType costType;
}
