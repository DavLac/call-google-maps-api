package fr.dla.app.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.Status;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends DlappException {

    public NotFoundException(String msg, String entityName, String errorKey) {
        super(Status.NOT_FOUND, ErrorConstants.ENTITY_NOT_FOUND_TYPE, msg, entityName, errorKey);
    }
}
