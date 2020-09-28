package fr.dla.app.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.Status;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends DlappException {

    public InternalServerErrorException(String msg, String entityName, String errorKey) {
        super(Status.INTERNAL_SERVER_ERROR, ErrorConstants.DEFAULT_TYPE, msg, entityName, errorKey);
    }
}
