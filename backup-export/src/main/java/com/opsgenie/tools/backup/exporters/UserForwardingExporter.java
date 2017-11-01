package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.ForwardingRule;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.ForwardingRetriever;

public class UserForwardingExporter extends BaseExporter<ForwardingRule> {

    public UserForwardingExporter(String backupRootDirectory) {
        super(backupRootDirectory, "forwardings");
    }

    @Override
    protected EntityRetriever<ForwardingRule> initializeEntityRetriever() {
        return new ForwardingRetriever();
    }

    @Override
    protected String getEntityFileName(ForwardingRule forwardingRule) {
        return forwardingRule.getId();
    }

}
