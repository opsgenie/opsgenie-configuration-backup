package com.opsgenie.tools.backup.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.oas.sdk.api.DeprecatedPolicyApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import com.opsgenie.tools.backup.retrieval.DeprecatedPolicyRetriever;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class DeprecatedPolicyImporter extends BaseImporter<DeprecatedAlertPolicy> {

    private static DeprecatedPolicyApi api = new DeprecatedPolicyApi();
    private String rootPath;
    private List<PolicyConfig> policyOrderConfig = new ArrayList<PolicyConfig>();

    public DeprecatedPolicyImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
        this.rootPath = backupRootDirectory;
    }

    @Override
    protected DeprecatedAlertPolicy readEntity(String fileName) {
        try {
            String alertPolicyJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            return readJson(alertPolicyJson);
        } catch (Exception e) {
            logger.error("Could not read policy from file:" + fileName);
            return null;
        }
    }

    @Override
    protected EntityRetriever<DeprecatedAlertPolicy> initializeEntityRetriever() {
        return new DeprecatedPolicyRetriever();
    }

    @Override
    protected EntityStatus checkEntity(DeprecatedAlertPolicy entity) {
        for (DeprecatedAlertPolicy policy : currentConfigs) {
            if (policy.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (policy.getName().equals(entity.getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    private DeprecatedAlertPolicy readJson(String alertPolicyJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.setDateFormat(sdf);
        return mapper.readValue(alertPolicyJson, DeprecatedAlertPolicy.class);
    }

    @Override
    protected String getImportDirectoryName() {
        return "policies";
    }

    @Override
    protected void createEntity(final DeprecatedAlertPolicy entity) throws Exception {
        entity.setId(null);
        RetryPolicyAdapter.invoke(new Callable<DeprecatedCreateAlertPolicyResponse>() {
            @Override
            public DeprecatedCreateAlertPolicyResponse call() throws Exception {
                return api.createAlertPolicy(entity);
            }
        });

    }

    @Override
    protected void updateEntity(DeprecatedAlertPolicy alertPolicy, EntityStatus entityStatus) throws Exception {
        final UpdateAlertPolicyRequest request = new UpdateAlertPolicyRequest();
        request.setBody(alertPolicy);
        final String id = alertPolicy.getId();
        alertPolicy.setId(null);
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setPolicyId(id);
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setPolicyId(findPolicyIdInCurrentConf(alertPolicy.getName()));
        }
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.updateAlertPolicy(request);
            }
        });

    }

    private String findPolicyIdInCurrentConf(String alertPolicyName) {
        for (DeprecatedAlertPolicy currentPolicy : currentConfigs) {
            if (currentPolicy.getName().equals(alertPolicyName)) {
                return currentPolicy.getId();
            }
        }
        return null;
    }

    @Override
    protected String getEntityIdentifierName(DeprecatedAlertPolicy alertPolicy) {
        return "Policy " + alertPolicy.getName();
    }

    @Override
    protected void updateEntityOrders() {
        try {
            String entityJson = BackupUtils.readFile(rootPath + "/orders/PolicyOrders.json");
            this.policyOrderConfig = BackupUtils.readWithTypeReference(entityJson);
        } catch (Exception e) {
            logger.error("Could not read policy orders from file: " + e.getMessage());
            return;
        }
        try {
            List<DeprecatedAlertPolicyMeta> list = new DeprecatedPolicyRetriever().retrievePolicyMetaList();

            List<PolicyConfig> configs = new ArrayList<PolicyConfig>();
            for (DeprecatedAlertPolicyMeta meta : list) {
                configs.add(new PolicyConfig().setId(meta.getId()).setName(meta.getName()).setOrder(meta.getOrder()));
            }
            if (equalsIgnoreOrder(configs, this.policyOrderConfig)) {
                return;
            }
            int size = this.currentConfigs.size();
            for (PolicyConfig config : policyOrderConfig) {
                final ChangeAlertPolicyOrderRequest params = new ChangeAlertPolicyOrderRequest();
                params.setPolicyId(getCurrentPolicyId(config.getId(), config.getName()));
                if (params.getPolicyId() == null) {
                    continue;
                }
                DeprecatedChangeAlertPolicyOrderPayload body = new DeprecatedChangeAlertPolicyOrderPayload();
                body.setTargetIndex(size + config.getOrder());
                params.setBody(body);
                RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
                    @Override
                    public SuccessResponse call() throws Exception {
                        return api.changeAlertPolicyOrder(params);
                    }
                });

            }
        } catch (Exception e) {
            logger.error("Could not read policy orders " + e.getMessage());
        }
    }

    private static boolean equalsIgnoreOrder(Collection<?> a, Collection<?> b) {
        return a == b || a != null && b != null && a.size() == b.size() && a.containsAll(b);
    }

    private String getCurrentPolicyId(String id, String name) {
        for (DeprecatedAlertPolicy policy : currentConfigs) {
            if (policy.getId().equals(id)) {
                return id;
            } else if (policy.getName().equals(name)) {
                return policy.getId();
            }
        }
        return null;
    }

    @Override
    protected void updateTeamIds(DeprecatedAlertPolicy entity) {}
}
