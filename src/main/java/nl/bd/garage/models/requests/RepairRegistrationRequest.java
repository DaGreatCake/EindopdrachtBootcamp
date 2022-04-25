package nl.bd.garage.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepairRegistrationRequest {
    private long customerId;
    private java.sql.Date date;
}
