package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ContactApi;
import com.opsgenie.oas.sdk.api.NotificationRuleApi;
import com.opsgenie.oas.sdk.api.UserApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.util.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.dto.UserConfig;

import java.util.ArrayList;
import java.util.List;

public class UserImporter extends BaseImporter<UserConfig> {

    private static UserApi userApi = new UserApi();
    private static ContactApi contactApi = new ContactApi();
    private static NotificationRuleApi notificationRuleApi = new NotificationRuleApi();
    private List<UserConfig> userConfigs;

    public UserImporter(String backupRootDirectory, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, addEntity, updateEntity);
    }

    @Override
    protected EntityStatus checkEntity(UserConfig entity) throws ApiException {
        for (UserConfig config : userConfigs) {
            final User currentUser = config.getUser();
            if (currentUser.getId().equals(entity.getUser().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (currentUser.getUsername().equals(entity.getUser().getUsername())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        userConfigs = EntityListService.listUserConfigs();
    }

    @Override
    protected UserConfig getNewInstance() {
        return new UserConfig();
    }

    @Override
    protected String getImportDirectoryName() {
        return "users";
    }

    @Override
    protected void createEntity(UserConfig userConfig) throws ApiException {
        CreateUserPayload payload = new CreateUserPayload();
        final User user = userConfig.getUser();
        payload.setUsername(user.getUsername());
        payload.setFullName(user.getFullName());
        payload.setLocale(user.getLocale());
        payload.setRole(user.getRole());
        payload.setDetails(user.getDetails());
        payload.setUserAddress(user.getUserAddress());
        payload.setSkypeUsername(user.getSkypeUsername());
        payload.setTags(user.getTags());
        payload.setTimeZone(user.getTimeZone());
        payload.setInvitationDisabled(true);

        if (BackupUtils.checkValidString(user.getSkypeUsername()))
            payload.setSkypeUsername(user.getSkypeUsername());

        payload.setTimeZone(user.getTimeZone());

        userApi.createUser(payload);
        addContacts(user);
        final List<NotificationRule> notificationRuleList = userConfig.getNotificationRuleList();
        compareNotificationRules(user, notificationRuleList);
    }

    private void createNotificationRule(User user, NotificationRule notificationRule) throws ApiException {
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

        CreateNotificationRuleRequest request = new CreateNotificationRuleRequest();
        request.setBody(payload);
        request.setIdentifier(user.getUsername());

        notificationRuleApi.createNotificationRule(request).getData().getId();
    }

    private List<CreateNotificationRuleStepPayload> constructCreateNotificationRuleStepPayloadList(NotificationRule notificationRule) {
        List<CreateNotificationRuleStepPayload> createNotificationRuleStepPayloadList = new ArrayList<CreateNotificationRuleStepPayload>();

        for (NotificationRuleStep notificationRuleStep : notificationRule.getSteps()) {
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
    protected void updateEntity(UserConfig userConfig, EntityStatus entityStatus) throws ApiException {
        UpdateUserPayload payload = new UpdateUserPayload();
        final User user = userConfig.getUser();
        payload.setUsername(user.getUsername());
        payload.setFullName(user.getFullName());
        payload.setLocale(user.getLocale());
        payload.setRole(user.getRole());
        payload.setDetails(user.getDetails());
        payload.setUserAddress(user.getUserAddress());
        payload.setSkypeUsername(user.getSkypeUsername());
        payload.setTags(user.getTags());
        payload.setTimeZone(user.getTimeZone());

        if (BackupUtils.checkValidString(user.getSkypeUsername()))
            payload.setSkypeUsername(user.getSkypeUsername());

        payload.setTimeZone(user.getTimeZone());

        UpdateUserRequest request = new UpdateUserRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(user.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setIdentifier(user.getUsername());
        }
        request.setBody(payload);
        userApi.updateUser(request);
        addContacts(user);
        final List<NotificationRule> notificationRuleList = userConfig.getNotificationRuleList();
        compareNotificationRules(user, notificationRuleList);
    }

    private void compareNotificationRules(User user, List<NotificationRule> notificationRuleList) throws ApiException {
        if (notificationRuleList != null) {
            logger.info("Updating notification rules for " + user.getUsername());
            for (NotificationRule notificationRule : notificationRuleList) {
                final String ruleIdByName = findRuleIdByName(user, notificationRule);
                if (ruleIdByName != null) {
                    updateNotificationRule(user, notificationRule);
                } else {
                    createNotificationRule(user, notificationRule);
                }
            }
        }
    }

    protected void updateNotificationRule(User user, NotificationRule notificationRule) throws ApiException {
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

        UpdateNotificationRuleRequest request = new UpdateNotificationRuleRequest();
        request.setRuleId(findRuleIdByName(user, notificationRule));
        request.setIdentifier(user.getUsername());
        request.setBody(payload);

        notificationRuleApi.updateNotificationRule(request);
    }

    private String findRuleIdByName(User user, NotificationRule notificationRule) {
        for (UserConfig userConfig : userConfigs) {
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

    private void addContacts(User user) throws ApiException {
        CreateContactRequest createContactRequest = new CreateContactRequest();
        createContactRequest.setIdentifier(user.getUsername());

        List<ContactWithApplyOrder> currentContactList = contactApi.listContacts(user.getUsername()).getData();
        List<UserContact> backupContactList = user.getUserContacts();

        if (backupContactList != null) {
            for (UserContact userContact : backupContactList) {
                if (userContact.getContactMethod() != null && !userContact.getContactMethod().equals(UserContact.ContactMethodEnum.MOBILE)) {
                    boolean notExist = true;
                    for (ContactWithApplyOrder currentContact : currentContactList) {
                        if (userContact.getTo().equals(currentContact.getTo())
                                && userContact.getContactMethod().getValue().equals(currentContact.getMethod())) {
                            notExist = false;
                            break;
                        }
                    }
                    if (notExist) {
                        CreateContactPayload payload = new CreateContactPayload();
                        payload.setMethod(CreateContactPayload.MethodEnum.fromValue(userContact.getContactMethod().getValue()));
                        payload.setTo(userContact.getTo());
                        contactApi.createContact(createContactRequest);
                    }

                }
            }
        }
    }

    @Override
    protected String getEntityIdentifierName(UserConfig entity) {
        return "User " + entity.getUser().getUsername();
    }
}
