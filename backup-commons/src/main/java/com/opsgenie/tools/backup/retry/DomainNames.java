package com.opsgenie.tools.backup.retry;

public enum DomainNames {
    SEARCH("search"),
    CONFIGURATION("configuration");

    private final String type;

    DomainNames(String type) {
        this.type = type;
    }

    public String value() {
        return type;
    }
}

