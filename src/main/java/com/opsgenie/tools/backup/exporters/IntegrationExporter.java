package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.IntegrationConfig;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class IntegrationExporter extends BaseExporter<IntegrationConfig> {

    public IntegrationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "integrations");
    }

    @Override
    protected String getEntityFileName(IntegrationConfig integrationConfig) {
        return integrationConfig.getIntegration().getId();
    }

    @Override
    protected List<IntegrationConfig> retrieveEntities() throws ParseException, IOException, ApiException {
        return EntityListService.listIntegrations();
    }
}
