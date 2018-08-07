package com.opsgenie.tools.backup.dto;

import com.opsgenie.oas.sdk.model.Policy;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 10:19
 */
public class PolicyWithTeamInfo {
    String teamId;
    String teamName;
    Policy policy;

    public PolicyWithTeamInfo(String teamId, String teamName, Policy policy){
        this.teamId = teamId;
        this.teamName = teamName;
        this.policy = policy;
    }
    public PolicyWithTeamInfo(){

    }

    public String getTeamId() {
        return teamId;
    }

    public PolicyWithTeamInfo setTeamId(String teamId) {
        this.teamId = teamId;
        return this;
    }

    public String getTeamName() {
        return teamName;
    }

    public PolicyWithTeamInfo setTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public Policy getPolicy() {
        return policy;
    }

    public PolicyWithTeamInfo setPolicy(Policy policy) {
        this.policy = policy;
        return this;
    }

}
