package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.TeamConfig;

import java.util.List;

public class TeamExporter extends BaseExporter<TeamConfig> {

    public TeamExporter(String backupRootDirectory) {
        super(backupRootDirectory, "teams");
    }

    @Override
    protected String getEntityFileName(TeamConfig team) {
        return team.getTeam().getName() + "-" + team.getTeam().getId();
    }

    @Override
    protected List<TeamConfig> retrieveEntities() throws ApiException {
        return EntityListService.listTeams();
    }
}
