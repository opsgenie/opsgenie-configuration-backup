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
    protected BeanStatus checkEntities(User oldEntity, User currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        if (oldEntity.getUsername().equals(currentEntity.getUsername())) {
            oldEntity.setId(currentEntity.getId());
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;

    }

    @Override
    protected User getBean() {
        return new User();
    }

    @Override
    protected String getImportDirectoryName() {
        return "users";
    }

    @Override
    protected void addBean(User bean) throws ApiException {
        CreateUserPayload payload = new CreateUserPayload();
        payload.setUsername(bean.getUsername());
        payload.setFullName(bean.getFullName());
        payload.setLocale(bean.getLocale());
        payload.setRole(bean.getRole());
        payload.setDetails(bean.getDetails());
        payload.setUserAddress(bean.getUserAddress());
        payload.setSkypeUsername(bean.getSkypeUsername());
        payload.setTags(bean.getTags());
        payload.setTimeZone(bean.getTimeZone());
        payload.setInvitationDisabled(false);

        if (BackupUtils.checkValidString(bean.getSkypeUsername()))
            payload.setSkypeUsername(bean.getSkypeUsername());

        payload.setTimeZone(bean.getTimeZone());

        userApi.createUser(payload);
        addContacts(bean);
    }

    @Override
    protected void updateBean(User bean) throws ApiException {
        UpdateUserPayload payload = new UpdateUserPayload();
        payload.setUsername(bean.getUsername());
        payload.setFullName(bean.getFullName());
        payload.setLocale(bean.getLocale());
        payload.setRole(bean.getRole());
        payload.setDetails(bean.getDetails());
        payload.setUserAddress(bean.getUserAddress());
        payload.setSkypeUsername(bean.getSkypeUsername());
        payload.setTags(bean.getTags());
        payload.setTimeZone(bean.getTimeZone());

        if (BackupUtils.checkValidString(bean.getSkypeUsername()))
            payload.setSkypeUsername(bean.getSkypeUsername());

        payload.setTimeZone(bean.getTimeZone());

        UpdateUserRequest request = new UpdateUserRequest();
        request.setIdentifier(bean.getId());
        request.setBody(payload);

        userApi.updateUser(request);
        addContacts(bean);
    }

    private void addContacts(User user) throws ApiException {
        CreateContactRequest createContactRequest = new CreateContactRequest();
        createContactRequest.setIdentifier(user.getUsername());

        List<ContactWithApplyOrder> currentContactList = contactApi.listContacts(user.getUsername()).getData();
        List<UserContact> backupContactList = user.getUserContacts();

        for (UserContact userContact : backupContactList) {
            if (userContact.getContactMethod() != null && !userContact.getContactMethod().equals(UserContact.ContactMethodEnum.MOBILE)) {
                boolean notExist = true;
                for (ContactWithApplyOrder currentContact : currentContactList) {
                    if (userContact.getTo().equals(currentContact.getTo())
                            && userContact.getContactMethod().equals(currentContact.getMethod())) {
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

    @Override
    protected List<User> retrieveEntities() throws ApiException {
        ListUsersRequest request = new ListUsersRequest();
        return userApi.listUsers(request).getData();
    }

    @Override
    protected String getEntityIdentifierName(User entitiy) {
        return "User " + entitiy.getUsername();
    }
}
