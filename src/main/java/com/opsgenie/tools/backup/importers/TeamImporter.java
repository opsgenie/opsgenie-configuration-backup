package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.model.CreateTeamPayload;
import com.opsgenie.client.model.GetTeamRequest;
import com.opsgenie.client.model.Team;
import com.opsgenie.client.model.UpdateTeamPayload;
import com.opsgenie.tools.backup.BackupUtils;

public class TeamImporter extends BaseImporter<Team> {

    private static TeamApi api = new TeamApi();

    public TeamImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected Team checkEntityWithName(Team team) throws ApiException {
        final GetTeamRequest getTeamRequest = new GetTeamRequest()
                .identifierType(GetTeamRequest.IdentifierTypeEnum.NAME)
                .identifier(team.getName());
        return api.getTeam(getTeamRequest).getData();
    }

    @Override
    protected Team checkEntityWithId(Team team) throws ApiException {
        final GetTeamRequest getTeamRequest = new GetTeamRequest()
                .identifier(team.getId());
        return api.getTeam(getTeamRequest).getData();
    }

    @Override
    protected Team getNewInstance() {
        return new Team();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teams";
    }

    @Override
    protected void createEntity(Team entity) throws ApiException {
        CreateTeamPayload payload = new CreateTeamPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription()))
            payload.setDescription(entity.getDescription());

        payload.setMembers(entity.getMembers());
        api.createTeam(payload);
    }

    @Override
    protected void updateEntity(Team entity, EntityStatus entityStatus) throws ApiException {
        UpdateTeamPayload payload = new UpdateTeamPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription()))
            payload.setDescription(entity.getDescription());

        payload.setMembers(entity.getMembers());

        api.updateTeam(entity.getId(), payload);
    }

    @Override
    protected String getEntityIdentifierName(Team entity) {
        return "Team " + entity.getName();
    }
}
