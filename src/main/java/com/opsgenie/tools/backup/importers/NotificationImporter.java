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
import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.BackupUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class imports Notifications from local directory called notifications to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class NotificationImporter extends BaseImporter<NotificationRule> {
    private final Logger logger = LogManager.getLogger(NotificationImporter.class);
    private String username;

    public NotificationImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(NotificationRule oldEntity, NotificationRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected NotificationRule getBean() throws IOException, ParseException {
        return new NotificationRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "notifications";
    }

    public void restore() {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");
        try {
            ListUsersRequest listUsersRequest = new ListUsersRequest();
            List<User> userList = getOpsGenieClient().user().listUsers(listUsersRequest).getUsers();
            File[] fileList = getImportDirectory().listFiles();
            for (File notificationDirectory : fileList) {
                if (notificationDirectory.exists() && notificationDirectory.isDirectory()) {
                    for (User user : userList) {
                        if (user.getUsername().equals(notificationDirectory.getName())) {
                            username = user.getUsername();
                            List<NotificationRule> backups = new ArrayList<NotificationRule>();
                            String[] files = BackupUtils.getFileListOf(notificationDirectory);
                            for (String fileName : files) {
                                try {
                                    String beanJson = BackupUtils.readFileAsJson(notificationDirectory.getAbsolutePath() + "/" + fileName);
                                    NotificationRule bean = getBean();
                                    JsonUtils.fromJson(bean, beanJson);
                                    backups.add(bean);
                                } catch (Exception e) {
                                    logger.error("Error at reading notification rule for user " + username + " file name " + fileName, e);
                                }
                            }
                            try {
                                importEntities(backups, retrieveEntities());
                            } catch (Exception e) {
                                logger.error("Error at restoring " + getImportDirectoryName() + " for user " + username, e);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error at restoring " + getImportDirectoryName(), e);

        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
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
        GetNotificationRuleRequest getNotificationRuleRequest = new GetNotificationRuleRequest();
        getNotificationRuleRequest.setUsername(username);
        getNotificationRuleRequest.setId(notificationRule.getId());
        NotificationRule current = getOpsGenieClient().notificationRule().getNotificationRule(getNotificationRuleRequest).getNotificationRule();
        List<NotificationRuleStep> stepList = notificationRule.getSteps();
        if (stepList != null && stepList.size() > 0) {
            for (NotificationRuleStep backupStep : stepList) {
                boolean notExist = true;
                for (NotificationRuleStep currentStep : current.getSteps()) {
                    if (backupStep.getId().equals(currentStep.getId())) {
                        notExist = false;
                        if (!backupStep.toString().equals(currentStep.toString()))
                            updateNotificationRuleStep(notificationRule.getId(), backupStep);
                    }
                    if (notExist)
                        addNotificationRuleStep(notificationRule.getId(), backupStep);
                }
            }
        }

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
