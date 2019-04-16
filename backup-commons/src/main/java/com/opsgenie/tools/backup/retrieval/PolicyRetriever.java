package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.PolicyWithTeamInfo;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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

    public void retrieveGlobalPolicies() throws Exception {
        logger.info("Retrieving global alert policy meta list");
        List<PolicyMeta> globalPolicies = RetryPolicyAdapter.invoke(new Callable<List<PolicyMeta>>() {
            @Override
            public List<PolicyMeta> call()  {
                return policyApi.listAlertPolicies(null).getData();
            }
        });

        getPolicies(globalPolicies, null, null);
    }

    public void retrieveTeamPolicies() throws Exception {
        logger.info("Retrieving team metas for team policies");
        final List<Team> teams = RetryPolicyAdapter.invoke(new Callable<List<Team>>() {
            @Override
            public List<Team> call()  {
                return teamApi.listTeams().getData();
            }
        });

        for (Team teamMeta : teams){
            getAlertPolicies(teamMeta.getId(), teamMeta.getName());
            getNotificationPolicies(teamMeta.getId(), teamMeta.getName());
        }
    }

    public void getAlertPolicies(final String teamId, final String teamName) throws Exception {
        logger.info("Retrieving alert policy list for team with id: " + teamId);
        ListPoliciesResponse listAlertPoliciesResponse = RetryPolicyAdapter.invoke(new Callable<ListPoliciesResponse>() {
            @Override
            public ListPoliciesResponse call()  {
                return policyApi.listAlertPolicies(teamId);
            }
        });

        List<PolicyMeta> policyMetaList = listAlertPoliciesResponse.getData();
        getPolicies(policyMetaList, teamId, teamName);
    }

    public void getNotificationPolicies(final String teamId, String teamName) throws Exception {
        logger.info("Retrieving notification policy list for team with id: " + teamId);
        ListPoliciesResponse listNotfPoliciesResponse = RetryPolicyAdapter.invoke(new Callable<ListPoliciesResponse>() {
            @Override
            public ListPoliciesResponse call()  {
                return policyApi.listNotificationPolicies(teamId);
            }
        });

        List<PolicyMeta> notfPolicyMetaList = listNotfPoliciesResponse.getData();

        getPolicies(notfPolicyMetaList, teamId, teamName);
    }

    public void getPolicies(List<PolicyMeta> policyMetaList, final String teamId, final String teamName) throws Exception {
        if (policyMetaList != null){
            for (final PolicyMeta policyMeta : policyMetaList){
                GetPolicyResponse policyResponse = RetryPolicyAdapter.invoke(new Callable<GetPolicyResponse>() {
                    @Override
                    public GetPolicyResponse call()  {
                        return policyApi.getPolicy(policyMeta.getId(), teamId);
                    }
                });

                Policy policy = policyResponse.getData();
                if (policy != null){
                    policies.add(new PolicyWithTeamInfo(teamId, teamName, policy));
                }
            }
        }
    }


}
