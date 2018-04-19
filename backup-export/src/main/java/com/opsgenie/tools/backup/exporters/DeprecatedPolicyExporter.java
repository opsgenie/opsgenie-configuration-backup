package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.DeprecatedAlertPolicy;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.DeprecatedPolicyRetriever;

public class DeprecatedPolicyExporter extends BaseExporter<DeprecatedAlertPolicy> {

    public DeprecatedPolicyExporter(String backupRootDirectory) {
        super(backupRootDirectory, "policies");
    }

    @Override
    protected EntityRetriever<DeprecatedAlertPolicy> initializeEntityRetriever() {
        return new DeprecatedPolicyRetriever();
    }

    @Override
    protected String getEntityFileName(DeprecatedAlertPolicy alertPolicy) {
        return alertPolicy.getId();
    }

}
