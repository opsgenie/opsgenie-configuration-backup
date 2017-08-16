package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.util.Collections;
import java.util.List;

public class TeamImporter extends BaseImporter<Team> {

    private static TeamApi api = new TeamApi();

    public TeamImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(Team oldEntity, Team currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected Team getBean() {
        return new Team();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teams";
    }

    @Override
    protected void addBean(Team bean) throws ApiException {
        CreateTeamPayload payload = new CreateTeamPayload();
        payload.setName(bean.getName());

        if (BackupUtils.checkValidString(bean.getDescription()))
            payload.setDescription(bean.getDescription());

        payload.setMembers(bean.getMembers());
        api.createTeam(payload);
    }

    @Override
    protected void updateBean(Team bean) throws ApiException {
        UpdateTeamPayload payload = new UpdateTeamPayload();
        payload.setName(bean.getName());

        if (BackupUtils.checkValidString(bean.getDescription()))
            payload.setDescription(bean.getDescription());

        payload.setMembers(bean.getMembers());

        api.updateTeam(bean.getId(), payload);
    }

    @Override
    protected List<Team> retrieveEntities() throws ApiException {
        return api.listTeams(Collections.singletonList("member")).getData();
    }

    @Override
    protected String getEntityIdentifierName(Team entitiy) {
        return "Team " + entitiy.getName();
    }
}
