package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.api.TeamRoutingRuleApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.TeamConfig;

import java.util.ArrayList;
import java.util.List;

public class TeamImporter extends BaseImporter<TeamConfig> {

    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private List<TeamConfig> teamConfigs = new ArrayList<TeamConfig>();

    public TeamImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(TeamConfig teamConfig) {
        for (TeamConfig config : teamConfigs) {
            final Team currentTeam = config.getTeam();
            if (currentTeam.getId().equals(teamConfig.getTeam().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (currentTeam.getName().equals(teamConfig.getTeam().getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        teamConfigs = EntityListService.listTeams();
    }

    @Override
    protected TeamConfig getNewInstance() {
        return new TeamConfig();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teams";
    }

    @Override
    protected void createEntity(TeamConfig entity) throws ApiException {
        CreateTeamPayload payload = new CreateTeamPayload();
        final Team team = entity.getTeam();
        payload.setName(team.getName());

        if (BackupUtils.checkValidString(team.getDescription()))
            payload.setDescription(team.getDescription());

        payload.setMembers(team.getMembers());
        teamApi.createTeam(payload);
//        final List<TeamRoutingRule> teamRoutingRules = entity.getTeamRoutingRules();
//        if (teamRoutingRules != null) {
//            for (TeamRoutingRule teamRoutingRule : teamRoutingRules) {
//                createTeamRoutingRule(team, teamRoutingRule);
//            }
//        }
    }

    private void createTeamRoutingRule(Team team, TeamRoutingRule teamRoutingRule) throws ApiException {
        CreateTeamRoutingRulePayload payload = new CreateTeamRoutingRulePayload();
        payload.setCriteria(teamRoutingRule.getCriteria());
        payload.setName(teamRoutingRule.getName());
        payload.setNotify(teamRoutingRule.getNotify().id(null));
        payload.setOrder(teamRoutingRule.getOrder());
        payload.setTimeRestriction(teamRoutingRule.getTimeRestriction());
        payload.setTimezone(teamRoutingRule.getTimezone());

        CreateTeamRoutingRuleRequest request = new CreateTeamRoutingRuleRequest();
        request.setIdentifier(team.getId());
        request.setTeamIdentifierType(CreateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);

        teamRoutingRuleApi.createTeamRoutingRule(request);
    }

    @Override
    protected void updateEntity(TeamConfig entity, EntityStatus entityStatus) throws ApiException {
        UpdateTeamPayload payload = new UpdateTeamPayload();
        final Team team = entity.getTeam();
        payload.setName(team.getName());

        if (BackupUtils.checkValidString(team.getDescription())) {
            payload.setDescription(team.getDescription());
        }
        payload.setMembers(team.getMembers());
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            teamApi.updateTeam(team.getId(), payload);
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            teamApi.updateTeam(findTeamNameInCurrentEntities(entity), payload);
        }
//        final List<TeamRoutingRule> teamRoutingRules = entity.getTeamRoutingRules();
//        if (teamRoutingRules != null) {
//            for (TeamRoutingRule teamRoutingRule : teamRoutingRules) {
//                updateTeamRoutingRule(team, teamRoutingRule, entityStatus);
//            }
//        }
    }

    private String findTeamNameInCurrentEntities(TeamConfig entity) {
        for (TeamConfig teamConfig : teamConfigs) {
            if (entity.getTeam().getName().equals(teamConfig.getTeam().getName())) {
                return teamConfig.getTeam().getId();
            }
        }
        return null;
    }

    private void updateTeamRoutingRule(Team team, TeamRoutingRule teamRoutingRule, EntityStatus entityStatus) throws ApiException {
        UpdateTeamRoutingRulePayload payload = new UpdateTeamRoutingRulePayload();
        payload.setCriteria(teamRoutingRule.getCriteria());
        payload.setName(teamRoutingRule.getName());
        payload.setNotify(teamRoutingRule.getNotify());
        payload.setTimeRestriction(teamRoutingRule.getTimeRestriction());
        payload.setTimezone(teamRoutingRule.getTimezone());

        UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setId(teamRoutingRule.getId());
        if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setTeamIdentifierType(UpdateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
            request.setIdentifier(team.getName());
        } else if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(team.getId());
        }
        request.body(payload);
        teamRoutingRuleApi.updateTeamRoutingRule(request);
    }

    @Override
    protected String getEntityIdentifierName(TeamConfig entity) {
        return "Team " + entity.getTeam().getName();
    }
}
