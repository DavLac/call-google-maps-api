package fr.dla.app.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DlappException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private final String entityName;

    private final String errorKey;

    public DlappException(Status status, URI type, String defaultMessage, String entityName, String errorKey) {
        super(type, defaultMessage, status, null, null, null, getAlertParameters(defaultMessage));
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    private static Map<String, Object> getAlertParameters(String defaultMessage) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", defaultMessage);
        return parameters;
    }
}
