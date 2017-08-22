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
    protected NotificationRule getNewInstance() {
        return new NotificationRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "notifications";
    }

    public void restore() {
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
    protected NotificationRule checkEntityWithId(NotificationRule entity) throws ApiException {
        return notificationRuleApi.getNotificationRule(new GetNotificationRuleRequest().identifier(username).ruleId(entity.getId())).getData();
    }

    private User findUser(File notificationDirectory) throws ApiException {
        return userApi.getUser(new GetUserRequest().identifier(notificationDirectory.getName())).getData();
    }

    private void importNotificationsForUser(User user, File notificationDirectory) throws ApiException {
        username = user.getUsername();
        String[] files = BackupUtils.getFileListOf(notificationDirectory);
        for (String fileName : files) {
            NotificationRule rule = readEntity(notificationDirectory.getName() + "/" + fileName);
            if (rule != null) {
                importEntity(rule);
            }
        }
    }

    @Override
    protected void createEntity(NotificationRule entity) throws ApiException {
        CreateNotificationRulePayload payload = new CreateNotificationRulePayload();
        payload.setActionType(entity.getActionType());
        payload.setCriteria(entity.getCriteria());
        payload.setEnabled(entity.isEnabled());
        payload.setName(entity.getName());
        payload.setNotificationTime(entity.getNotificationTime());
        payload.setOrder(entity.getOrder());
        payload.setRepeat(entity.getRepeat());
        payload.setSchedules(entity.getSchedules());
        payload.setTimeRestriction(entity.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(entity));

        CreateNotificationRuleRequest request = new CreateNotificationRuleRequest();
        request.setBody(payload);
        request.setIdentifier(username);

        notificationRuleApi.createNotificationRule(request).getData().getId();
    }

    private List<CreateNotificationRuleStepPayload> constructCreateNotificationRuleStepPayloadList(NotificationRule notificationRule) {
        List<CreateNotificationRuleStepPayload> createNotificationRuleStepPayloadList = new ArrayList<CreateNotificationRuleStepPayload>();

        for (NotificationRuleStep notificationRuleStep : notificationRule.getSteps()) {
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
    protected void updateEntity(NotificationRule entity, EntityStatus entityStatus) throws ApiException {
        UpdateNotificationRulePayload payload = new UpdateNotificationRulePayload();
        payload.setCriteria(entity.getCriteria());
        payload.setEnabled(entity.isEnabled());
        payload.setName(entity.getName());
        payload.setNotificationTime(entity.getNotificationTime());
        payload.setOrder(entity.getOrder());
        payload.setRepeat(entity.getRepeat());
        payload.setSchedules(entity.getSchedules());
        payload.setTimeRestriction(entity.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(entity));

        UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setRuleId(entity.getId());
        request.setIdentifier(username);
        request.setBody(payload);

        notificationRuleApi.updateNotificationRule(request);
    }

    @Override
    protected String getEntityIdentifierName(NotificationRule notificationRule) {
        return "Notification " + notificationRule.getName() + " for user " + username;
    }
}
