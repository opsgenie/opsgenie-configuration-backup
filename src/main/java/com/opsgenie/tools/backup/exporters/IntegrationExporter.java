package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.IntegrationApi;
import com.opsgenie.client.model.Integration;
import com.opsgenie.client.model.IntegrationMeta;
import com.opsgenie.client.model.ListIntegrationRequest;
import com.opsgenie.tools.backup.IntegrationWrapper;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationExporter extends BaseExporter<IntegrationWrapper> {

    private static IntegrationApi integrationApi = new IntegrationApi();

    public IntegrationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "integrations");
    }

    @Override
    protected String getEntityFileName(IntegrationWrapper integrationWrapper) {
        return integrationWrapper.getIntegration().getId();
    }

    @Override
    protected List<IntegrationWrapper> retrieveEntities() throws ParseException, IOException, ApiException {
        final List<IntegrationMeta> integrationMetaList = integrationApi.listIntegrations(new ListIntegrationRequest()).getData();
        List<IntegrationWrapper> integrations = new ArrayList<IntegrationWrapper>();
        for (IntegrationMeta meta : integrationMetaList) {
            final IntegrationWrapper integrationWrapper = new IntegrationWrapper();
            final Integration integration = integrationApi.getIntegration(meta.getId()).getData();

            integration.setId(meta.getId());
            integrationWrapper.setIntegration(integration);
            try {
                integrationWrapper.setIntegrationActions(integrationApi.getIntegrationActions(meta.getId()).getData());
            } catch (Exception e) {
                logger.info(integration.getName() + " is not an advanced integration, so not exporting actions");
            }

            integrations.add(integrationWrapper);
        }
        return integrations;
    }
}
