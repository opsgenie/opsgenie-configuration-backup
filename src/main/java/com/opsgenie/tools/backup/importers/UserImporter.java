package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Contact;
import com.ifountain.opsgenie.client.model.beans.User;
import com.ifountain.opsgenie.client.model.contact.AddContactRequest;
import com.ifountain.opsgenie.client.model.contact.ListContactsRequest;
import com.ifountain.opsgenie.client.model.user.AddUserRequest;
import com.ifountain.opsgenie.client.model.user.ListUsersRequest;
import com.ifountain.opsgenie.client.model.user.UpdateUserRequest;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports Users from local directory called users to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class UserImporter extends BaseImporter<User> {
    public UserImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected int checkEntities(User oldEntity, User currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        } else if (oldEntity.getUsername().equals(currentEntity.getUsername())) {
            oldEntity.setId(currentEntity.getId());
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        }
        return -1;
    }

    @Override
    protected User getBean() throws IOException, ParseException {
        return new User();
    }

    @Override
    protected String getImportDirectoryName() {
        return "users";
    }

    @Override
    protected void addBean(User bean) throws ParseException, OpsGenieClientException, IOException {
        AddUserRequest request = new AddUserRequest();
        request.setUsername(bean.getUsername());
        request.setFullname(bean.getFullname());
        request.setLocale(bean.getLocale());
        request.setRole(bean.getRole());
        if (BackupUtils.checkValidString(bean.getSkypeUsername()))
            request.setSkypeUsername(bean.getSkypeUsername());
        request.setTimeZone(bean.getTimeZone());
        getOpsGenieClient().user().addUser(request);
        addContacts(bean);
    }

    @Override
    protected void updateBean(User bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(bean.getId());
        request.setFullname(bean.getFullname());
        request.setLocale(bean.getLocale());
        request.setRole(bean.getRole());
        if (BackupUtils.checkValidString(bean.getSkypeUsername()))
            request.setSkypeUsername(bean.getSkypeUsername());
        request.setTimeZone(bean.getTimeZone());
        getOpsGenieClient().user().updateUser(request);
        addContacts(bean);
    }

    private void addContacts(User user) throws ParseException, OpsGenieClientException, IOException {
        AddContactRequest addContactRequest = new AddContactRequest();
        addContactRequest.setUsername(user.getUsername());
        ListContactsRequest listContactsRequest = new ListContactsRequest();
        listContactsRequest.setUsername(user.getUsername());
        List<Contact> currentContactList = getOpsGenieClient().contact().listContact(listContactsRequest).getUserContacts();
        List<Contact> backupContactList = user.getUserContacts();
        for (Contact userContact : backupContactList) {
            if (userContact.getMethod() != null && !userContact.getMethod().equals(Contact.Method.MOBILE_APP)) {
                boolean notExist = true;
                for (Contact currentContact : currentContactList) {
                    if (userContact.getTo().equals(currentContact.getTo())
                            && userContact.getMethod().equals(currentContact.getMethod())) {
                        notExist = false;
                        break;
                    }
                }
                if (notExist) {
                    addContactRequest.setTo(userContact.getTo());
                    addContactRequest.setMethod(userContact.getMethod());
                    getOpsGenieClient().contact().addContact(addContactRequest);
                }

            }
        }
    }

    @Override
    protected List<User> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListUsersRequest request = new ListUsersRequest();
        return getOpsGenieClient().user().listUsers(request).getUsers();
    }

    @Override
    protected boolean isSame(User oldEntity, User currentEntity) {
        oldEntity.setEscalations(null);
        oldEntity.setGroups(null);
        oldEntity.setSchedules(null);
        currentEntity.setEscalations(null);
        currentEntity.setGroups(null);
        currentEntity.setSchedules(null);
        return super.isSame(oldEntity, currentEntity);
    }

    @Override
    protected String getEntityIdentifierName(User entitiy) {
        return "User " + entitiy.getUsername();
    }
}
