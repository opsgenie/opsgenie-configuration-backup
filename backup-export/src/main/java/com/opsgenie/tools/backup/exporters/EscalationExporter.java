package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.model.Escalation;
import com.opsgenie.tools.backup.EntityListService;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class EscalationExporter extends BaseExporter<Escalation> {

    public EscalationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "escalations");
    }

    @Override
    protected String getEntityFileName(Escalation escalation) {
        return escalation.getName() + "-" + escalation.getId();
    }


    @Override
    protected List<Escalation> retrieveEntities() throws ParseException, IOException, ApiException {
        return EntityListService.listEscalations();
    }
}
