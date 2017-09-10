package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.UserConfig;

import java.util.List;

public class UserExporter extends BaseExporter<UserConfig> {
    public UserExporter(String backupRootDirectory) {
        super(backupRootDirectory, "users");
    }

    @Override
    protected String getEntityFileName(UserConfig userConfig) {
        return userConfig.getUser().getUsername() + "-" + userConfig.getUser().getId();
    }

    @Override
    protected List<UserConfig> retrieveEntities() throws ApiException {
        return EntityListService.listUserConfigs();
    }
}
