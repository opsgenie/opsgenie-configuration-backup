package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.NotificationRuleApi;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class UserNotificationRuleExporter extends BaseExporter<NotificationRule> {
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();
    private static UserApi userApi = new UserApi();
    private static String userId;

    public UserNotificationRuleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "notifications");
    }

    @Override
    protected String getBeanFileName(NotificationRule bean) {
        return bean.getName() + "-" + bean.getId();
    }

    @Override
    public void export() {
        try {
            final List<User> users = userApi.listUsers(new ListUsersRequest()).getData();
            for (User user : users) {
                try {
                    userId = user.getId();
                    List<NotificationRule> notificationRuleList = retrieveEntities();
                    if (notificationRuleList != null) {
                        File userNotificationFile = new File(getExportDirectory().getAbsolutePath() + "/" + user.getUsername());
                        userNotificationFile.mkdirs();
                        for (NotificationRule bean : notificationRuleList) {
                            exportFile(getExportDirectory().getAbsolutePath() + "/" + user.getUsername() + "/" + getBeanFileName(bean) + ".json", bean);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error at Listing notifications for user " + user.getUsername(), e);
                }

            }
        } catch (Exception e) {
            logger.error("Error at Listing users for notification rules", e);
        }

    }


    @Override
    protected List<NotificationRule> retrieveEntities() throws ParseException, IOException, ApiException {
        final List<NotificationRuleMeta> data = notificationRuleApi.listNotificationRules(userId).getData();
        List<NotificationRule> rules = new ArrayList<NotificationRule>();
        for (NotificationRuleMeta meta : data) {
            final NotificationRule notificationRule = notificationRuleApi.getNotificationRule(new GetNotificationRuleRequest().identifier(userId).ruleId(meta.getId())).getData();
            rules.add(notificationRule);
        }
        return rules;
    }
}
