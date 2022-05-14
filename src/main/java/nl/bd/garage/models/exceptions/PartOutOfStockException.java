package nl.bd.garage.models.exceptions;

public class PartOutOfStockException extends RuntimeException {
    public PartOutOfStockException(long id) {
        super("Part with id: " + id + " out of stock.");
    }
}
