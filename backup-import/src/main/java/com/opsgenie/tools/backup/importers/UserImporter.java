package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ContactApi;
import com.opsgenie.oas.sdk.api.UserApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.UserConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.UserRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.util.List;
import java.util.concurrent.Callable;

public class UserImporter extends BaseImporterWithRateLimiting<UserConfig> {
    private static final int ALREADY_EXISTS_STATUS_CODE = 409;
    private static UserApi userApi = new UserApi();
    private static ContactApi contactApi = new ContactApi();

    public UserImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntity) {
        super(backupRootDirectory, rateLimitManager, addEntity, updateEntity);
    }

    @Override
    protected EntityRetriever<UserConfig> initializeEntityRetriever() {
        return new UserRetriever(rateLimitManager);
    }

    @Override
    protected EntityStatus checkEntity(UserConfig entity) throws ApiException {
        for (UserConfig config : currentConfigs) {
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
    protected UserConfig getNewInstance() {
        return new UserConfig();
    }

    @Override
    protected String getImportDirectoryName() {
        return "users";
    }

    @Override
    protected void createEntity(UserConfig userConfig) throws Exception {
        final CreateUserPayload payload = new CreateUserPayload();
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

        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                try {
                    return userApi.createUser(payload);
                } catch (ApiException e) {
                    if (e.getCode() == ALREADY_EXISTS_STATUS_CODE) {
                        throw new Exception("The user being created is registered in another OpsGenie account. ");
                    } else {
                        throw e;
                    }
                }
            }
        });

        addContacts(user);
    }

    @Override
    protected void updateEntity(UserConfig userConfig, EntityStatus entityStatus) throws Exception {
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

        final UpdateUserRequest request = new UpdateUserRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(user.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setIdentifier(user.getUsername());
        }
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {

            @Override
            public SuccessResponse call() throws Exception {
                return userApi.updateUser(request);
            }
        });

        addContacts(user);
    }

    private void addContacts(final User user) throws Exception {
        final CreateContactRequest createContactRequest = new CreateContactRequest();
        createContactRequest.setIdentifier(user.getUsername());

        List<ContactWithApplyOrder> currentContactList = RetryPolicyAdapter.invoke(new Callable<List<ContactWithApplyOrder>>() {
            @Override
            public List<ContactWithApplyOrder> call() throws Exception {
                return contactApi.listContacts(user.getUsername()).getData();
            }
        });

        List<UserContact> backupContactList = user.getUserContacts();

        if (backupContactList != null) {
            for (UserContact userContact : backupContactList) {
                try {
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
                            createContactRequest.body(payload);
                            RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
                                @Override
                                public SuccessResponse call() throws Exception {
                                    return contactApi.createContact(createContactRequest);
                                }
                            });

                        }

                    }
                } catch (Exception e) {
                    logger.error("Could not add contact " + userContact.getContactMethod() + "to user:" + user.getUsername() + ", " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected String getEntityIdentifierName(UserConfig entity) {
        return "User " + entity.getUser().getUsername();
    }
}
