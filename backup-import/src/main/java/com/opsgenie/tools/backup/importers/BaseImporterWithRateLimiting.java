package com.opsgenie.tools.backup.importers;

import com.opsgenie.tools.backup.retry.RateLimitManager;

abstract class BaseImporterWithRateLimiting<T> extends BaseImporter<T> {

    protected final RateLimitManager rateLimitManager;

    BaseImporterWithRateLimiting(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntityEnabled, boolean updateEntityEnabled) {
        super(backupRootDirectory, addEntityEnabled, updateEntityEnabled);
        this.rateLimitManager = rateLimitManager;
    }
}
