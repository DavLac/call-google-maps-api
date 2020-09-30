package fr.dla.app.web.rest.errors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class DlappExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DlappException.class)
    public ResponseEntity<DlappErrorResponse> handleDlappException(DlappException ex) {
        if (ex.getStatus() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DlappErrorResponse(ex.getMessage()));
        }

        return ResponseEntity.status(ex.getStatus().getStatusCode()).body(new DlappErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DlappErrorResponse> handleDlappException(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DlappErrorResponse(ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(new DlappErrorResponse(ex.getMessage()));
    }
}
