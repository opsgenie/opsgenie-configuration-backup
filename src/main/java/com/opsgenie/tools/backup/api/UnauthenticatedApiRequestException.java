package com.opsgenie.tools.backup.api;

/**
 * @author Mehmet Baris Kalkar
 * @version 1/16/17
 */
class UnauthenticatedApiRequestException extends Exception {
    public UnauthenticatedApiRequestException(String s) {
        super(s);
    }
}
