package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.PolicyApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PolicyImporter extends BaseImporter<AlertPolicy> {

    private static PolicyApi api = new PolicyApi();

    public PolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
    }

    @Override
    protected BeanStatus checkEntities(AlertPolicy oldEntity, AlertPolicy currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
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
        bean.getFilter().setType(null);
        bean.type(null);
        bean.getTimeRestrictions().setType(null);
        api.createAlertPolicy(bean);
    }

    @Override
    protected void updateBean(AlertPolicy bean) throws ApiException {
        UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
        bean.type(null);
        bean.getFilter().setType(null);
        bean.getTimeRestrictions().setType(null);
        request.setBody(bean);
        request.setPolicyId(bean.getId());

        api.updateAlertPolicy(request);
    }

    @Override
    protected List<AlertPolicy> retrieveEntities() throws ApiException {
        List<AlertPolicyMeta> metaList = api.listAlertPolicies().getData();
        List<AlertPolicy> alertPolicyList = new ArrayList<AlertPolicy>();

        for (AlertPolicyMeta meta: metaList){
            GetAlertPolicyResponse response = api.getAlertPolicy(meta.getId());
            alertPolicyList.add(response.getData());
        }

        return alertPolicyList;
    }

    @Override
    protected String getEntityIdentifierName(AlertPolicy bean) {
        return "Policy " + bean.getName();
    }
}