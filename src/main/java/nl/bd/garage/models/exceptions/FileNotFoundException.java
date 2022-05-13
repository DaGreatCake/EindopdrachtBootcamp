package nl.bd.garage.models.exceptions;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(Long id) {
        super("No file uploaded in repair with id: " + id);
    }
}
