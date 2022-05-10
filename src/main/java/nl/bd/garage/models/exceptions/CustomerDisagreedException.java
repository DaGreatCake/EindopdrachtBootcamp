package nl.bd.garage.models.exceptions;

public class CustomerDisagreedException extends RuntimeException {
    public CustomerDisagreedException() {
        super("Unable to perform operation. Customer disagreed to the repair.");
    }
}
