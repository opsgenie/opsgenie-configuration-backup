package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Heartbeat;
import com.ifountain.opsgenie.client.model.customer.ListHeartbeatsRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Heartbeats from Opsgenie account to local directory called heartbeats
 *
 * @author Mehmet Mustafa Demir
 */
public class HeartbeatExporter extends BaseExporter<Heartbeat> {

    public HeartbeatExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "heartbeats");
    }

    @Override
    protected String getBeanFileName(Heartbeat bean) {
        return bean.getName();
    }

    @Override
    protected List<Heartbeat> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListHeartbeatsRequest request = new ListHeartbeatsRequest();
        return getOpsGenieClient().listHeartbeats(request).getHeartbeats();
    }


}
