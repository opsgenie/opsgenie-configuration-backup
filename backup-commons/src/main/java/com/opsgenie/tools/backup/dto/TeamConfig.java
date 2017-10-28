package com.opsgenie.tools.backup.dto;

import com.opsgenie.oas.sdk.model.Team;
import com.opsgenie.oas.sdk.model.TeamRoutingRule;

import java.util.List;

public class TeamConfig {

    private Team team;
    private List<TeamRoutingRule> teamRoutingRules;

    public Team getTeam() {
        return team;
    }

    public TeamConfig setTeam(Team team) {
        this.team = team;
        return this;
    }

    public List<TeamRoutingRule> getTeamRoutingRules() {
        return teamRoutingRules;
    }

    public TeamConfig setTeamRoutingRules(List<TeamRoutingRule> teamRoutingRules) {
        this.teamRoutingRules = teamRoutingRules;
        return this;
    }
}
