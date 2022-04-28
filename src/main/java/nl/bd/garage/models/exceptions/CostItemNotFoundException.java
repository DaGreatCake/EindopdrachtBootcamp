package nl.bd.garage.models.exceptions;

public class CostItemNotFoundException extends RuntimeException {
    public CostItemNotFoundException(Long id) {
        super("Could not find CostItem with id: " + id.toString());
    }
}
