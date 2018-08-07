package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.api.TeamRoleApi;
import com.opsgenie.oas.sdk.api.TeamRoutingRuleApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.TeamRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TeamImporter extends BaseImporterWithRateLimiting<TeamConfig> {

    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private static TeamRoleApi teamRoleApi = new TeamRoleApi();

    public TeamImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, rateLimitManager, addEntity, updateEntity);
    }

    @Override
    protected EntityRetriever<TeamConfig> initializeEntityRetriever() {
        return new TeamRetriever(rateLimitManager);
    }

    @Override
    protected EntityStatus checkEntity(TeamConfig teamConfig) {
        for (TeamConfig config : currentConfigs) {
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
    protected TeamConfig getNewInstance() {
        return new TeamConfig();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teams";
    }

    @Override
    protected void createEntity(TeamConfig entity) throws Exception {
        throw new IllegalStateException("This should not happen because the team template importer should create all of the teams");

    }

    @Override
    protected void updateEntity(final TeamConfig entity, EntityStatus entityStatus) throws Exception {
        final SuccessResponse teamUpdateResponse;

        UpdateTeamPayload payload = new UpdateTeamPayload();
        final Team team = entity.getTeam();
        payload.setName(team.getName());

        if (BackupUtils.checkValidString(team.getDescription())) {
            payload.setDescription(team.getDescription());
        }
        payload.setMembers(new ArrayList<TeamMember>());

        final UpdateTeamRequest request = new UpdateTeamRequest().body(payload);

        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            teamUpdateResponse = RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
                @Override
                public SuccessResponse call() throws Exception {
                    return teamApi.updateTeam(request.identifier(team.getId()));
                }
            });

        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            teamUpdateResponse = RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
                @Override
                public SuccessResponse call() throws Exception {
                    return teamApi.updateTeam(request.identifier(findTeamIdInCurrentTeams(entity)));
                }
            });

        } else {
            logger.warn("Could not find team in current configuration. Update team skipped for " + team.getName());
            return;
        }

        final List<TeamRoutingRule> teamRoutingRules = entity.getTeamRoutingRules();
        if (teamRoutingRules != null) {
            for (TeamRoutingRule teamRoutingRule : teamRoutingRules) {

                if (teamRoutingRule.isIsDefault()) {
                    teamRoutingRule.setTimezone(null);
                }
                final String routingRuleIdInCurrent = findRoutingRuleIdInCurrent(entity, teamRoutingRule);
                if (routingRuleIdInCurrent != null) {
                    teamRoutingRule.setId(routingRuleIdInCurrent);
                    updateTeamRoutingRule(team, teamRoutingRule, entityStatus);
                } else {
                    createTeamRoutingRule(team, teamRoutingRule);
                }
            }
        }

        final List<TeamRole> teamRoles = entity.getTeamRoles();
        if (teamRoles != null) {
            for (TeamRole teamRole : teamRoles) {
                final String roleIdInCurrent = findRoleIdInCurrent(entity, teamRole);
                if (roleIdInCurrent != null) {
                    teamRole.setId(roleIdInCurrent);
                    updateTeamRole(team, teamRole, entityStatus);
                } else {
                    createTeamRole(team, teamRole);
                }
            }
        }

        // Set members after adding custom team roles
        UpdateTeamPayload updateTeamPayload = new UpdateTeamPayload();
        updateTeamPayload.setName(teamUpdateResponse.getData().getName());
        if (BackupUtils.checkValidString(team.getDescription())) {
            updateTeamPayload.setDescription(team.getDescription());
        }
        updateTeamPayload.setMembers(team.getMembers());
        final UpdateTeamRequest updateTeamRequest = new UpdateTeamRequest().body(updateTeamPayload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamApi.updateTeam(updateTeamRequest.identifier(teamUpdateResponse.getData().getId()));
            }
        });

    }

    private void createTeamRoutingRule(Team team, TeamRoutingRule teamRoutingRule) throws Exception {
        CreateTeamRoutingRulePayload payload = new CreateTeamRoutingRulePayload();
        payload.setCriteria(teamRoutingRule.getCriteria());
        payload.setName(teamRoutingRule.getName());
        payload.setNotify(teamRoutingRule.getNotify().id(null));
        payload.setOrder(teamRoutingRule.getOrder());
        payload.setTimeRestriction(teamRoutingRule.getTimeRestriction());
        payload.setTimezone(teamRoutingRule.getTimezone());

        final CreateTeamRoutingRuleRequest request = new CreateTeamRoutingRuleRequest();
        request.setIdentifier(team.getName());
        request.setTeamIdentifierType(CreateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);

        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamRoutingRuleApi.createTeamRoutingRule(request);
            }
        });

    }

    private void createTeamRole(Team team, TeamRole teamRole) throws Exception {
        CreateTeamRolePayload payload = new CreateTeamRolePayload();
        payload.setName(teamRole.getName());
        payload.setRights(teamRole.getRights());

        final AddTeamRoleRequest request = new AddTeamRoleRequest();
        request.setIdentifier(team.getName());
        request.setTeamIdentifierType(AddTeamRoleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamRoleApi.createTeamRole(request);
            }
        });
    }


    private String findTeamIdInCurrentTeams(TeamConfig entity) {
        for (TeamConfig teamConfig : currentConfigs) {
            if (entity.getTeam().getName().equals(teamConfig.getTeam().getName())) {
                return teamConfig.getTeam().getId();
            }
        }
        return null;
    }

    private String findRoutingRuleIdInCurrent(TeamConfig entity, TeamRoutingRule teamRoutingRule) {
        for (TeamConfig teamConfig : currentConfigs) {
            if (entity.getTeam().getName().equals(teamConfig.getTeam().getName())) {
                for (TeamRoutingRule currentRoutingRule : teamConfig.getTeamRoutingRules()) {
                    if (currentRoutingRule.getName().equals(teamRoutingRule.getName())) {
                        return currentRoutingRule.getId();
                    }
                }
            }
        }
        return null;
    }

    private String findRoleIdInCurrent(TeamConfig entity, TeamRole teamRole) {
        for (TeamConfig teamConfig : currentConfigs) {
            if (entity.getTeam().getName().equals(teamConfig.getTeam().getName())) {
                for (TeamRole currentRole : teamConfig.getTeamRoles()) {
                    if (currentRole.getName().equals(teamRole.getName())) {
                        return currentRole.getId();
                    }
                }
            }
        }
        return null;
    }

    private void updateTeamRoutingRule(Team team, TeamRoutingRule teamRoutingRule, EntityStatus entityStatus) throws Exception {
        UpdateTeamRoutingRulePayload payload = new UpdateTeamRoutingRulePayload();
        payload.setCriteria(teamRoutingRule.getCriteria());
        payload.setName(teamRoutingRule.getName());
        payload.setNotify(teamRoutingRule.getNotify());
        payload.setTimeRestriction(teamRoutingRule.getTimeRestriction());
        payload.setTimezone(teamRoutingRule.getTimezone());

        final UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setId(teamRoutingRule.getId());
        if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setTeamIdentifierType(UpdateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
            request.setIdentifier(team.getName());
        } else if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(team.getId());
        }
        request.body(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamRoutingRuleApi.updateTeamRoutingRule(request);
            }
        });

    }

    private void updateTeamRole(Team team, TeamRole teamRole, EntityStatus entityStatus) throws Exception {
        UpdateTeamRolePayload payload = new UpdateTeamRolePayload();
        payload.setName(teamRole.getName());
        payload.setRights(teamRole.getRights());
 
        final UpdateTeamRoleRequest request = new UpdateTeamRoleRequest();
        request.setTeamRoleIdentifier(teamRole.getId());
        request.setIdentifierType(UpdateTeamRoleRequest.IdentifierTypeEnum.ID);
        if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setTeamIdentifierType(UpdateTeamRoleRequest.TeamIdentifierTypeEnum.NAME);
            request.setIdentifier(team.getName());
        } else if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(team.getId());
        }
        request.body(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamRoleApi.updateTeamRole(request);
            }
        });

    }

    @Override
    protected String getEntityIdentifierName(TeamConfig entity) {
        return "Team " + entity.getTeam().getName();
    }

    @Override
    protected void updateTeamIds(TeamConfig entity) {
        oldTeamIdMap.put(entity.getTeam().getId(), entity.getTeam().getName());
    }
}
