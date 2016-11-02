package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Forwarding;
import com.ifountain.opsgenie.client.model.user.forward.ListForwardingsRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports user forwarding from Opsgenie account to local directory called
 * forwardings
 *
 * @author Mehmet Mustafa Demir
 */
public class UserForwardingExporter extends BaseExporter<Forwarding> {

    public UserForwardingExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "forwardings");
    }

    @Override
    protected String getBeanFileName(Forwarding bean) {
        return bean.getFromUser() + "-" + bean.getId();
    }


    @Override
    protected List<Forwarding> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListForwardingsRequest listForwardingsRequest = new ListForwardingsRequest();
        return getOpsGenieClient().user().listForwardings(listForwardingsRequest).getForwardings();
    }
}
