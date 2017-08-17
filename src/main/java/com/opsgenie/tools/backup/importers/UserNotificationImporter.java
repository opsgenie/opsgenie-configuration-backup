package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.NotificationRuleApi;
import com.opsgenie.client.api.NotificationRuleStepApi;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserNotificationImporter extends BaseImporter<NotificationRule> {

    private static UserApi userApi = new UserApi();
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();
    private static NotificationRuleStepApi notificationRuleStepApi = new NotificationRuleStepApi();

    private final Logger logger = LogManager.getLogger(UserNotificationImporter.class);
    private String username;

    public UserNotificationImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
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
        for (String fileName : files) {
            NotificationRule bean = readEntity(notificationDirectory.getName() + "/" + fileName);
            if (bean != null) {
                importEntity(bean);
            }
        }
    }

    @Override
    protected void addBean(NotificationRule bean) throws ApiException {
        CreateNotificationRulePayload payload = new CreateNotificationRulePayload();
        payload.setActionType(bean.getActionType());
        payload.setCriteria(bean.getCriteria().type(null));
        payload.setEnabled(bean.isEnabled());
        payload.setName(bean.getName());
        payload.setNotificationTime(bean.getNotificationTime());
        payload.setOrder(bean.getOrder());
        payload.setRepeat(bean.getRepeat());
        payload.setSchedules(bean.getSchedules());
        payload.setTimeRestriction(bean.getTimeRestriction().type(null));
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(bean));

        CreateNotificationRuleRequest request = new CreateNotificationRuleRequest();
        request.setBody(payload);
        request.setIdentifier(username);

        String id = notificationRuleApi.createNotificationRule(request).getData().getId();

        for (NotificationRuleStep step : bean.getSteps()) {
            addNotificationRuleStep(id, step);
        }
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
        payload.setCriteria(bean.getCriteria().type(null));
        payload.setEnabled(bean.isEnabled());
        payload.setName(bean.getName());
        payload.setNotificationTime(bean.getNotificationTime());
        payload.setOrder(bean.getOrder());
        payload.setRepeat(bean.getRepeat());
        payload.setSchedules(bean.getSchedules());
        payload.setTimeRestriction(bean.getTimeRestriction().type(null));
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(bean));

        UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setRuleId(bean.getId());
        request.setIdentifier(username);
        request.setBody(payload);

        notificationRuleApi.updateNotificationRule(request);
        //importNotificationRuleSteps(bean);

    }

    private void importNotificationRuleSteps(NotificationRule notificationRule) throws ApiException {
        List<NotificationRuleStep> stepList = notificationRule.getSteps();
        if (stepList == null || stepList.size() == 0) {
            return;
        }
        GetNotificationRuleRequest getNotificationRuleRequest = new GetNotificationRuleRequest();
        getNotificationRuleRequest.setIdentifier(username);
        getNotificationRuleRequest.setRuleId(notificationRule.getId());
        NotificationRule current = notificationRuleApi.getNotificationRule(getNotificationRuleRequest).getData();
        for (NotificationRuleStep backupStep : stepList) {
            importSingleNotificationRuleStep(notificationRule.getId(), current, backupStep);
        }

    }

    private void importSingleNotificationRuleStep(String notificationRuleId, NotificationRule current, NotificationRuleStep backupStep) throws ApiException {
        for (NotificationRuleStep currentStep : current.getSteps()) {
            if (backupStep.getId().equals(currentStep.getId())) {
                if (!backupStep.toString().equals(currentStep.toString()))
                    updateNotificationRuleStep(notificationRuleId, backupStep);
                return;
            }
        }
        addNotificationRuleStep(notificationRuleId, backupStep);
    }

   private void addNotificationRuleStep(String ruleId, NotificationRuleStep step) throws ApiException {
        CreateNotificationRuleStepPayload payload = new CreateNotificationRuleStepPayload();
        payload.setContact(step.getContact());
        payload.setEnabled(step.isEnabled());
        payload.setSendAfter(step.getSendAfter());

        CreateNotificationRuleStepRequest request = new CreateNotificationRuleStepRequest();
        request.setRuleId(ruleId);
        request.setIdentifier(step.getId());
        request.setBody(payload);

        notificationRuleStepApi.createNotificationRuleStep(request);
    }

    private void updateNotificationRuleStep(String ruleId, NotificationRuleStep step) throws ApiException {
        UpdateNotificationRuleStepPayload payload = new UpdateNotificationRuleStepPayload();
        payload.setContact(step.getContact());
        payload.setEnabled(step.isEnabled());
        payload.setSendAfter(step.getSendAfter());

        UpdateNotificationRuleStepRequest request = new UpdateNotificationRuleStepRequest();
        request.setBody(payload);
        request.setId(step.getId());
        request.setRuleId(ruleId);
        request.setIdentifier(username);
        notificationRuleStepApi.updateNotificationRuleStep(request);
    }

    @Override
    protected String getEntityIdentifierName(NotificationRule bean) {
        return "Notification " + bean.getName() + " for user " + username;
    }
}
