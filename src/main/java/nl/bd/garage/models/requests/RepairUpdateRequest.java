package nl.bd.garage.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepairUpdateRequest {
    private java.sql.Date examinationDate;
    private String foundProblems;
    private java.sql.Date repairDate;
    private Boolean customerAgreed;
    private String partsUsed;
    private double otherActionsPrice;
    private Boolean completed;
    private Boolean paid;
}
