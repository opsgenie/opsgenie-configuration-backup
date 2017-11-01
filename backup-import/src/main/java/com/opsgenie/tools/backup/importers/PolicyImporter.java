package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.model.AlertPolicy;
import com.opsgenie.oas.sdk.model.UpdateAlertPolicyRequest;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.PolicyRetriever;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class PolicyImporter extends BaseImporter<AlertPolicy> {

    private static PolicyApi api = new PolicyApi();

    public PolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
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

    @Override
    protected EntityRetriever<AlertPolicy> initializeEntityRetriever() {
        return new PolicyRetriever();
    }

    @Override
    protected EntityStatus checkEntity(AlertPolicy entity) {
        for (AlertPolicy policy : currentConfigs) {
            if (policy.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (policy.getName().equals(entity.getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
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
    protected void updateEntity(AlertPolicy alertPolicy, EntityStatus entityStatus) throws ApiException {
        UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
        request.setBody(alertPolicy);
        final String id = alertPolicy.getId();
        alertPolicy.setId(null);
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setPolicyId(id);
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setPolicyId(findPolicyIdInCurrentConf(alertPolicy));
        }
        api.updateAlertPolicy(request);
    }

    private String findPolicyIdInCurrentConf(AlertPolicy alertPolicy) {
        for (AlertPolicy currentPolicy : currentConfigs) {
            if (currentPolicy.getName().equals(alertPolicy.getName())) {
                return currentPolicy.getId();
            }
        }
        return null;
    }

    @Override
    protected String getEntityIdentifierName(AlertPolicy alertPolicy) {
        return "Policy " + alertPolicy.getName();
    }
}
