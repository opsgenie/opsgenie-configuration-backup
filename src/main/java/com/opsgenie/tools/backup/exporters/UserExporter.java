package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.ListUsersRequest;
import com.opsgenie.client.model.User;

import java.util.List;

public class UserExporter extends BaseExporter<User> {

    private static UserApi userApi = new UserApi();

    public UserExporter(String backupRootDirectory) {
        super(backupRootDirectory, "users");
    }

    @Override
    protected String getBeanFileName(User bean) {
        return bean.getUsername() + "-" + bean.getId();
    }


    @Override
    protected List<User> retrieveEntities() throws ApiException {
        return userApi.listUsers(new ListUsersRequest()).getData();
    }
}
