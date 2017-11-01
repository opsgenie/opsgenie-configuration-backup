package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.model.AlertPolicy;
import com.opsgenie.oas.sdk.model.AlertPolicyMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PolicyRetriever implements EntityRetriever<AlertPolicy> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyRetriever.class);

    private static final PolicyApi policyApi = new PolicyApi();

    @Override
    public List<AlertPolicy> retrieveEntities() {
        logger.info("------------------------------------");
        logger.info("Retrieving current policy configurations");
        final List<AlertPolicyMeta> policyMetaList = policyApi.listAlertPolicies().getData();
        List<AlertPolicy> policies = new ArrayList<AlertPolicy>();
        for (AlertPolicyMeta meta : policyMetaList) {
            policies.add(policyApi.getAlertPolicy(meta.getId()).getData());
        }
        return policies;


    }
}
