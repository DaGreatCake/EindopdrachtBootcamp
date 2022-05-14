package nl.bd.garage.models.exceptions;

public class AddStockToActionException extends RuntimeException {
    public AddStockToActionException(Long id)  {
        super("Cannot add stock to cost item with id: " + id + ". Cost item type is ACTION.");
    }
}
