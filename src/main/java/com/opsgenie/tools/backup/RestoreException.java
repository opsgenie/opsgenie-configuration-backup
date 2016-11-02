package com.opsgenie.tools.backup;

/**
 * @author Mehmet Mustafa Demir
 */
public class RestoreException extends Exception {
    public RestoreException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
