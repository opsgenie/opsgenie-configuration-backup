package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.DeprecatedPolicyApi;
import com.opsgenie.oas.sdk.model.DeprecatedAlertPolicy;
import com.opsgenie.oas.sdk.model.DeprecatedAlertPolicyMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DeprecatedPolicyRetriever implements EntityRetriever<DeprecatedAlertPolicy> {

    private static final Logger logger = LoggerFactory.getLogger(DeprecatedPolicyRetriever.class);

    private static final DeprecatedPolicyApi policyApi = new DeprecatedPolicyApi();

    @Override
    public List<DeprecatedAlertPolicy> retrieveEntities() {
        logger.info("Retrieving current policy (old version) configurations");
        List<DeprecatedAlertPolicy> policies = new ArrayList<DeprecatedAlertPolicy>();
        for (DeprecatedAlertPolicyMeta meta : retrievePolicyMetaList()) {
            policies.add(policyApi.getAlertPolicy(meta.getId()).getData());
        }
        return policies;
    }

    public List<DeprecatedAlertPolicyMeta> retrievePolicyMetaList() {
        logger.info("Retrieving policy meta list (old version)");
        return policyApi.listAlertPolicies().getData();
    }
}
