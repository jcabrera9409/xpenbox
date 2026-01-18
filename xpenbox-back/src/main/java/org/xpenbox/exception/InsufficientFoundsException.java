package org.xpenbox.exception;

/**
 * Exception thrown when there are insufficient funds in an account to complete a transaction.
 * Maps to HTTP 400 Bad Request status.
 */
public class InsufficientFoundsException extends BussinessException {
    public InsufficientFoundsException(String message) {
        super(message);
    }
}
