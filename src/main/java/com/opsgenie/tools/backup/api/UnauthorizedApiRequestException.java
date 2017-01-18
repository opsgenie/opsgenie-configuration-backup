package com.opsgenie.tools.backup.api;

/**
 * @author Mehmet Baris Kalkar
 * @version 1/17/17
 */
public class UnauthorizedApiRequestException extends Exception {
    public UnauthorizedApiRequestException(String s) {
        super(s);
    }
}
