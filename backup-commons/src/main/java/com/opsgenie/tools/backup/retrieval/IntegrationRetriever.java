package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.IntegrationActionApi;
import com.opsgenie.oas.sdk.api.IntegrationApi;
import com.opsgenie.oas.sdk.model.Integration;
import com.opsgenie.oas.sdk.model.IntegrationMeta;
import com.opsgenie.oas.sdk.model.ListIntegrationRequest;
import com.opsgenie.tools.backup.dto.IntegrationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IntegrationRetriever implements EntityRetriever<IntegrationConfig> {

    private static Logger logger = LoggerFactory.getLogger(IntegrationRetriever.class);

    private static IntegrationApi integrationApi = new IntegrationApi();
    private static IntegrationActionApi integrationActionApi = new IntegrationActionApi();

    @Override
    public List<IntegrationConfig> retrieveEntities() throws InterruptedException {
        logger.info("Retrieving current integration configurations");
        final List<IntegrationMeta> integrationMetaList = integrationApi.listIntegrations(new ListIntegrationRequest()).getData();
        final ConcurrentLinkedQueue<IntegrationConfig> integrations = new ConcurrentLinkedQueue<IntegrationConfig>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final IntegrationMeta meta : integrationMetaList) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final IntegrationConfig integrationConfig = populateIntegrationActions(meta);
                        integrations.add(integrationConfig);
                    } catch (Exception e) {
                        logger.error("Could not retrieve integration with id: " + meta.getId() + " name:" + meta.getName() + "." + e.getMessage());
                    }
                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.info("Retrieving integrations :" + integrations.size() + "/" + integrationMetaList.size());
        }
        return new ArrayList<IntegrationConfig>(integrations);
    }

    private IntegrationConfig populateIntegrationActions(IntegrationMeta meta) {
        final IntegrationConfig integrationConfig = new IntegrationConfig();
        final Integration integration = integrationApi.getIntegration(meta.getId()).getData();

        integration.setId(meta.getId());
        integrationConfig.setIntegration(integration);
        try {
            integrationConfig.setIntegrationActions(integrationActionApi.listIntegrationActions(meta.getId()).getData());
        } catch (Exception e) {
            logger.info(integration.getName() + " is not an advanced integration, so not exporting actions");
        }
        return integrationConfig;
    }
}
