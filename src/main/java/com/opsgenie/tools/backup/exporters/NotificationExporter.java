package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.NotificationRule;
import com.ifountain.opsgenie.client.model.beans.User;
import com.ifountain.opsgenie.client.model.notification_rule.ListNotificationRulesRequest;
import com.ifountain.opsgenie.client.model.user.ListUsersRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Notification rules from Opsgenie account to local directory called
 * notifications
 *
 * @author Mehmet Mustafa Demir
 */
public class NotificationExporter extends BaseExporter<NotificationRule> {
    private final Logger logger = LogManager.getLogger(NotificationExporter.class);
    private ListNotificationRulesRequest listNotificationRulesRequest = null;

    public NotificationExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "notifications");
    }

    @Override
    protected String getBeanFileName(NotificationRule bean) {
        return bean.getName() + "-" + bean.getId();
    }

    @Override
    public void export() {
        try {
            ListUsersRequest request = new ListUsersRequest();
            List<User> currentUserList = getOpsGenieClient().user().listUsers(request).getUsers();
            listNotificationRulesRequest = new ListNotificationRulesRequest();
            for (User user : currentUserList) {
                listNotificationRulesRequest.setUsername(user.getUsername());
                List<NotificationRule> notificationRuleList = retrieveEntities();
                if (notificationRuleList != null) {
                    File userNotificationFile = new File(getExportDirectory().getAbsolutePath() + "/" + user.getUsername());
                    userNotificationFile.mkdirs();
                    for (NotificationRule bean : notificationRuleList) {
                        exportFile(getExportDirectory().getAbsolutePath() + "/" + user.getUsername() + "/" + getBeanFileName(bean) + ".json", bean);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error at Listing notifications", e);
        }

    }


    @Override
    protected List<NotificationRule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        return getOpsGenieClient().notificationRule().listNotificationRule(listNotificationRulesRequest).getRules();
    }
}
