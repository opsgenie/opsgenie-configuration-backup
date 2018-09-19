package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.TeamRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TeamTemplateImporter extends BaseImporterWithRateLimiting<TeamConfig> {

    private static TeamApi teamApi = new TeamApi();

    public TeamTemplateImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntity) {
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
        final CreateTeamPayload payload = new CreateTeamPayload();
        final Team team = entity.getTeam();
        payload.setName(team.getName());

        if (BackupUtils.checkValidString(team.getDescription()))
            payload.setDescription(team.getDescription());

        payload.setMembers(new ArrayList<TeamMember>());
        final SuccessResponse teamCreateResult = RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return teamApi.createTeam(payload);
            }
        });
    }

    @Override
    protected void updateEntity(final TeamConfig entity, EntityStatus entityStatus) throws Exception {}

    @Override
    protected String getEntityIdentifierName(TeamConfig entity) {
        return "Team " + entity.getTeam().getName();
    }

    @Override
    protected void updateTeamIds(TeamConfig entity) {}



}
