package com.opsgenie.tools.backup;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.*;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.IntegrationConfig;
import com.opsgenie.tools.backup.dto.ScheduleConfig;
import com.opsgenie.tools.backup.dto.TeamConfig;
import com.opsgenie.tools.backup.dto.UserConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityListService {

    private static final Logger logger = LogManager.getLogger(EntityListService.class);

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();
    private static UserApi userApi = new UserApi();
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();
    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi overrideApi = new ScheduleOverrideApi();
    private static PolicyApi policyApi = new PolicyApi();
    private static EscalationApi escalationApi = new EscalationApi();
    private static IntegrationApi integrationApi = new IntegrationApi();
    private static IntegrationActionApi integrationActionApi = new IntegrationActionApi();

    public static List<ForwardingRule> listForwardingRules() throws ApiException {
        return forwardingRuleApi.listForwardingRules().getData();
    }

    public static List<UserConfig> listUserConfigs() throws ApiException {
        List<User> userList = getAllUsers();
        List<User> usersWithContact = new ArrayList<User>();
        for (User user : userList) {
            usersWithContact.add(userApi.getUser(new GetUserRequest().identifier(user.getId()).expand(Collections.singletonList("contact"))).getData());
        }
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        for (User user : usersWithContact) {
            UserConfig wrapper = new UserConfig();
            wrapper.setUser(user);
            final List<NotificationRuleMeta> data = notificationRuleApi.listNotificationRules(user.getId()).getData();
            List<NotificationRule> rules = new ArrayList<NotificationRule>();
            for (NotificationRuleMeta meta : data) {
                final NotificationRule notificationRule = notificationRuleApi.getNotificationRule(new GetNotificationRuleRequest().identifier(user.getId()).ruleId(meta.getId())).getData();
                rules.add(notificationRule);
            }
            wrapper.setNotificationRuleList(rules);
            userConfigs.add(wrapper);
        }
        return userConfigs;
    }

    private static List<User> getAllUsers() throws ApiException {
        final ListUsersResponse listUsersResponse = userApi.listUsers(new ListUsersRequest());
        List<User> userList = listUsersResponse.getData();
        final Long pageCount = listUsersResponse.getTotalCount() + 1;
        for (int i = 1; i < (pageCount * 1.0) / 100; i++) {
            userList.addAll(userApi.listUsers(new ListUsersRequest().offset(100 * i)).getData());
        }
        return userList;
    }

    public static List<TeamConfig> listTeams() throws ApiException {
        final List<Team> teams = teamApi.listTeams(Collections.singletonList("member")).getData();
        List<TeamConfig> teamConfigs = new ArrayList<TeamConfig>();
        for (Team team : teams) {
            TeamConfig teamConfig = new TeamConfig();
            teamConfig.setTeam(team);
            final List<TeamRoutingRule> routingRules = teamRoutingRuleApi.listTeamRoutingRules(new ListTeamRoutingRulesRequest().identifier(team.getId())).getData();
            teamConfig.setTeamRoutingRules(routingRules);
            teamConfigs.add(teamConfig);
        }
        return teamConfigs;
    }

    public static List<ScheduleConfig> listSchedules() throws ApiException {
        List<ScheduleConfig> scheduleConfigs = new ArrayList<ScheduleConfig>();
        final List<Schedule> schedules = scheduleApi.listSchedules(Collections.singletonList("rotation")).getData();
        for (Schedule schedule : schedules) {
            ScheduleConfig scheduleConfig = new ScheduleConfig();
            scheduleConfig.setSchedule(schedule);
            scheduleConfig.setScheduleOverrideList(overrideApi.listScheduleOverride(new ListScheduleOverridesRequest().identifier(schedule.getId())).getData());
            scheduleConfigs.add(scheduleConfig);
        }
        return scheduleConfigs;
    }

    public static List<AlertPolicy> listPolicies() throws ApiException {
        final List<AlertPolicyMeta> policyMetaList = policyApi.listAlertPolicies().getData();
        List<AlertPolicy> policies = new ArrayList<AlertPolicy>();
        for (AlertPolicyMeta meta : policyMetaList) {
            policies.add(policyApi.getAlertPolicy(meta.getId()).getData());
        }
        return policies;
    }

    public static List<IntegrationConfig> listIntegrations() throws ApiException {
        final List<IntegrationMeta> integrationMetaList = integrationApi.listIntegrations(new ListIntegrationRequest()).getData();
        List<IntegrationConfig> integrations = new ArrayList<IntegrationConfig>();
        for (IntegrationMeta meta : integrationMetaList) {
            final IntegrationConfig integrationConfig = new IntegrationConfig();
            final Integration integration = integrationApi.getIntegration(meta.getId()).getData();

            integration.setId(meta.getId());
            integrationConfig.setIntegration(integration);
            try {
                integrationConfig.setIntegrationActions(integrationActionApi.listIntegrationActions(meta.getId()).getData());
            } catch (Exception e) {
                logger.info(integration.getName() + " is not an advanced integration, so not exporting actions");
            }

            integrations.add(integrationConfig);
        }
        return integrations;
    }

    public static List<Escalation> listEscalations() throws ApiException {
        return escalationApi.listEscalations().getData();
    }
}
