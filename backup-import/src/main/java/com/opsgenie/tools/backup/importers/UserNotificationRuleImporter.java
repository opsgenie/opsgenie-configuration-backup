package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.NotificationRuleApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.UserConfig;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class UserNotificationRuleImporter extends UserImporter {
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();

    public UserNotificationRuleImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, rateLimitManager, addEntity, updateEntity);
    }

    @Override
    protected void createEntity(UserConfig userConfig) throws Exception {

    }

   @Override
   protected void updateEntity(UserConfig userConfig, EntityStatus entityStatus) throws Exception {
       final List<NotificationRule> notificationRuleList = userConfig.getNotificationRuleList();
       compareNotificationRules(userConfig.getUser(), notificationRuleList);
   }

    private void compareNotificationRules(User user, List<NotificationRule> notificationRuleList) throws ApiException {
        if (notificationRuleList != null) {
            logger.info("Updating notification rules for " + user.getUsername());
            for (NotificationRule notificationRule : notificationRuleList) {
                final String ruleIdByName = findRuleIdByName(user, notificationRule);
                try {
                    if (ruleIdByName != null) {
                        updateNotificationRule(user, notificationRule);
                    } else {
                        createNotificationRule(user, notificationRule);
                    }
                } catch (Exception e) {
                    logger.error("Could not update notification rule for user: " + user.getUsername() + ", " + e.getMessage());
                }
            }
        }
    }

    private void updateNotificationRule(User user, NotificationRule notificationRule) throws Exception {
        UpdateNotificationRulePayload payload = new UpdateNotificationRulePayload();
        payload.setCriteria(notificationRule.getCriteria());
        payload.setEnabled(notificationRule.isEnabled());
        payload.setName(notificationRule.getName());
        payload.setNotificationTime(notificationRule.getNotificationTime());
        payload.setOrder(notificationRule.getOrder());
        payload.setRepeat(notificationRule.getRepeat());
        payload.setSchedules(notificationRule.getSchedules());
        payload.setTimeRestriction(notificationRule.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(notificationRule));

        final UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setRuleId(findRuleIdByName(user, notificationRule));
        request.setIdentifier(user.getUsername());
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<UpdateNotificationRuleResponse>() {
            @Override
            public UpdateNotificationRuleResponse call() throws Exception {
                return notificationRuleApi.updateNotificationRule(request);
            }
        });

    }

    private void createNotificationRule(User user, NotificationRule notificationRule) throws Exception {
        CreateNotificationRulePayload payload = new CreateNotificationRulePayload();
        payload.setActionType(notificationRule.getActionType());
        payload.setCriteria(notificationRule.getCriteria());
        payload.setEnabled(notificationRule.isEnabled());
        payload.setName(notificationRule.getName());
        payload.setNotificationTime(notificationRule.getNotificationTime());
        payload.setOrder(notificationRule.getOrder());
        payload.setRepeat(notificationRule.getRepeat());
        payload.setSchedules(notificationRule.getSchedules());
        payload.setTimeRestriction(notificationRule.getTimeRestriction());
        payload.setSteps(constructCreateNotificationRuleStepPayloadList(notificationRule));

        final CreateNotificationRuleRequest request = new CreateNotificationRuleRequest();
        request.setBody(payload);
        request.setIdentifier(user.getUsername());
        RetryPolicyAdapter.invoke(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return notificationRuleApi.createNotificationRule(request).getData().getId();
            }
        });

    }

    private List<CreateNotificationRuleStepPayload> constructCreateNotificationRuleStepPayloadList(NotificationRule notificationRule) {
        List<CreateNotificationRuleStepPayload> createNotificationRuleStepPayloadList = new ArrayList<CreateNotificationRuleStepPayload>();

        for (NotificationRuleStep notificationRuleStep : notificationRule.getSteps()) {
            final CreateNotificationRuleStepPayload notificationRuleStepPayload = new CreateNotificationRuleStepPayload()
                    .contact(notificationRuleStep.getContact())
                    .enabled(notificationRuleStep.isEnabled())
                    .sendAfter(notificationRuleStep.getSendAfter());
            if (notificationRuleStepPayload.getContact().getMethod().equals(ContactMeta.MethodEnum.MOBILE)) {
                logger.warn("Skipping mobile contact method");
            } else {
                createNotificationRuleStepPayloadList.add(
                        notificationRuleStepPayload);
            }
        }

        return createNotificationRuleStepPayloadList;
    }

    private String findRuleIdByName(User user, NotificationRule notificationRule) {
        for (UserConfig userConfig : currentConfigs) {
            if (user.getUsername().equals(userConfig.getUser().getUsername())) {
                for (NotificationRule currentNotificationRule : userConfig.getNotificationRuleList()) {
                    if (currentNotificationRule.getName().equals(notificationRule.getName())) {
                        return currentNotificationRule.getId();
                    }
                }
            }
        }
        return null;
    }

}
