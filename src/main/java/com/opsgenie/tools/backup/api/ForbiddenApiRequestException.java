package com.opsgenie.tools.backup.api;

/**
 * @author Mehmet Baris Kalkar
 * @version 1/17/17
 */
public class ForbiddenApiRequestException extends Exception {
    public ForbiddenApiRequestException(String s) {
        super(s);
    }
}
