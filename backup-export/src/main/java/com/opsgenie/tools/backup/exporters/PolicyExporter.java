package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.AlertPolicy;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.PolicyRetriever;

public class PolicyExporter extends BaseExporter<AlertPolicy> {

    public PolicyExporter(String backupRootDirectory) {
        super(backupRootDirectory, "policies");
    }

    @Override
    protected EntityRetriever<AlertPolicy> initializeEntityRetriever() {
        return new PolicyRetriever();
    }

    @Override
    protected String getEntityFileName(AlertPolicy alertPolicy) {
        return alertPolicy.getId();
    }

}
