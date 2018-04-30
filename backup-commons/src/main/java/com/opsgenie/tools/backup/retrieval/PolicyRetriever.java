package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.PolicyWithTeamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zeynep Sengil
 * @version 19.04.2018 14:37
 */
public class PolicyRetriever  implements EntityRetriever<PolicyWithTeamInfo> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyRetriever.class);

    private static final PolicyApi policyApi = new PolicyApi();
    private static final TeamApi teamApi = new TeamApi();

    private List<PolicyWithTeamInfo> policies = new ArrayList<PolicyWithTeamInfo>();

    @Override
    public List<PolicyWithTeamInfo> retrieveEntities() throws Exception {
        logger.info("Retrieving current policy (new version) configurations");
        retrieveGlobalPolicies();
        retrieveTeamPolicies();
        return policies;
    }

    public void retrieveGlobalPolicies() {
        logger.info("Retrieving global alert policy meta list");
        List<PolicyMeta> globalPolicies =  policyApi.listAlertPolicies(null).getData();
        getPolicies(globalPolicies, null);
    }

    public void retrieveTeamPolicies(){
        logger.info("Retrieving team metas for team policies");
        final List<Team> teams = teamApi.listTeams(new ArrayList<String>()).getData();
        for (Team teamMeta : teams){
            getAlertPolicies(teamMeta.getId());
            getNotificationPolicies(teamMeta.getId());
        }
    }

    public void getAlertPolicies(String teamId){
        logger.info("Retrieving alert policy list for team with id: " + teamId);
        ListPoliciesResponse listAlertPoliciesResponse = policyApi.listAlertPolicies(teamId);
        List<PolicyMeta> policyMetaList = listAlertPoliciesResponse.getData();
        getPolicies(policyMetaList, teamId);
    }

    public void getNotificationPolicies(String teamId){
        logger.info("Retrieving notification policy list for team with id: " + teamId);
        ListPoliciesResponse listNotfPoliciesResponse = policyApi.listNotificationPolicies(teamId);
        List<PolicyMeta> notfPolicyMetaList = listNotfPoliciesResponse.getData();

        getPolicies(notfPolicyMetaList, teamId);
    }

    public void getPolicies(List<PolicyMeta> policyMetaList, String teamId){
        if (policyMetaList != null){
            for (PolicyMeta policyMeta : policyMetaList){
                GetPolicyResponse policyResponse = policyApi.getPolicy(policyMeta.getId(), teamId);
                Policy policy = policyResponse.getData();
                if (policy != null){
                    policies.add(new PolicyWithTeamInfo(teamId, policy));
                }
            }
        }
    }


}
