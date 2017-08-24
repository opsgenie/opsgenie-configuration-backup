package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ContactApi;
import com.opsgenie.client.api.NotificationRuleApi;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.UserConfig;

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
