package com.opsgenie.tools.backup.api;

/**
 * @author Mehmet Baris Kalkar
 * @version 1/16/17
 */
class MaxRetryCountExceededException extends Exception {
    public MaxRetryCountExceededException(String s) {
        super(s);
    }
}

