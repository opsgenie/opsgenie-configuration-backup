package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Team;
import com.ifountain.opsgenie.client.model.beans.TeamRoutingRule;
import com.ifountain.opsgenie.client.model.team.ListTeamsRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.ListTeamRoutingRulesRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Team Routing rules from Opsgenie account to local directory called
 * teamRoutingRules
 *
 * @author Mehmet Mustafa Demir
 */
public class TeamRoutingRuleExporter extends BaseExporter<TeamRoutingRule> {
    private final Logger logger = LogManager.getLogger(TeamRoutingRuleExporter.class);
    private ListTeamRoutingRulesRequest listTeamRoutingRulesRequest = null;

    public TeamRoutingRuleExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "teamRoutingRules");
    }

    @Override
    protected String getBeanFileName(TeamRoutingRule bean) {
        return bean.getName() + "-" + bean.getId();
    }

    @Override
    public void export() {
        try {
            ListTeamsRequest listTeamsRequest = new ListTeamsRequest();
            List<Team> currentTeamList = getOpsGenieClient().team().listTeams(listTeamsRequest).getTeams();
            listTeamRoutingRulesRequest = new ListTeamRoutingRulesRequest();
            for (Team team : currentTeamList) {
                try {
                    listTeamRoutingRulesRequest.setTeamName(team.getName());
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
    protected List<TeamRoutingRule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        return getOpsGenieClient().team().listTeamRoutingRules(listTeamRoutingRulesRequest).getRules();
    }
}
