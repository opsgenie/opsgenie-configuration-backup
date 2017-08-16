package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.PolicyApi;
import com.opsgenie.client.model.*;

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
    protected AlertPolicy getBean(){
        return new AlertPolicy();
    }

    @Override
    protected String getImportDirectoryName(){
        return "policies";
    }

    @Override
    protected void addBean(AlertPolicy bean) throws ApiException {
        api.createAlertPolicy(bean);
    }

    @Override
    protected void updateBean(AlertPolicy bean) throws ApiException {
        UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
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