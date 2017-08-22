package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.api.TeamRoutingRuleApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.File;

public class TeamRoutingRuleImporter extends BaseImporter<TeamRoutingRule> {

    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private Team teamInProcess;

    public TeamRoutingRuleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected TeamRoutingRule getNewInstance() {
        return new TeamRoutingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teamRoutingRules";
    }

    @Override
    public void restore() {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.warn("Warning: " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipeed");
            return;
        }

        File[] fileList = getImportDirectory().listFiles();
        if (fileList == null || fileList.length == 0) {
            logger.warn("Warning: " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        for (File teamDirectory : fileList) {
            Team team = findTeam(teamDirectory);
            if (team != null) {
                importRoutingRulesForTeam(team, teamDirectory);
            }
        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    @Override
    protected TeamRoutingRule checkEntityWithName(TeamRoutingRule entity) throws ApiException {
        return teamRoutingRuleApi.getTeamRoutingRule(new GetTeamRoutingRuleRequest()
                .teamIdentifierType(GetTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME)
                .identifier(teamInProcess.getName()).id(entity.getId())).getData();
    }

    @Override
    protected TeamRoutingRule checkEntityWithId(TeamRoutingRule entity) throws ApiException {
        return teamRoutingRuleApi.getTeamRoutingRule(new GetTeamRoutingRuleRequest()
                .teamIdentifierType(GetTeamRoutingRuleRequest.TeamIdentifierTypeEnum.ID)
                .identifier(teamInProcess.getId()).id(entity.getId())).getData();
    }

    private Team findTeam(File teamDirectory) throws ApiException {
        if (!teamDirectory.exists() || !teamDirectory.isDirectory()) {
            return null;
        }
        return teamApi.getTeam(new GetTeamRequest().identifierType(GetTeamRequest.IdentifierTypeEnum.NAME).identifier(teamDirectory.getName())).getData();
    }

    private void importRoutingRulesForTeam(Team team, File teamDirectory) throws ApiException {
        teamInProcess = team;

        String[] files = BackupUtils.getFileListOf(teamDirectory);

        for (String fileName : files) {
            TeamRoutingRule teamRoutingRule = readEntity(teamDirectory.getName() + "/" + fileName);
            if (teamRoutingRule != null) {
                importEntity(teamRoutingRule);
            }
        }
    }

    @Override
    protected void createEntity(TeamRoutingRule entity) throws ApiException {

        CreateTeamRoutingRulePayload payload = new CreateTeamRoutingRulePayload();
        payload.setCriteria(entity.getCriteria());
        payload.setName(entity.getName());
        payload.setNotify(entity.getNotify().id(null));
        payload.setOrder(entity.getOrder());
        payload.setTimeRestriction(entity.getTimeRestriction());
        payload.setTimezone(entity.getTimezone());

        CreateTeamRoutingRuleRequest request = new CreateTeamRoutingRuleRequest();
        request.setIdentifier(teamInProcess.getId());
        request.setTeamIdentifierType(CreateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);

        teamRoutingRuleApi.createTeamRoutingRule(request);
    }

    @Override
    protected void updateEntity(TeamRoutingRule entity, EntityStatus entityStatus) throws ApiException {
        UpdateTeamRoutingRulePayload payload = new UpdateTeamRoutingRulePayload();
        payload.setCriteria(entity.getCriteria());
        payload.setName(entity.getName());
        payload.setNotify(entity.getNotify());
        payload.setTimeRestriction(entity.getTimeRestriction());
        payload.setTimezone(entity.getTimezone());

        UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setId(entity.getId());
        request.setIdentifier(teamInProcess.getId());
        request.setTeamIdentifierType(UpdateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.body(payload);

        teamRoutingRuleApi.updateTeamRoutingRule(request);
    }

    @Override
    protected String getEntityIdentifierName(TeamRoutingRule teamRoutingRule) {
        return "Team Routing rule  " + teamRoutingRule.getName() + " for team " + teamName;
    }
}
