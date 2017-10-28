package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.IntegrationActionApi;
import com.opsgenie.oas.sdk.api.IntegrationApi;
import com.opsgenie.oas.sdk.model.CreateIntegrationResponse;
import com.opsgenie.oas.sdk.model.Integration;
import com.opsgenie.oas.sdk.model.UpdateIntegrationActionRequest;
import com.opsgenie.oas.sdk.model.UpdateIntegrationRequest;
import com.opsgenie.tools.backup.util.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.dto.IntegrationConfig;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class IntegrationImporter extends BaseImporter<IntegrationConfig> {

    private static IntegrationApi integrationApi = new IntegrationApi();
    private static IntegrationActionApi integrationActionApi = new IntegrationActionApi();
    private List<IntegrationConfig> integrationConfigList = new ArrayList<IntegrationConfig>();

    public IntegrationImporter(String backupRootDirectory, boolean addEntityEnabled, boolean updateEntityEnabled) {
        super(backupRootDirectory, addEntityEnabled, updateEntityEnabled);
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
    protected EntityStatus checkEntity(IntegrationConfig integrationConfigToImport) throws ApiException {
        for (IntegrationConfig currentIntegrationConfig : integrationConfigList) {
            final Integration currentIntegration = currentIntegrationConfig.getIntegration();
            if (currentIntegration.getId().equals(integrationConfigToImport.getIntegration().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (currentIntegration.getName().equals(integrationConfigToImport.getIntegration().getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        integrationConfigList = EntityListService.listIntegrations();
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
    protected void createEntity(IntegrationConfig integrationConfig) throws ParseException, IOException, ApiException {
        final Integration integration = integrationConfig.getIntegration();
        final CreateIntegrationResponse createIntegrationResponse = integrationApi.createIntegration(integration);
        System.out.println(createIntegrationResponse);
        final UpdateIntegrationActionRequest updateIntegrationActionRequest = new UpdateIntegrationActionRequest()
                .body(integrationConfig.getIntegrationActions())
                .id(createIntegrationResponse.getData().getId());
        integrationActionApi.updateIntegrationActions(updateIntegrationActionRequest);
    }

    @Override
    protected void updateEntity(IntegrationConfig integration, EntityStatus entityStatus) throws ParseException, IOException, ApiException {
        final String integrationId = integration.getIntegration().getId();
        final UpdateIntegrationRequest updateIntegrationRequest = new UpdateIntegrationRequest().body(integration.getIntegration());
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            updateIntegrationRequest.setId(integrationId);
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            updateIntegrationRequest.setId(findIntegrationIdWithName(integration));
        }
        integrationApi.updateIntegration(updateIntegrationRequest);
        final UpdateIntegrationActionRequest updateIntegrationActionRequest = new UpdateIntegrationActionRequest()
                .body(integration.getIntegrationActions())
                .id(findIntegrationIdWithName(integration));
        integrationActionApi.updateIntegrationActions(updateIntegrationActionRequest);
    }

    private String findIntegrationIdWithName(IntegrationConfig integrationToImport) {
        for (IntegrationConfig currentConf : integrationConfigList) {
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
