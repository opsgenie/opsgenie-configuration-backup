package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.NotificationRule;
import com.ifountain.opsgenie.client.model.beans.NotificationRuleStep;
import com.ifountain.opsgenie.client.model.beans.User;
import com.ifountain.opsgenie.client.model.notification_rule.AddNotificationRuleRequest;
import com.ifountain.opsgenie.client.model.notification_rule.AddNotificationRuleStepRequest;
import com.ifountain.opsgenie.client.model.notification_rule.GetNotificationRuleRequest;
import com.ifountain.opsgenie.client.model.notification_rule.ListNotificationRulesRequest;
import com.ifountain.opsgenie.client.model.notification_rule.UpdateNotificationRuleRequest;
import com.ifountain.opsgenie.client.model.notification_rule.UpdateNotificationRuleStepRequest;
import com.ifountain.opsgenie.client.model.user.ListUsersRequest;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class imports Notifications from local directory called notifications to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class UserNotificationImporter extends BaseImporter<NotificationRule> {
    private final Logger logger = LogManager.getLogger(UserNotificationImporter.class);
    private String username;

    public UserNotificationImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(NotificationRule oldEntity, NotificationRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            oldEntity.setApplyOrder(0);
            currentEntity.setApplyOrder(0);
            sortNotifyBeforeList(oldEntity);
            sortNotifyBeforeList(currentEntity);
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }
        return BeanStatus.NOT_EXIST;
    }

    private void sortNotifyBeforeList(NotificationRule entity) {
        if (entity.getNotifyBefore() != null && entity.getNotifyBefore().size() > 0) {
            Collections.sort(entity.getNotifyBefore());
        }
    }

    @Override
    protected NotificationRule getBean() throws IOException, ParseException {
        return new NotificationRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "notifications";
    }

    public void restore() throws RestoreException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.error("Error : " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipeed");
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
            return getOpsGenieClient().user().listUsers(listUsersRequest).getUsers();
        } catch (Exception e) {
            throw new RestoreException("Error at listing users for notification rules", e);
        }

    }

    @Override
    protected void addBean(NotificationRule bean) throws ParseException, OpsGenieClientException, IOException {
        AddNotificationRuleRequest request = new AddNotificationRuleRequest();
        request.setUsername(username);
        request.setName(bean.getName());
        request.setActionType(bean.getActionType());
        request.setApplyOrder(bean.getApplyOrder());
        request.setConditionMatchType(bean.getConditionMatchType());
        request.setConditions(bean.getConditions());
        request.setRestrictions(bean.getRestrictions());
        request.setNotifyBefore(bean.getNotifyBefore());
        request.setSchedules(bean.getSchedules());
        String id = getOpsGenieClient().notificationRule().addNotificationRule(request).getId();
        for (NotificationRuleStep step : bean.getSteps()) {
            addNotificationRuleStep(id, step);
        }
    }

    @Override
    protected void updateBean(NotificationRule bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setUsername(username);
        request.setName(bean.getName());
        request.setId(bean.getId());
        request.setSchedules(bean.getSchedules());
        request.setNotifyBefore(bean.getNotifyBefore());
        request.setActionType(bean.getActionType());
        request.setConditionMatchType(bean.getConditionMatchType());
        request.setConditions(bean.getConditions());
        request.setRestrictions(bean.getRestrictions());
        getOpsGenieClient().notificationRule().updateNotificationRule(request).getId();
        importNotificationRuleSteps(bean);

    }

    private void importNotificationRuleSteps(NotificationRule notificationRule) throws ParseException, OpsGenieClientException, IOException {
        List<NotificationRuleStep> stepList = notificationRule.getSteps();
        if (stepList == null || stepList.size() == 0) {
            return;
        }
        GetNotificationRuleRequest getNotificationRuleRequest = new GetNotificationRuleRequest();
        getNotificationRuleRequest.setUsername(username);
        getNotificationRuleRequest.setId(notificationRule.getId());
        NotificationRule current = getOpsGenieClient().notificationRule().getNotificationRule(getNotificationRuleRequest).getNotificationRule();
        for (NotificationRuleStep backupStep : stepList) {
            importSingleNotificationRuleStep(notificationRule.getId(), current, backupStep);
        }

    }

    private void importSingleNotificationRuleStep(String notificationRuleId, NotificationRule current, NotificationRuleStep backupStep) throws ParseException, OpsGenieClientException, IOException {
        for (NotificationRuleStep currentStep : current.getSteps()) {
            if (backupStep.getId().equals(currentStep.getId())) {
                if (!backupStep.toString().equals(currentStep.toString()))
                    updateNotificationRuleStep(notificationRuleId, backupStep);
                return;
            }
        }
        addNotificationRuleStep(notificationRuleId, backupStep);
    }

    private void addNotificationRuleStep(String ruleId, NotificationRuleStep step) throws ParseException, OpsGenieClientException, IOException {
        AddNotificationRuleStepRequest request = new AddNotificationRuleStepRequest();
        request.setUsername(username);
        request.setRuleId(ruleId);
        request.setMethod(step.getMethod());
        request.setTo(step.getTo());
        request.setSendAfter(step.getSendAfter());
        getOpsGenieClient().notificationRule().addNotificationRuleStep(request);
    }

    private void updateNotificationRuleStep(String ruleId, NotificationRuleStep step) throws ParseException, OpsGenieClientException, IOException {
        UpdateNotificationRuleStepRequest request = new UpdateNotificationRuleStepRequest();
        request.setUsername(username);
        request.setRuleId(ruleId);
        request.setMethod(step.getMethod());
        request.setTo(step.getTo());
        request.setSendAfter(step.getSendAfter());
        request.setId(step.getId());
        getOpsGenieClient().notificationRule().updateNotificationRuleStep(request);
    }


    @Override
    protected List<NotificationRule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListNotificationRulesRequest listNotificationRulesRequest = new ListNotificationRulesRequest();
        listNotificationRulesRequest.setUsername(username);
        return getOpsGenieClient().notificationRule().listNotificationRule(listNotificationRulesRequest).getRules();
    }


    @Override
    protected String getEntityIdentifierName(NotificationRule bean) {
        return "Notification " + bean.getName() + " for user " + username;
    }
}
