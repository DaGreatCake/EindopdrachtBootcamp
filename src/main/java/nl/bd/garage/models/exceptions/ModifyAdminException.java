package nl.bd.garage.models.exceptions;

public class ModifyAdminException extends RuntimeException {
    public ModifyAdminException(String action) {
        super("Unable to " + action + " employee with role ADMIN");
    }
}
