package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.PolicyApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import com.opsgenie.tools.backup.dto.PolicyWithTeamInfo;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.PolicyOrderRetriever;
import com.opsgenie.tools.backup.retrieval.PolicyRetriever;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 10:54
 */
public class PolicyImporter extends BaseImporter<PolicyWithTeamInfo> {

    private static PolicyApi api = new PolicyApi();
    private String rootPath;
    private List<PolicyConfig> policyOrderConfigFromFile = new ArrayList<PolicyConfig>();

    public PolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
        this.rootPath = backupRootDirectory;
    }
    @Override
    protected EntityRetriever<PolicyWithTeamInfo> initializeEntityRetriever() {
        return new PolicyRetriever();
    }

    @Override
    protected EntityStatus checkEntity(PolicyWithTeamInfo entity) throws ApiException {
        for (PolicyWithTeamInfo policyWithTeamInfo : currentConfigs) {
            if (policyWithTeamInfo.getPolicy().getId().equals(entity.getPolicy().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (policyWithTeamInfo.getPolicy().getName().equals(entity.getPolicy().getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void createEntity(PolicyWithTeamInfo entity) throws ParseException, IOException, ApiException {
        Policy policy = entity.getPolicy();
        policy.setId(null);
        api.createPolicy(policy, entity.getTeamId());
    }

    @Override
    protected void updateEntity(PolicyWithTeamInfo entity, EntityStatus entityStatus) throws ParseException, IOException, ApiException {
        Policy policy = entity.getPolicy();
        UpdatePolicyRequest request = new UpdatePolicyRequest();
        request.setBody(policy);
        request.setTeamId(entity.getTeamId());

        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setPolicyId(policy.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setPolicyId(findPolicyIdInCurrentConf(policy.getName()));
        }
        api.updatePolicy(request);
    }

    private String findPolicyIdInCurrentConf(String alertPolicyName) {
        for (PolicyWithTeamInfo currentPolicyWithTeamInfo : currentConfigs) {
            if (currentPolicyWithTeamInfo.getPolicy().getName().equals(alertPolicyName)) {
                return currentPolicyWithTeamInfo.getPolicy().getId();
            }
        }
        return null;
    }

    @Override
    protected void updateEntityOrders() {
        try {
            String entityJson = BackupUtils.readFile(rootPath + "/ordersV2/PolicyOrders.json");
            this.policyOrderConfigFromFile = BackupUtils.readWithTypeReference(entityJson);
        } catch (Exception e) {
            logger.error("Could not read policy V2 orders from file: " + e.getMessage());
            return;
        }
        List<PolicyConfig> currentOrderConfigs;
        currentOrderConfigs = new PolicyOrderRetriever().retrieveEntities();


        if (equalsIgnoreOrder(currentOrderConfigs, this.policyOrderConfigFromFile)) {
            return;
        }
        int size = currentOrderConfigs.size();
        for (PolicyConfig config : policyOrderConfigFromFile) {
            ChangePolicyOrderRequest params = new ChangePolicyOrderRequest();
            params.setPolicyId(getCurrentPolicyId(config.getId(), config.getName(), currentOrderConfigs));
            if (params.getPolicyId() == null) {
                continue;
            }
            ChangePolicyOrderPayload body = new ChangePolicyOrderPayload();
            body.setTargetIndex(size + config.getOrder());
            params.setBody(body);
            params.setTeamId(config.getTeam());
            api.changePolicyOrder(params);
        }
    }

    private static boolean equalsIgnoreOrder(Collection<?> a, Collection<?> b) {
        return a == b || a != null && b != null && a.size() == b.size() && a.containsAll(b);
    }

    private String getCurrentPolicyId(String id, String name, List<PolicyConfig> currentOrderConfigs) {
        for (PolicyConfig policyConfig : currentOrderConfigs) {
            if (policyConfig.getId().equals(id)) {
                return id;
            } else if (policyConfig.getName().equals(name)) {
                return policyConfig.getId();
            }
        }
        return null;
    }

    @Override
    protected String getEntityIdentifierName(PolicyWithTeamInfo entity) {
        return "Policy " + entity.getPolicy().getName();
    }

    @Override
    protected String getImportDirectoryName() {
        return "policiesV2";
    }

    @Override
    protected PolicyWithTeamInfo readEntity(String fileName) {
        try {
            String alertPolicyJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            return readJson(alertPolicyJson);
        } catch (Exception e) {
            logger.error("Could not read policy from file:" + fileName);
            return null;
        }
    }

    private PolicyWithTeamInfo readJson(String policyJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(policyJson, PolicyWithTeamInfo.class);
    }
}
