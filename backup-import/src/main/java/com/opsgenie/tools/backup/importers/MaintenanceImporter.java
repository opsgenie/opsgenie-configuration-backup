package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.MaintenanceApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.MaintenanceRetriever;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Callable;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 16:45
 */
public class MaintenanceImporter extends BaseImporter<Maintenance> {

    private static MaintenanceApi api = new MaintenanceApi();

    public MaintenanceImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityRetriever<Maintenance> initializeEntityRetriever() {
        return new MaintenanceRetriever();
    }

    @Override
    protected EntityStatus checkEntity(Maintenance entity) throws ApiException {
        for (Maintenance maintenance : currentConfigs) {
            if (maintenance.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void createEntity(Maintenance entity) throws Exception {

        final CreateMaintenancePayload payload = new CreateMaintenancePayload();
        payload.setDescription(entity.getDescription());
        payload.setTime(entity.getTime());
        payload.setRules(entity.getRules());
        RetryPolicyAdapter.invoke(new Callable<CreateMaintenanceResponse>() {
            @Override
            public CreateMaintenanceResponse call() throws Exception {
                return api.createMaintenance(payload);
            }
        });

    }

    @Override
    protected void updateEntity(Maintenance entity, EntityStatus entityStatus) throws Exception {

        UpdateMaintenancePayload payload = new UpdateMaintenancePayload();
        payload.setDescription(entity.getDescription());
        payload.setRules(entity.getRules());
        payload.setTime(entity.getTime());

        final UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setId(entity.getId());
        request.setBody(payload);

        RetryPolicyAdapter.invoke(new Callable<UpdateMaintenanceResponse>() {
            @Override
            public UpdateMaintenanceResponse call() throws Exception {
                return api.updateMaintenance(request);
            }
        });

    }

    @Override
    protected String getEntityIdentifierName(Maintenance entity) {
        return "Maintenance " + entity.getDescription();
    }

    @Override
    protected String getImportDirectoryName() {
        return "maintenance";
    }

    @Override
    protected Maintenance getNewInstance() {
        return new Maintenance();
    }
}
