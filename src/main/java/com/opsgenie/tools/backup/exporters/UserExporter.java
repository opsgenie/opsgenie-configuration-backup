package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.User;
import com.ifountain.opsgenie.client.model.user.ListUsersRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class UserExporter extends BaseExporter<User> {
    public UserExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "users");
    }

    @Override
    protected String getBeanFileName(User bean) {
        return bean.getUsername() + "-" + bean.getId();
    }


    @Override
    protected List<User> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListUsersRequest request = new ListUsersRequest();
        return getOpsGenieClient().user().listUsers(request).getUsers();
    }
}
