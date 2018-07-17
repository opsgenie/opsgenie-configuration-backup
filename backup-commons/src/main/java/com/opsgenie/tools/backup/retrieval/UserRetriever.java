package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.NotificationRuleApi;
import com.opsgenie.oas.sdk.api.UserApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class UserRetriever implements EntityRetriever<UserConfig> {

    private static final Logger logger = LoggerFactory.getLogger(UserRetriever.class);

    private static final UserApi userApi = new UserApi();
    private static final NotificationRuleApi notificationRuleApi = new NotificationRuleApi();

    @Override
    public List<UserConfig> retrieveEntities() throws Exception {
        logger.info("Retrieving current user configurations");
        final List<User> userList = getAllUsers();
        final List<User> usersWithContacts = new ArrayList<User>(populateUserContacts(userList));
        return populateUsersWithNotificationRules(usersWithContacts);
    }

    private static List<User> getAllUsers() throws Exception {
        final ListUsersResponse listUsersResponse = ApiAdapter.invoke(new Callable<ListUsersResponse>() {
            @Override
            public ListUsersResponse call() {
                return userApi.listUsers(new ListUsersRequest());
            }
        });

        List<User> userList = listUsersResponse.getData();
        final Long pageCount = listUsersResponse.getTotalCount() + 1;
        logger.info("Retrieved " + userList.size() + "/" + listUsersResponse.getTotalCount());
        for (int i = 1; i < (pageCount * 1.0) / 100; i++) {
            final int offset = 100 * i;
            userList.addAll(ApiAdapter.invoke(new Callable<Collection<? extends User>>() {
                        @Override
                        public Collection<? extends User> call()  {
                            return userApi.listUsers(new ListUsersRequest().offset(offset)).getData();
                        }
                    }));
            logger.info("Retrieved " + userList.size() + "/" + listUsersResponse.getTotalCount());
        }
        return userList;
    }

    private static ConcurrentLinkedQueue<User> populateUserContacts(List<User> userList) throws InterruptedException {
        logger.info("Populating user contacts rules");
        final ConcurrentLinkedQueue<User> usersWithContact = new ConcurrentLinkedQueue<User>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final User user : userList) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        usersWithContact.add(ApiAdapter.invoke(new Callable<User>() {
                                    @Override
                                    public User call()  {
                                        return userApi.getUser(new GetUserRequest().identifier(user.getId()).expand(Collections.singletonList("contact"))).getData();
                                    }
                                }));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.info("Populating user contacts:" + usersWithContact.size() + "/" + userList.size());
        }
        return usersWithContact;
    }

    private static List<UserConfig> populateUsersWithNotificationRules(List<User> users) throws InterruptedException {
        logger.info("Populating user notification rules");
        final ConcurrentLinkedQueue<UserConfig> usersWithNotificationRules = new ConcurrentLinkedQueue<UserConfig>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final User user : users) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    UserConfig userConfigWrapper = new UserConfig();
                    List<NotificationRuleMeta> data = null;
                    try {
                        data = ApiAdapter.invoke(new Callable<List<NotificationRuleMeta>>() {
                            @Override
                            public List<NotificationRuleMeta> call()  {
                                return notificationRuleApi.listNotificationRules(user.getId()).getData();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<NotificationRule> rules = new ArrayList<NotificationRule>();
                    for (final NotificationRuleMeta meta : data) {
                        try {
                            final NotificationRule notificationRule = ApiAdapter.invoke(new Callable<NotificationRule>() {
                                @Override
                                public NotificationRule call()  {
                                    return notificationRuleApi.getNotificationRule(new GetNotificationRuleRequest().identifier(user.getId()).ruleId(meta.getId())).getData();
                                }
                            });

                            rules.add(notificationRule);
                        } catch (Exception e) {
                            logger.error("Could not populate notification rule " + meta.getId() + "for user " + user.getUsername());
                        }
                    }
                    userConfigWrapper.setUser(user);
                    userConfigWrapper.setNotificationRuleList(rules);
                    usersWithNotificationRules.add(userConfigWrapper);
                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.info("Populating user notification rules:" + usersWithNotificationRules.size() + "/" + users.size());
        }
        return new ArrayList<UserConfig>(usersWithNotificationRules);
    }
}
