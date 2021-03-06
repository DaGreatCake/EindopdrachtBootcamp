package nl.bd.garage.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepairSetPartsRequest {
    private List<Long> partsUsed;
    private double otherActionsPrice;
}
