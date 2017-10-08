package com.opsgenie.tools.backup;

import com.opsgenie.oas.sdk.model.NotificationRule;
import com.opsgenie.oas.sdk.model.User;

import java.util.List;

public class UserConfig {

    private User user;
    private List<NotificationRule> notificationRuleList;

    public User getUser() {
        return user;
    }

    public UserConfig setUser(User user) {
        this.user = user;
        return this;
    }

    public List<NotificationRule> getNotificationRuleList() {
        return notificationRuleList;
    }

    public UserConfig setNotificationRuleList(List<NotificationRule> notificationRuleList) {
        this.notificationRuleList = notificationRuleList;
        return this;
    }
}
