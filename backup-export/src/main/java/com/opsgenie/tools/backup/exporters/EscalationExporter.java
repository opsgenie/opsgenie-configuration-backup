package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.Escalation;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.EscalationRetriever;

public class EscalationExporter extends BaseExporter<Escalation> {

    public EscalationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "escalations");
    }

    @Override
    protected EntityRetriever<Escalation> initializeEntityRetriever() {
        return new EscalationRetriever();
    }

    @Override
    protected String getEntityFileName(Escalation escalation) {
        return escalation.getName() + "-" + escalation.getId();
    }

}
