package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.api.TeamApi;
import com.opsgenie.oas.sdk.model.PolicyMeta;
import com.opsgenie.oas.sdk.model.Team;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Zeynep Sengil
 * @version 24.04.2018 13:35
 */

public class PolicyOrderRetriever implements EntityRetriever<PolicyConfig> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyOrderRetriever.class);

    private static final PolicyApi policyApi = new PolicyApi();
    private static final TeamApi teamApi = new TeamApi();

    @Override
    public List<PolicyConfig> retrieveEntities() throws Exception {
        logger.info("Retrieving current policy V2 orders");
        List<PolicyConfig> policyOrderList = new ArrayList<PolicyConfig>();

        final List<Team> teamList = RetryPolicyAdapter.invoke(new Callable<List<Team>>() {
            @Override
            public List<Team> call()  {
                return teamApi.listTeams().getData();
            }
        });

        final List<PolicyMeta> globalPolicyMetaList = RetryPolicyAdapter.invoke(new Callable<List<PolicyMeta>>() {
            @Override
            public List<PolicyMeta> call()  {
                return policyApi.listAlertPolicies("").getData();
            }
        });

        for (PolicyMeta meta : globalPolicyMetaList) {
            policyOrderList.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName())
                    .setOrder(meta.getOrder()).setTeam(""));
        }

        for (final Team team : teamList){
            final List<PolicyMeta> alertPolicyMetaList = RetryPolicyAdapter.invoke(new Callable<List<PolicyMeta>>() {
                @Override
                public List<PolicyMeta> call()  {
                    return policyApi.listAlertPolicies(team.getId()).getData();
                }
            });

            final List<PolicyMeta> notfPolicyMetaList = RetryPolicyAdapter.invoke(new Callable<List<PolicyMeta>>() {
                @Override
                public List<PolicyMeta> call()  {
                    return policyApi.listNotificationPolicies(team.getId()).getData();
                }
            });


            for (PolicyMeta meta : alertPolicyMetaList) {
                policyOrderList.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName())
                        .setOrder(meta.getOrder()).setTeam(team.getId()));
            }

            for (PolicyMeta meta : notfPolicyMetaList) {
                policyOrderList.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName())
                        .setOrder(meta.getOrder()).setTeam(team.getId()));
            }

        }
        return policyOrderList;
    }

}