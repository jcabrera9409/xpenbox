package org.xpenbox.exception;

public class UnprocessableContentException extends BussinessException {
    public static final Integer UNPROCESSABLE_ENTITY_STATUS_CODE = 422;

    public UnprocessableContentException(String message) {
        super(message, UNPROCESSABLE_ENTITY_STATUS_CODE);
    }

    public UnprocessableContentException(String message, Throwable cause) {
        super(message, cause, UNPROCESSABLE_ENTITY_STATUS_CODE);
    }   
}
