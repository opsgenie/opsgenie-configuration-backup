package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.retry.RateLimitManager;

abstract class BaseExporterWithRateLimiting<T> extends BaseExporter<T> {

    protected final RateLimitManager rateLimitManager;

    BaseExporterWithRateLimiting(String backupRootDirectory, String exportDirectoryName, RateLimitManager rateLimitManager) {
        super(backupRootDirectory, exportDirectoryName);
        this.rateLimitManager = rateLimitManager;
    }
}
