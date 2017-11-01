package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.TeamRetriever;

public class TeamExporter extends BaseExporter<TeamConfig> {

    public TeamExporter(String backupRootDirectory) {
        super(backupRootDirectory, "teams");
    }

    @Override
    protected EntityRetriever<TeamConfig> initializeEntityRetriever() {
        return new TeamRetriever();
    }

    @Override
    protected String getEntityFileName(TeamConfig team) {
        return team.getTeam().getName() + "-" + team.getTeam().getId();
    }

}
