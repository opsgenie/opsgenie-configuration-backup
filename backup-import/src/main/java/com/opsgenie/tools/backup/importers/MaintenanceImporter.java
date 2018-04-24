package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.MaintenanceApi;
import com.opsgenie.oas.sdk.model.CreateMaintenancePayload;
import com.opsgenie.oas.sdk.model.Maintenance;
import com.opsgenie.oas.sdk.model.UpdateMaintenancePayload;
import com.opsgenie.oas.sdk.model.UpdateMaintenanceRequest;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.MaintenanceRetriever;

import java.io.IOException;
import java.text.ParseException;

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
    protected void createEntity(Maintenance entity) throws ParseException, IOException, ApiException {

        CreateMaintenancePayload payload = new CreateMaintenancePayload();
        payload.setDescription(entity.getDescription());
        payload.setTime(entity.getTime());
        payload.setRules(entity.getRules());

        api.createMaintenance(payload);
    }

    @Override
    protected void updateEntity(Maintenance entity, EntityStatus entityStatus) throws ParseException, IOException, ApiException {

        UpdateMaintenancePayload payload = new UpdateMaintenancePayload();
        payload.setDescription(entity.getDescription());
        payload.setRules(entity.getRules());
        payload.setTime(entity.getTime());

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setId(entity.getId());
        request.setBody(payload);

        api.updateMaintenance(request);
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
