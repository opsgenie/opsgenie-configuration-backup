package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.PolicyApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PolicyImporter extends BaseImporter<AlertPolicy> {

    private static PolicyApi api = new PolicyApi();
    private final Logger logger = LogManager.getLogger(PolicyImporter.class);

    public PolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
    }

    @Override
    protected void getEntityWithId(AlertPolicy entity) throws ApiException {
        api.getAlertPolicy(entity.getId());
    }

    @Override
    protected AlertPolicy getBean() throws IOException, ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected AlertPolicy readEntity(String fileName) {
        try {
            String beanJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            AlertPolicy alertPolicy = readJson(beanJson);
            return alertPolicy;
        } catch (Exception e) {
            logger.error("Could not read policy from file:"+ fileName);
            return null;
        }
    }

    private AlertPolicy readJson(String beanJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(beanJson, AlertPolicy.class);
    }

    @Override
    protected String getImportDirectoryName(){
        return "policies";
    }

    @Override
    protected void addBean(AlertPolicy bean) throws ApiException {
        bean.setId(null);
        api.createAlertPolicy(bean);
    }

    @Override
    protected void updateBean(AlertPolicy bean) throws ApiException {
        UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
        bean.setId(null);
        request.setBody(bean);
        request.setPolicyId(bean.getId());
        api.updateAlertPolicy(request);
    }

    @Override
    protected String getEntityIdentifierName(AlertPolicy bean) {
        return "Policy " + bean.getName();
    }
}