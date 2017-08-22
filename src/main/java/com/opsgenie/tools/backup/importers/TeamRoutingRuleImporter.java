package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.api.TeamRoutingRuleApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import java.io.File;
import java.util.List;

public class TeamRoutingRuleImporter extends BaseImporter<TeamRoutingRule> {

    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private String teamName;

    public TeamRoutingRuleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected TeamRoutingRule getBean() {
        return new TeamRoutingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teamRoutingRules";
    }

    public void restore() throws RestoreException, ApiException {
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
    protected void getEntityWithId(TeamRoutingRule entity) throws ApiException {
        teamRoutingRuleApi.getTeamRoutingRule(new GetTeamRoutingRuleRequest()
                .teamIdentifierType(GetTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME)
                .identifier(teamName).id(entity.getId()));
    }

    private Team findTeam(File teamDirectory) throws ApiException {
        if (!teamDirectory.exists() || !teamDirectory.isDirectory()) {
            return null;
        }
        return teamApi.getTeam(new GetTeamRequest().identifierType(GetTeamRequest.IdentifierTypeEnum.NAME).identifier(teamDirectory.getName())).getData();
    }

    private void importRoutingRulesForTeam(Team team, File teamDirectory) throws ApiException {
        teamName = team.getName();
        String[] files = BackupUtils.getFileListOf(teamDirectory);

        List<TeamRoutingRule> teamRoutingRules = this.listRoutingRulesWithoutId();

        for (String fileName : files) {
            TeamRoutingRule entity = readEntity(teamDirectory.getName() + "/" + fileName);
            TeamRoutingRule cloneEntity = readEntity(teamDirectory.getName() + "/" + fileName);
            unsetIds(cloneEntity);

            if (entity != null && !teamRoutingRules.contains(cloneEntity)) {
                importEntity(entity);
            }
        }
    }

    private List<TeamRoutingRule> listRoutingRulesWithoutId() throws ApiException {
        ListTeamRoutingRulesRequest request = new ListTeamRoutingRulesRequest()
                .identifier(teamName)
                .teamIdentifierType(ListTeamRoutingRulesRequest.TeamIdentifierTypeEnum.NAME);
        ListTeamRoutingRulesResponse response = teamRoutingRuleApi.listTeamRoutingRules(request);

        for (TeamRoutingRule routingRule : response.getData()) {
            unsetIds(routingRule);
        }

        return response.getData();
    }

    private void unsetIds(TeamRoutingRule routingRule) {
        routingRule.setId(null);
        routingRule.getNotify().setId(null);
        routingRule.setIsDefault(false);
    }

    @Override
    protected void addBean(TeamRoutingRule bean) throws ApiException {

        CreateTeamRoutingRulePayload payload = new CreateTeamRoutingRulePayload();
        payload.setCriteria(bean.getCriteria());
        payload.setName(bean.getName());
        payload.setNotify(bean.getNotify().id(null));
        payload.setOrder(bean.getOrder());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setTimezone(bean.getTimezone());

        CreateTeamRoutingRuleRequest request = new CreateTeamRoutingRuleRequest();
        request.setIdentifier(teamName);
        request.setTeamIdentifierType(CreateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);

        teamRoutingRuleApi.createTeamRoutingRule(request);
    }

    @Override
    protected void updateBean(TeamRoutingRule bean) throws ApiException {
        UpdateTeamRoutingRulePayload payload = new UpdateTeamRoutingRulePayload();
        payload.setCriteria(bean.getCriteria());
        payload.setName(bean.getName());
        payload.setNotify(bean.getNotify());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setTimezone(bean.getTimezone());

        UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setId(bean.getId());
        request.setIdentifier(teamName);
        request.setTeamIdentifierType(UpdateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.body(payload);

        teamRoutingRuleApi.updateTeamRoutingRule(request);
    }

    @Override
    protected String getEntityIdentifierName(TeamRoutingRule bean) {
        return "Team Routing rule  " + bean.getName() + " for team " + teamName;
    }
}
