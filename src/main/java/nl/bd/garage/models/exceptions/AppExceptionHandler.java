package nl.bd.garage.models.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {AddStockToActionException.class, CostItemNotFoundException.class,
            CustomerDisagreedException.class, CustomerDisagreedException.class, CustomerNotFoundException.class,
            EmployeeNotFoundException.class, FileNotFoundException.class, IncorrectSyntaxException.class,
            ModifyAdminException.class, PartOutOfStockException.class, PreviousStepUncompletedException.class,
            RepairNotFoundException.class})
    public ResponseEntity<Object> handlePublicException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
