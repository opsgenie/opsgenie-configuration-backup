package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.TeamRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;

public class TeamExporter extends BaseExporterWithRateLimiting<TeamConfig> {

    public TeamExporter(String backupRootDirectory, RateLimitManager rateLimitManager) {
        super(backupRootDirectory, "teams",rateLimitManager);
    }

    @Override
    protected EntityRetriever<TeamConfig> initializeEntityRetriever() {
        return new TeamRetriever(rateLimitManager);
    }

    @Override
    protected String getEntityFileName(TeamConfig team) {
        return team.getTeam().getName() + "-" + team.getTeam().getId();
    }

}
