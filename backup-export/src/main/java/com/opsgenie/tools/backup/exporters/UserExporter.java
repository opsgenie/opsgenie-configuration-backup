package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.UserConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.UserRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;

public class UserExporter extends BaseExporterWithRateLimiting<UserConfig> {
    public UserExporter(String backupRootDirectory, RateLimitManager rateLimitManager) {
        super(backupRootDirectory, "users",rateLimitManager);
    }

    @Override
    protected String getEntityFileName(UserConfig userConfig) {
        return userConfig.getUser().getUsername() + "-" + userConfig.getUser().getId();
    }

    @Override
    protected EntityRetriever<UserConfig> initializeEntityRetriever() {
        return new UserRetriever(rateLimitManager);
    }


}
