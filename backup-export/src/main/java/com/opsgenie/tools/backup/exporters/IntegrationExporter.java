package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.IntegrationConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.IntegrationRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;

public class IntegrationExporter extends BaseExporterWithRateLimiting<IntegrationConfig> {

    public IntegrationExporter(String backupRootDirectory, RateLimitManager rateLimitManager) {
        super(backupRootDirectory, "integrations", rateLimitManager);
    }

    @Override
    protected EntityRetriever<IntegrationConfig> initializeEntityRetriever() {
        return new IntegrationRetriever(rateLimitManager);
    }

    @Override
    protected String getEntityFileName(IntegrationConfig integrationConfig) {
        return integrationConfig.getIntegration().getId();
    }

}
