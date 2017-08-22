package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ContactApi;
import com.opsgenie.client.api.UserApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.util.List;

public class UserImporter extends BaseImporter<User> {

    private static UserApi userApi = new UserApi();
    private static ContactApi contactApi = new ContactApi();

    public UserImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected User checkEntityWithName(User user) throws ApiException {
        return userApi.getUser(new GetUserRequest().identifier(user.getUsername())).getData();
    }

    @Override
    protected User checkEntityWithId(User user) throws ApiException {
        return userApi.getUser(new GetUserRequest().identifier(user.getId())).getData();
    }

    @Override
    protected User getNewInstance() {
        return new User();
    }

    @Override
    protected String getImportDirectoryName() {
        return "users";
    }

    @Override
    protected void createEntity(User entity) throws ApiException {
        CreateUserPayload payload = new CreateUserPayload();
        payload.setUsername(entity.getUsername());
        payload.setFullName(entity.getFullName());
        payload.setLocale(entity.getLocale());
        payload.setRole(entity.getRole());
        payload.setDetails(entity.getDetails());
        payload.setUserAddress(entity.getUserAddress());
        payload.setSkypeUsername(entity.getSkypeUsername());
        payload.setTags(entity.getTags());
        payload.setTimeZone(entity.getTimeZone());
        payload.setInvitationDisabled(false);

        if (BackupUtils.checkValidString(entity.getSkypeUsername()))
            payload.setSkypeUsername(entity.getSkypeUsername());

        payload.setTimeZone(entity.getTimeZone());

        userApi.createUser(payload);
        addContacts(entity);
    }

    @Override
    protected void updateEntity(User entity, EntityStatus entityStatus) throws ApiException {
        UpdateUserPayload payload = new UpdateUserPayload();
        payload.setUsername(entity.getUsername());
        payload.setFullName(entity.getFullName());
        payload.setLocale(entity.getLocale());
        payload.setRole(entity.getRole());
        payload.setDetails(entity.getDetails());
        payload.setUserAddress(entity.getUserAddress());
        payload.setSkypeUsername(entity.getSkypeUsername());
        payload.setTags(entity.getTags());
        payload.setTimeZone(entity.getTimeZone());

        if (BackupUtils.checkValidString(entity.getSkypeUsername()))
            payload.setSkypeUsername(entity.getSkypeUsername());

        payload.setTimeZone(entity.getTimeZone());

        UpdateUserRequest request = new UpdateUserRequest();
        request.setIdentifier(entity.getId());
        request.setBody(payload);

        userApi.updateUser(request);
        addContacts(entity);
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
    protected String getEntityIdentifierName(User entity) {
        return "User " + entity.getUsername();
    }
}
