package nl.bd.garage.models.exceptions;

public class RepairNotFoundException extends RuntimeException {
    public RepairNotFoundException(Long id) {
        super("Could not find repair with id: " + id.toString());
    }
}
