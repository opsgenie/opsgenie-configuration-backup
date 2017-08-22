package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.IntegrationApi;
import com.opsgenie.client.model.CreateIntegrationActionRequest;
import com.opsgenie.client.model.UpdateIntegrationActionRequest;
import com.opsgenie.client.model.UpdateIntegrationRequest;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.IntegrationWrapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class IntegrationImporter extends BaseImporter<IntegrationWrapper> {

    private static IntegrationApi integrationApi = new IntegrationApi();

    public IntegrationImporter(String backupRootDirectory, boolean addEntityEnabled, boolean updateEntityEnabled) {
        super(backupRootDirectory, addEntityEnabled, updateEntityEnabled);
    }

    @Override
    protected IntegrationWrapper checkEntityWithId(IntegrationWrapper integrationWrapper) throws ApiException {
        IntegrationWrapper result = new IntegrationWrapper();
        final String integrationId = integrationWrapper.getIntegration().getId();
        integrationWrapper.setIntegration(integrationApi.getIntegration(integrationId).getData());
        try {
            integrationWrapper.setIntegrationActions(integrationApi.getIntegrationActions(integrationId).getData());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    @Override
    protected IntegrationWrapper checkEntityWithName(IntegrationWrapper integration) throws ApiException {
        return null;
    }

    @Override
    protected IntegrationWrapper readEntity(String fileName) {
        try {
            String entityJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            return readJson(entityJson);
        } catch (Exception e) {
            logger.error("Could not read integration from file:" + fileName);
            return null;
        }
    }

    private IntegrationWrapper readJson(String entityJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(entityJson, IntegrationWrapper.class);
    }

    @Override
    protected String getImportDirectoryName() {
        return "integrations";
    }

    @Override
    protected void createEntity(IntegrationWrapper integrationWrapper) throws ParseException, IOException, ApiException {
        integrationApi.createIntegration(integrationWrapper.getIntegration());
    }

    @Override
    protected void updateEntity(IntegrationWrapper entity, EntityStatus entityStatus) throws ParseException, IOException, ApiException {
        final String integrationId = entity.getIntegration().getId();
        final UpdateIntegrationRequest updateIntegrationRequest = new UpdateIntegrationRequest().body(entity.getIntegration()).id(integrationId);
        integrationApi.updateIntegration(updateIntegrationRequest);
        final UpdateIntegrationActionRequest updateIntegrationActionRequest = new UpdateIntegrationActionRequest().body(entity.getIntegrationActions()).integrationId(integrationId);
        integrationApi.updateIntegrationActions(updateIntegrationActionRequest);
    }

    @Override
    protected String getEntityIdentifierName(IntegrationWrapper entity) {
        return "Integration " + entity.getIntegration().getName();
    }
}
