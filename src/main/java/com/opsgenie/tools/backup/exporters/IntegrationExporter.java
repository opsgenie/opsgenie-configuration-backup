package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.IntegrationApi;
import com.opsgenie.client.model.Integration;
import com.opsgenie.client.model.IntegrationMeta;
import com.opsgenie.client.model.ListIntegrationPayload;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationExporter extends BaseExporter<Integration> {

    private static IntegrationApi integrationApi = new IntegrationApi();

    public IntegrationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "integrations");
    }

    @Override
    protected String getBeanFileName(Integration integration) {
        return integration.getId();
    }

    @Override
    protected List<Integration> retrieveEntities() throws ParseException, IOException, ApiException {
        final List<IntegrationMeta> integrationMetaList = integrationApi.listIntegrations(new ListIntegrationPayload()).getData();
        List<Integration> integrations = new ArrayList<Integration>();
        for (IntegrationMeta meta : integrationMetaList) {
            final Integration integration = integrationApi.getIntegration(meta.getId()).getData();
            integration.setId(meta.getId());
            integrations.add(integration);
        }
        return integrations;
    }
}
