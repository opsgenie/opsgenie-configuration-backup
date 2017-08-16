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
import java.util.Collections;
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
    protected BeanStatus checkEntities(NotificationRule oldEntity, NotificationRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            oldEntity.setOrder(0);
            currentEntity.setOrder(0);
            sortNotifyBeforeList(oldEntity);
            sortNotifyBeforeList(currentEntity);
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }
        return BeanStatus.NOT_EXIST;
    }

    private void sortNotifyBeforeList(NotificationRule entity) {
        if (entity.getNotificationTime() != null && entity.getNotificationTime().size() > 0) {
            Collections.sort(entity.getNotificationTime());
        }
    }

    @Override
    protected NotificationRule getBean() {
        return new NotificationRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "notifications";
    }

    public void restore() throws RestoreException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.error("Error : " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        File[] fileList = getImportDirectory().listFiles();
        if (fileList == null || fileList.length == 0) {
            logger.error("Error : " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        List<User> userList = retrieveUserList();

        for (File notificationDirectory : fileList) {
            User user = findUser(notificationDirectory, userList);
            if (user != null) {
                importNotificationsForUser(user, notificationDirectory);
            }
        }

        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    private User findUser(File notificationDirectory, List<User> userList) {
        if (!notificationDirectory.exists() || !notificationDirectory.isDirectory()) {
            return null;
        }
        for (User user : userList) {
            if (user.getUsername().equals(notificationDirectory.getName())) {
                return user;
            }
        }
        return null;
    }

    private void importNotificationsForUser(User user, File notificationDirectory) {
        username = user.getUsername();
        List<NotificationRule> backups = new ArrayList<NotificationRule>();
        String[] files = BackupUtils.getFileListOf(notificationDirectory);
        for (String fileName : files) {
            NotificationRule bean = readEntity(notificationDirectory.getName() + "/" + fileName);
            if (bean != null) {
                backups.add(bean);
            }
        }
        try {
            importEntities(backups, retrieveEntities());
        } catch (Exception e) {
            logger.error("Error at restoring " + getImportDirectoryName() + " for user " + username, e);
        }
    }

    private List<User> retrieveUserList() throws RestoreException {
        try {
            ListUsersRequest listUsersRequest = new ListUsersRequest();
            return userApi.listUsers(listUsersRequest).getData();
        } catch (Exception e) {
            throw new RestoreException("Error at listing users for notification rules", e);
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

        String id = notificationRuleApi.createNotificationRule(request).getData().getId();

        for (NotificationRuleStep step : bean.getSteps()) {
            addNotificationRuleStep(id, step);
        }
    }

    private List<CreateNotificationRuleStepPayload> constructCreateNotificationRuleStepPayloadList(NotificationRule bean){
        List<CreateNotificationRuleStepPayload> createNotificationRuleStepPayloadList = new ArrayList<CreateNotificationRuleStepPayload>();

        for (NotificationRuleStep notificationRuleStep: bean.getSteps()) {
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
        importNotificationRuleSteps(bean);

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
    protected List<NotificationRule> retrieveEntities() throws ApiException {
        List<NotificationRuleMeta> metaList = notificationRuleApi.listNotificationRules(username).getData();
        List<NotificationRule> notificationRuleList = new ArrayList<NotificationRule>();

        for (NotificationRuleMeta meta:metaList){
            GetNotificationRuleRequest getRequest = new GetNotificationRuleRequest().identifier(username).ruleId(meta.getId());
            GetNotificationRuleResponse getNotificationRuleResponse = notificationRuleApi.getNotificationRule(getRequest);
            notificationRuleList.add(getNotificationRuleResponse.getData());
        }

        return notificationRuleList;
    }


    @Override
    protected String getEntityIdentifierName(NotificationRule bean) {
        return "Notification " + bean.getName() + " for user " + username;
    }
}
