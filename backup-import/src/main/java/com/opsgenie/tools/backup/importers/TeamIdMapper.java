package com.opsgenie.tools.backup.importers;

import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.retrieval.TeamRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Muhammad Usman
 * @version 07.08.2018
 */

public class TeamIdMapper {
    private final TeamRetriever teamRetriever;
    private static List<TeamConfig> teamConfigs;

    TeamIdMapper(RateLimitManager rateLimitManager) {
        teamRetriever = new TeamRetriever(rateLimitManager);
    }

    Map<String, String> getTeamIdMap() throws Exception {
        List<TeamConfig> teamsFromApi = getTeamsList();
        Map<String, String> newTeamIdMap = new HashMap<String, String>();

        for (TeamConfig teamConfig : teamsFromApi) {
            newTeamIdMap.put(teamConfig.getTeam().getName(), teamConfig.getTeam().getId());
        }
        return newTeamIdMap;
    }

    private List<TeamConfig> getTeamsList() throws Exception {
        if (teamConfigs == null) {
            teamConfigs = teamRetriever.retrieveEntities();
        }
        return teamConfigs;
    }


}
