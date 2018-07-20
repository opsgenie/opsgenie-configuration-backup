package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.UserConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.UserRetriever;

public class UserExporter extends BaseExporter<UserConfig> {
    public UserExporter(String backupRootDirectory) {
        super(backupRootDirectory, "users");
    }

    @Override
    protected String getEntityFileName(UserConfig userConfig) {
        return userConfig.getUser().getUsername() + "-" + userConfig.getUser().getId();
    }

    @Override
    protected EntityRetriever<UserConfig> initializeEntityRetriever() {
        return new UserRetriever(getApiLimits());
    }


}
