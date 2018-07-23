package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.IntegrationActionApi;
import com.opsgenie.oas.sdk.api.IntegrationApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.IntegrationConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.IntegrationRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

public class IntegrationImporter extends BaseImporterWithRateLimiting<IntegrationConfig> {

    private static IntegrationApi integrationApi = new IntegrationApi();
    private static IntegrationActionApi integrationActionApi = new IntegrationActionApi();

    public IntegrationImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntityEnabled, boolean updateEntityEnabled) {
        super(backupRootDirectory, rateLimitManager, addEntityEnabled, updateEntityEnabled);
    }

    @Override
    protected IntegrationConfig readEntity(String fileName) {
        try {
            String entityJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            return readJson(entityJson);
        } catch (Exception e) {
            logger.error("Could not read integration from file:" + fileName);
            return null;
        }
    }

    @Override
    protected EntityRetriever<IntegrationConfig> initializeEntityRetriever() {
        return new IntegrationRetriever(rateLimitManager);
    }

    @Override
    protected EntityStatus checkEntity(IntegrationConfig integrationConfigToImport) throws ApiException {
        for (IntegrationConfig currentIntegrationConfig : currentConfigs) {
            final Integration currentIntegration = currentIntegrationConfig.getIntegration();
            if (currentIntegration.getId().equals(integrationConfigToImport.getIntegration().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (currentIntegration.getName().equals(integrationConfigToImport.getIntegration().getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    private IntegrationConfig readJson(String entityJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(entityJson, IntegrationConfig.class);
    }

    @Override
    protected String getImportDirectoryName() {
        return "integrations";
    }

    @Override
    protected void createEntity(IntegrationConfig integrationConfig) throws Exception {
        final Integration integration = integrationConfig.getIntegration();
        final CreateIntegrationResponse createIntegrationResponse = RetryPolicyAdapter.invoke(new Callable<CreateIntegrationResponse>() {
            @Override
            public CreateIntegrationResponse call() throws Exception {
                return integrationApi.createIntegration(integration);
            }
        });

        System.out.println(createIntegrationResponse);
        final UpdateIntegrationActionRequest updateIntegrationActionRequest = new UpdateIntegrationActionRequest()
                .body(integrationConfig.getIntegrationActions())
                .id(createIntegrationResponse.getData().getId());
        RetryPolicyAdapter.invoke(new Callable<UpdateIntegrationActionsResponse>() {
            @Override
            public UpdateIntegrationActionsResponse call() throws Exception {
                return integrationActionApi.updateIntegrationActions(updateIntegrationActionRequest);
            }
        });

    }

    @Override
    protected void updateEntity(IntegrationConfig integration, EntityStatus entityStatus) throws Exception {
        final String integrationId = integration.getIntegration().getId();
        final UpdateIntegrationRequest updateIntegrationRequest = new UpdateIntegrationRequest().body(integration.getIntegration());
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            updateIntegrationRequest.setId(integrationId);
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            updateIntegrationRequest.setId(findIntegrationIdWithName(integration));
        }
        RetryPolicyAdapter.invoke(new Callable<UpdateIntegrationResponse>() {
            @Override
            public UpdateIntegrationResponse call() throws Exception {
                return integrationApi.updateIntegration(updateIntegrationRequest);
            }
        });

        final UpdateIntegrationActionRequest updateIntegrationActionRequest = new UpdateIntegrationActionRequest()
                .body(integration.getIntegrationActions())
                .id(findIntegrationIdWithName(integration));
        RetryPolicyAdapter.invoke(new Callable<UpdateIntegrationActionsResponse>() {
            @Override
            public UpdateIntegrationActionsResponse call() throws Exception {
                return integrationActionApi.updateIntegrationActions(updateIntegrationActionRequest);
            }
        });

    }

    private String findIntegrationIdWithName(IntegrationConfig integrationToImport) {
        for (IntegrationConfig currentConf : currentConfigs) {
            if (currentConf.getIntegration().getName().equals(integrationToImport.getIntegration().getName())) {
                return currentConf.getIntegration().getId();
            }
        }
        return null;
    }

    @Override
    protected String getEntityIdentifierName(IntegrationConfig entity) {
        return "Integration " + entity.getIntegration().getName();
    }
}
