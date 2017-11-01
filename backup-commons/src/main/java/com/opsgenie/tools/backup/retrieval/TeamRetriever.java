package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.api.TeamRoutingRuleApi;
import com.opsgenie.oas.sdk.model.ListTeamRoutingRulesRequest;
import com.opsgenie.oas.sdk.model.Team;
import com.opsgenie.oas.sdk.model.TeamRoutingRule;
import com.opsgenie.tools.backup.dto.TeamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
        final List<Team> teams = teamApi.listTeams(Collections.singletonList("member")).getData();
        final ConcurrentLinkedQueue<TeamConfig> teamsWithRoutingRules = new ConcurrentLinkedQueue<TeamConfig>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final Team team : teams) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    TeamConfig teamConfig = new TeamConfig();
                    teamConfig.setTeam(team);
                    final List<TeamRoutingRule> routingRules = teamRoutingRuleApi.listTeamRoutingRules(new ListTeamRoutingRulesRequest().identifier(team.getId())).getData();
                    teamConfig.setTeamRoutingRules(routingRules);
                    teamsWithRoutingRules.add(teamConfig);
                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.info("Populating team routing rules:" + teamsWithRoutingRules.size() + "/" + teams.size());
        }
        return new ArrayList<TeamConfig>(teamsWithRoutingRules);
    }
}
