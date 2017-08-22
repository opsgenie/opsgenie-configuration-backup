package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.NotificationRuleApi;
import com.opsgenie.client.api.NotificationRuleStepApi;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserNotificationRuleImporter extends BaseImporter<NotificationRule> {

    private static UserApi userApi = new UserApi();
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();
    private static NotificationRuleStepApi notificationRuleStepApi = new NotificationRuleStepApi();

    private String username;

    public UserNotificationRuleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected NotificationRule getBean() {
        return new NotificationRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "notifications";
    }

    public void restore() throws RestoreException, ApiException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.warn("Warning: " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        File[] fileList = getImportDirectory().listFiles();
        if (fileList == null || fileList.length == 0) {
            logger.warn("Warning : " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        for (File notificationDirectory : fileList) {
            User user = findUser(notificationDirectory);
            if (user != null) {
                importNotificationsForUser(user, notificationDirectory);
            }
        }

        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    @Override
    protected void getEntityWithId(NotificationRule entity) throws ApiException {
        notificationRuleApi.getNotificationRule(new GetNotificationRuleRequest().identifier(username).ruleId(entity.getId())).getData();
    }

    private User findUser(File notificationDirectory) throws ApiException {
        return userApi.getUser(new GetUserRequest().identifier(notificationDirectory.getName())).getData();
    }

    private void importNotificationsForUser(User user, File notificationDirectory) throws ApiException {
        username = user.getUsername();
        String[] files = BackupUtils.getFileListOf(notificationDirectory);

        List<NotificationRule> notificationRules = this.listNotificationRulesWithoutId();

        for (String fileName : files) {
            NotificationRule entity = readEntity(notificationDirectory.getName() + "/" + fileName);
            NotificationRule cloneEntity = readEntity(notificationDirectory.getName() + "/" + fileName);
            unsetIds(cloneEntity);

            if (entity != null && !notificationRules.contains(cloneEntity)) {
                importEntity(entity);
            }
        }
    }

    private List<NotificationRule> listNotificationRulesWithoutId() throws ApiException {
        ListNotificationRulesResponse listResponse = notificationRuleApi.listNotificationRules(username);
        List<NotificationRuleMeta> ruleMetaList = listResponse.getData();

        List<NotificationRule> result = new ArrayList<NotificationRule>();

        for (NotificationRuleMeta meta : ruleMetaList) {
            GetNotificationRuleRequest request = new GetNotificationRuleRequest()
                    .identifier(username)
                    .ruleId(meta.getId());

            GetNotificationRuleResponse response = notificationRuleApi.getNotificationRule(request);

            NotificationRule notificationRule = response.getData();
            unsetIds(notificationRule);

            result.add(notificationRule);
        }

        return result;
    }

    private void unsetIds(NotificationRule notificationRule) {
        notificationRule.setId(null);

        for (NotificationRuleStep step : notificationRule.getSteps()) {
            step.setId(null);
            step.getParent().setId(null);
        }
    }

    @Override
    protected void addBean(NotificationRule bean) throws ApiException {
        CreateNotificationRulePayload payload = new CreateNotificationRulePayload();
        payload.setActionType(bean.getActionType());
        payload.setCriteria(bean.getCriteria());
        payload.setEnabled(bean.isEnabled());
        payload.setName(bean.getName());
        payload.setNotificationTime(bean.getNotificationTime());
        payload.setOrder(bean.getOrder());
        payload.setRepeat(bean.getRepeat());
        payload.setSchedules(bean.getSchedules());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(bean));

        CreateNotificationRuleRequest request = new CreateNotificationRuleRequest();
        request.setBody(payload);
        request.setIdentifier(username);

        notificationRuleApi.createNotificationRule(request).getData().getId();
    }

    private List<CreateNotificationRuleStepPayload> constructCreateNotificationRuleStepPayloadList(NotificationRule bean) {
        List<CreateNotificationRuleStepPayload> createNotificationRuleStepPayloadList = new ArrayList<CreateNotificationRuleStepPayload>();

        for (NotificationRuleStep notificationRuleStep : bean.getSteps()) {
            createNotificationRuleStepPayloadList.add(
                    new CreateNotificationRuleStepPayload()
                            .contact(notificationRuleStep.getContact())
                            .enabled(notificationRuleStep.isEnabled())
                            .sendAfter(notificationRuleStep.getSendAfter())
            );
        }

        return createNotificationRuleStepPayloadList;
    }

    @Override
    protected void updateBean(NotificationRule bean) throws ApiException {
        UpdateNotificationRulePayload payload = new UpdateNotificationRulePayload();
        payload.setCriteria(bean.getCriteria());
        payload.setEnabled(bean.isEnabled());
        payload.setName(bean.getName());
        payload.setNotificationTime(bean.getNotificationTime());
        payload.setOrder(bean.getOrder());
        payload.setRepeat(bean.getRepeat());
        payload.setSchedules(bean.getSchedules());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(bean));

        UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setRuleId(bean.getId());
        request.setIdentifier(username);
        request.setBody(payload);

        notificationRuleApi.updateNotificationRule(request);
    }

    @Override
    protected String getEntityIdentifierName(NotificationRule bean) {
        return "Notification " + bean.getName() + " for user " + username;
    }
}
