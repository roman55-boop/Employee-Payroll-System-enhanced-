package com.payroll.exception;

/**
 * Custom exception class for handling payroll-related errors.
 * Extends Exception to provide checked exception behavior.
 * 
 * Demonstrates: Custom Exception, Exception Handling
 */
public class PayrollException extends Exception {

    /**
     * Constructor with only an error message.
     * @param message Description of the error
     */
    public PayrollException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and a root cause.
     * Useful for wrapping lower-level exceptions.
     * @param message Description of the error
     * @param cause   The underlying exception that caused this one
     */
    public PayrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
