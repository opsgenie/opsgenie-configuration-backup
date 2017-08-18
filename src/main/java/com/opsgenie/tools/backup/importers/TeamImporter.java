package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

public class TeamImporter extends BaseImporter<Team> {

    private static TeamApi api = new TeamApi();

    public TeamImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected void getEntityWithId(Team team) throws ApiException {
        GetTeamResponse team1 = api.getTeam(new GetTeamRequest().identifierType(GetTeamRequest.IdentifierTypeEnum.NAME).identifier(team.getName()));
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
    protected String getEntityIdentifierName(Team entity) {
        return "Team " + entity.getName();
    }
}
