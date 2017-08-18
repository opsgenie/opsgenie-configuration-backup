package com.opsgenie.tools.backup.exporters;


import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.api.TeamRoutingRuleApi;
import com.opsgenie.client.model.ListTeamRoutingRulesRequest;
import com.opsgenie.client.model.Team;
import com.opsgenie.client.model.TeamRoutingRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;


public class TeamRoutingRuleExporter extends BaseExporter<TeamRoutingRule> {
    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private static String teamId;

    public TeamRoutingRuleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "teamRoutingRules");
    }

    @Override
    protected String getBeanFileName(TeamRoutingRule bean) {
        return bean.getName() + "-" + bean.getId();
    }

    @Override
    public void export() {
        try {
            List<Team> currentTeamList = teamApi.listTeams(Collections.<String>emptyList()).getData();
            for (Team team : currentTeamList) {
                try {
                    teamId = team.getId();
                    List<TeamRoutingRule> teamRoutingRules = retrieveEntities();
                    if (teamRoutingRules != null && teamRoutingRules.size() > 0) {
                        File teamFile = new File(getExportDirectory().getAbsolutePath() + "/" + team.getName());
                        teamFile.mkdirs();
                        for (TeamRoutingRule bean : teamRoutingRules) {
                            exportFile(getExportDirectory().getAbsolutePath() + "/" + team.getName() + "/" + getBeanFileName(bean) + ".json", bean);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error at Listing team routing rules for team " + team.getName(), e);
                }

            }
        } catch (Exception e) {
            logger.error("Error at Listing teams for team routing rules", e);
        }

    }


    @Override
    protected List<TeamRoutingRule> retrieveEntities() throws ApiException {
        return teamRoutingRuleApi.listTeamRoutingRules(new ListTeamRoutingRulesRequest().identifier(teamId)).getData();
    }
}
