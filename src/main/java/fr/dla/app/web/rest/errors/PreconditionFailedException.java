package fr.dla.app.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.Status;

@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED)
public class PreconditionFailedException extends DlappException {

    public PreconditionFailedException(String defaultMessage, String entityName, String errorKey) {
        super(Status.PRECONDITION_FAILED, ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey);
    }
}
