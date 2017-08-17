package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.GetUserRequest;
import com.opsgenie.client.model.ListUsersRequest;
import com.opsgenie.client.model.User;

import java.util.ArrayList;
import java.util.Collections;
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
        List<User> userList = userApi.listUsers(new ListUsersRequest()).getData();
        List<User> usersWithContact = new ArrayList<User>();
        for(User user:userList){
            usersWithContact.add(userApi.getUser(new GetUserRequest().identifier(user.getId()).expand(Collections.singletonList("contact"))).getData());
        }
        return usersWithContact;
    }
}
