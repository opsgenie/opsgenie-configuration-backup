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
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;
import org.eclipse.jgit.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 10:54
 */
public class PolicyImporter extends BaseImporter<PolicyWithTeamInfo> {

    private static PolicyApi api = new PolicyApi();
    private String rootPath;
    private RateLimitManager rateLimitManager;
    private List<PolicyConfig> policyOrderConfigFromFile = new ArrayList<PolicyConfig>();

    public PolicyImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
        this.rootPath = backupRootDirectory;
        this.rateLimitManager = rateLimitManager;
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
    protected void createEntity(final PolicyWithTeamInfo entity) throws Exception {
        final Policy policy = entity.getPolicy();
        policy.setId(null);
        RetryPolicyAdapter.invoke(new Callable<CreatePolicyResponse>() {
            @Override
            public CreatePolicyResponse call() throws Exception {
                return api.createPolicy(policy, entity.getTeamId());
            }
        });

    }

    @Override
    protected void updateEntity(PolicyWithTeamInfo entity, EntityStatus entityStatus) throws Exception {
        Policy policy = entity.getPolicy();
        final UpdatePolicyRequest request = new UpdatePolicyRequest();
        request.setBody(policy);
        request.setTeamId(entity.getTeamId());

        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setPolicyId(policy.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setPolicyId(findPolicyIdInCurrentConf(policy.getName()));
        }
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.updatePolicy(request);
            }
        });

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
    protected void updateEntityOrders() throws Exception {
        try {
            String entityJson = BackupUtils.readFile(rootPath + "/ordersV2/PolicyOrders.json");
            this.policyOrderConfigFromFile = BackupUtils.readWithTypeReference(entityJson);
        } catch (Exception e) {
            logger.error("Could not read policy V2 orders from file: " + e.getMessage());
            return;
        }
        List<PolicyConfig> currentOrderConfigs;
        Map<String, String> teamIdMap;
        try {
            currentOrderConfigs = new PolicyOrderRetriever().retrieveEntities();
            teamIdMap = new TeamIdMapper(rateLimitManager).getTeamIdMap();
            if (equalsIgnoreOrder(currentOrderConfigs, this.policyOrderConfigFromFile)) {
                return;
            }
            int size = currentOrderConfigs.size();
            for (PolicyConfig config : policyOrderConfigFromFile) {
                final ChangePolicyOrderRequest params = new ChangePolicyOrderRequest();
                params.setPolicyId(getCurrentPolicyId(config.getId(), config.getName(), currentOrderConfigs));
                if (params.getPolicyId() == null) {
                    continue;
                }
                ChangePolicyOrderPayload body = new ChangePolicyOrderPayload();
                body.setTargetIndex(size + config.getOrder());
                params.setBody(body);
                String teamName = oldTeamIdMap.get(config.getTeam());
                params.setTeamId(teamIdMap.get(teamName));
                RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
                    @Override
                    public SuccessResponse call() throws Exception {
                        return api.changePolicyOrder(params);
                    }
                });

            }
        } catch (Exception e) {
            logger.error("Could not read policy V2 orders" + e.getMessage());
        }
    }

    @Override
    protected void updateTeamIds(PolicyWithTeamInfo entity) throws Exception {
        Map<String, String> teamIdMap = new TeamIdMapper(rateLimitManager).getTeamIdMap();

        if(entity.getTeamId() != null) {
            String teamName = entity.getTeamName();

            if(entity.getPolicy().getClass() == AlertPolicy.class) {
                AlertPolicy alertPolicy = (AlertPolicy) entity.getPolicy();
                for(Responder responder : alertPolicy.getResponders()) {
                    if(responder.getType() == Responder.TypeEnum.TEAM) {
                        File teamsDirectory = new File(backupRootDirectory + "/teams/");
                        String responderTeamName = BackupUtils.getTeamNameFromId(teamsDirectory, responder.getId());
                        if (!StringUtils.isEmptyOrNull(responderTeamName)){
                            responder.setId(teamIdMap.get(responderTeamName));
                        }
                        else {
                            logger.info("Could not find team name for team Id {} in the backup folder", responder.getId());
                        }
                    }
                }
            }

            entity.setTeamId(teamIdMap.get(teamName));
            entity.getPolicy().setTeamId(teamIdMap.get(teamName));
        }

        else {
            AlertPolicy alertPolicy = (AlertPolicy) entity.getPolicy();

            for(Responder responder : alertPolicy.getResponders()) {
                String responderId = responder.getId();
                String teamName;
                if((teamName = oldTeamIdMap.get(responderId)) != null) {
                    responder.setId(teamIdMap.get(teamName));
                }
            }
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
