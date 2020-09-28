package fr.dla.app.web.rest.errors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DlappErrorResponse {
    private String error;

    public DlappErrorResponse(String error) {
        this.error = error;
    }
}
