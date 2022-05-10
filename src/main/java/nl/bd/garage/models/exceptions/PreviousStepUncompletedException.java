package nl.bd.garage.models.exceptions;

public class PreviousStepUncompletedException extends RuntimeException {
    public PreviousStepUncompletedException(String step) {
        super("Unable to perform operation. Previous step: \"" + step + "\" not yet completed");
    }
}
