package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.model.AlertPolicyMeta;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PolicyOrderRetriever implements EntityRetriever<PolicyConfig> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyOrderRetriever.class);

    private static final PolicyApi policyApi = new PolicyApi();

    @Override
    public List<PolicyConfig> retrieveEntities() {
        logger.info("Retrieving current policy orders");
        final List<AlertPolicyMeta> policyMetaList = policyApi.listAlertPolicies().getData();
        List<PolicyConfig> policyOrderList = new ArrayList<PolicyConfig>();
        for (AlertPolicyMeta meta : policyMetaList) {
            policyOrderList.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName()).setOrder(meta.getOrder()));
        }
        return policyOrderList;
    }

}
