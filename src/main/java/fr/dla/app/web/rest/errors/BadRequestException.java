package fr.dla.app.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.Status;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends DlappException {

    public BadRequestException(String msg, String entityName, String errorKey) {
        super(Status.BAD_REQUEST, ErrorConstants.DEFAULT_TYPE, msg, entityName, errorKey);
    }
}
