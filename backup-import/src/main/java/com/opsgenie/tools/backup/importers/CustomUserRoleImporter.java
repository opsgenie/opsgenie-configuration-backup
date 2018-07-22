package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.CustomUserRoleApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.retrieval.CustomUserRoleRetriever;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;

import java.util.concurrent.Callable;

public class CustomUserRoleImporter extends BaseImporter<CustomUserRole> {

    private static CustomUserRoleApi api = new CustomUserRoleApi();

    public CustomUserRoleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityRetriever<CustomUserRole> initializeEntityRetriever() {
        return new CustomUserRoleRetriever();
    }

    @Override
    protected EntityStatus checkEntity(CustomUserRole entity) throws ApiException {
        for (CustomUserRole customUserRole : currentConfigs) {
            if (customUserRole.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (customUserRole.getName().equals(entity.getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected CustomUserRole getNewInstance() {
        return new CustomUserRole();
    }

    @Override
    protected String getImportDirectoryName() {
        return "customUserRoles";
    }

    @Override
    protected void createEntity(CustomUserRole entity) throws Exception {
        CreateCustomUserRolePayload payload = new CreateCustomUserRolePayload();
        payload.setName(entity.getName());

        payload.setExtendedRole(entity.getExtendedRole());
        payload.setGrantedRights(entity.getGrantedRights());
        payload.setDisallowedRights(entity.getDisallowedRights());

        final AddCustomUserRoleRequest request = new AddCustomUserRoleRequest();
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.createCustomUserRole(request);
            }
        });

    }

    @Override
    protected void updateEntity(CustomUserRole entity, EntityStatus entityStatus) throws Exception {

        UpdateCustomUserRolePayload payload = new UpdateCustomUserRolePayload();
        payload.setName(entity.getName());

        payload.setExtendedRole(entity.getExtendedRole());
        payload.setDisallowedRights(entity.getDisallowedRights());
        payload.setGrantedRights(entity.getGrantedRights());

        final UpdateCustomUserRoleRequest request = new UpdateCustomUserRoleRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(entity.getId());
            request.setIdentifierType(UpdateCustomUserRoleRequest.IdentifierTypeEnum.ID);
        } else {
            request.setIdentifier(entity.getName());
            request.setIdentifierType(UpdateCustomUserRoleRequest.IdentifierTypeEnum.NAME);
        }
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.updateCustomUserRole(request);
            }
        });

    }

    @Override
    protected String getEntityIdentifierName(CustomUserRole entity) {
        return "CustomUserRole " + entity.getName();
    }

}
