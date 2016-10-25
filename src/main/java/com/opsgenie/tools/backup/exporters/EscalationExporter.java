package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Escalation;
import com.ifountain.opsgenie.client.model.escalation.ListEscalationsRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Escalation from Opsgenie account to local directory called escalations
 * @author Mehmet Mustafa Demir
 */
public class EscalationExporter extends BaseExporter<Escalation> {
    public EscalationExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "escalations");
    }

    @Override
    protected String getBeanFileName(Escalation bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Escalation> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListEscalationsRequest request = new ListEscalationsRequest();
        return getOpsGenieClient().escalation().listEscalations(request).getEscalations();
    }
}
