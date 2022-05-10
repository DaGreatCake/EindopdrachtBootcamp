package nl.bd.garage.models.exceptions;

public class IncorrectSyntaxException extends RuntimeException {
    public IncorrectSyntaxException(String field) {
        super("Incorrect syntax, field \"" + field + "\" was either empty or incorrect.\n" +
                "Refer to the manual for further instructions.");
    }
}
