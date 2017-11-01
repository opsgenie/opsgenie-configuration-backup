package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.IntegrationConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.IntegrationRetriever;

public class IntegrationExporter extends BaseExporter<IntegrationConfig> {

    public IntegrationExporter(String backupRootDirectory) {
        super(backupRootDirectory, "integrations");
    }

    @Override
    protected EntityRetriever<IntegrationConfig> initializeEntityRetriever() {
        return new IntegrationRetriever();
    }

    @Override
    protected String getEntityFileName(IntegrationConfig integrationConfig) {
        return integrationConfig.getIntegration().getId();
    }

}
