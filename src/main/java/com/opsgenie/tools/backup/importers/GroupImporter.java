package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Group;
import com.ifountain.opsgenie.client.model.group.AddGroupRequest;
import com.ifountain.opsgenie.client.model.group.ListGroupsRequest;
import com.ifountain.opsgenie.client.model.group.UpdateGroupRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports Groups from local directory called groups to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class GroupImporter extends BaseImporter<Group> {
    public GroupImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected int checkEntities(Group oldEntity, Group currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        } else if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        }
        return -1;
    }

    @Override
    protected Group getBean() throws IOException, ParseException {
        return new Group();
    }

    @Override
    protected String getImportDirectoryName() {
        return "groups";
    }

    @Override
    protected void addBean(Group bean) throws ParseException, OpsGenieClientException, IOException {
        AddGroupRequest request = new AddGroupRequest();
        request.setName(bean.getName());
        request.setUsers(bean.getUsers());
        getOpsGenieClient().group().addGroup(request);
    }

    @Override
    protected void updateBean(Group bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateGroupRequest request = new UpdateGroupRequest();
        request.setId(bean.getId());
        request.setName(bean.getName());
        request.setUsers(bean.getUsers());
        getOpsGenieClient().group().updateGroup(request);

    }

    @Override
    protected List<Group> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListGroupsRequest listGroupsRequest = new ListGroupsRequest();
        return getOpsGenieClient().group().listGroups(listGroupsRequest).getGroups();
    }

    @Override
    protected String getEntityIdentifierName(Group entitiy) {
        return "Group " + entitiy.getName();
    }
}
