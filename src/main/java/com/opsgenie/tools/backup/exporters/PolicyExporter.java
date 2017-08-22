package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.PolicyApi;
import com.opsgenie.client.model.AlertPolicy;
import com.opsgenie.client.model.AlertPolicyMeta;

import java.util.ArrayList;
import java.util.List;

public class PolicyExporter extends BaseExporter<AlertPolicy> {

    private static PolicyApi policyApi = new PolicyApi();

    public PolicyExporter(String backupRootDirectory) {
        super(backupRootDirectory, "policies");
    }

    @Override
    protected String getEntityFileName(AlertPolicy alertPolicy) {
        return alertPolicy.getId();
    }

    @Override
    protected List<AlertPolicy> retrieveEntities() throws ApiException {
        final List<AlertPolicyMeta> policyMetaList = policyApi.listAlertPolicies().getData();
        List<AlertPolicy> policies = new ArrayList<AlertPolicy>();
        for (AlertPolicyMeta meta : policyMetaList) {
            policies.add(policyApi.getAlertPolicy(meta.getId()).getData());
        }
        return policies;
    }
}
