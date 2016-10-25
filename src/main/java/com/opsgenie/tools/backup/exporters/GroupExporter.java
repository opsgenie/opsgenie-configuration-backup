package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Group;
import com.ifountain.opsgenie.client.model.group.ListGroupsRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Groups from Opsgenie account to local directory called groups
 *
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class GroupExporter extends BaseExporter<Group> {
    public GroupExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "groups");
    }

    @Override
    protected String getBeanFileName(Group bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Group> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListGroupsRequest request = new ListGroupsRequest();
        return getOpsGenieClient().group().listGroups(request).getGroups();
    }
}
