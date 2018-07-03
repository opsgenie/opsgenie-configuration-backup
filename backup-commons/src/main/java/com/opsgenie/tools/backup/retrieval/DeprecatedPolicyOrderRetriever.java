package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.DeprecatedPolicyApi;
import com.opsgenie.oas.sdk.model.DeprecatedAlertPolicyMeta;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DeprecatedPolicyOrderRetriever implements EntityRetriever<PolicyConfig> {

    private static final Logger logger = LoggerFactory.getLogger(DeprecatedPolicyOrderRetriever.class);

    private static final DeprecatedPolicyApi policyApi = new DeprecatedPolicyApi();

    @Override
    public List<PolicyConfig> retrieveEntities() throws Exception {
        logger.info("Retrieving current policy orders");
        final List<DeprecatedAlertPolicyMeta> policyMetaList = apiAdapter.invoke(new Callable<List<DeprecatedAlertPolicyMeta>>() {
            @Override
            public List<DeprecatedAlertPolicyMeta> call() throws Exception {
                return policyApi.listAlertPolicies().getData();
            }
        });
        List<PolicyConfig> policyOrderList = new ArrayList<PolicyConfig>();
        for (DeprecatedAlertPolicyMeta meta : policyMetaList) {
            policyOrderList.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName()).setOrder(meta.getOrder()));
        }
        return policyOrderList;
    }

}
