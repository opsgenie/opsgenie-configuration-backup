package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.api.TeamRoutingRuleApi;
import com.opsgenie.oas.sdk.model.GetTeamRequest;
import com.opsgenie.oas.sdk.model.ListTeamRoutingRulesRequest;
import com.opsgenie.oas.sdk.model.Team;
import com.opsgenie.oas.sdk.model.TeamRoutingRule;
import com.opsgenie.tools.backup.dto.TeamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TeamRetriever implements EntityRetriever<TeamConfig> {

    private static final Logger logger = LoggerFactory.getLogger(TeamRetriever.class);

    private static final TeamApi teamApi = new TeamApi();
    private static final TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();

    @Override
    public List<TeamConfig> retrieveEntities() throws InterruptedException {
        logger.info("------------------------------------");
        logger.info("Retrieving current team configurations");
        final List<Team> teams = teamApi.listTeams(new ArrayList<String>()).getData();
        final ConcurrentLinkedQueue<TeamConfig> teamsWithDetails = new ConcurrentLinkedQueue<TeamConfig>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final Team teamMeta : teams) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    TeamConfig teamConfig = new TeamConfig();
                    boolean failed = false;
                    try {
                        final List<TeamRoutingRule> routingRules = teamRoutingRuleApi.listTeamRoutingRules(new ListTeamRoutingRulesRequest().identifier(teamMeta.getId())).getData();
                        teamConfig.setTeamRoutingRules(routingRules);
                    } catch (Exception e) {
                        failed = true;
                        logger.error("Could not list team routing rules for team: " + teamMeta.getId());
                    }

                    try {
                        final Team team = teamApi.getTeam(new GetTeamRequest().identifier(teamMeta.getId())).getData();
                        teamConfig.setTeam(team);
                    } catch (Exception e) {
                        failed = true;
                        logger.error("Could not get team details for team: " + teamMeta.getId());
                    }
                    if (!failed) {
                        teamsWithDetails.add(teamConfig);
                    }
                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.info("Populating team routing rules:" + teamsWithDetails.size() + "/" + teams.size());
        }
        return new ArrayList<TeamConfig>(teamsWithDetails);
    }
}
