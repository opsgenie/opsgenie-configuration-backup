package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.PolicyApi;
import com.opsgenie.client.model.AlertPolicy;
import com.opsgenie.client.model.UpdateAlertPolicyRequest;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class PolicyImporter extends BaseImporter<AlertPolicy> {

    private static PolicyApi api = new PolicyApi();

    public PolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
    }

    @Override
    protected AlertPolicy checkEntityWithId(AlertPolicy entity) throws ApiException {
        return api.getAlertPolicy(entity.getId()).getData();
    }

    @Override
    protected AlertPolicy checkEntityWithName(AlertPolicy entity) throws ApiException {
        return null;
    }

    @Override
    protected AlertPolicy readEntity(String fileName) {
        try {
            String alertPolicyJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            return readJson(alertPolicyJson);
        } catch (Exception e) {
            logger.error("Could not read policy from file:" + fileName);
            return null;
        }
    }

    private AlertPolicy readJson(String alertPolicyJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(alertPolicyJson, AlertPolicy.class);
    }

    @Override
    protected String getImportDirectoryName() {
        return "policies";
    }

    @Override
    protected void createEntity(AlertPolicy entity) throws ApiException {
        entity.setId(null);
        api.createAlertPolicy(entity);
    }

    @Override
    protected void updateEntity(AlertPolicy entity, EntityStatus entityStatus) throws ApiException {
        UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
        entity.setId(null);
        request.setBody(entity);
        request.setPolicyId(entity.getId());
        api.updateAlertPolicy(request);
    }

    @Override
    protected String getEntityIdentifierName(AlertPolicy alertPolicy) {
        return "Policy " + alertPolicy.getName();
    }
}