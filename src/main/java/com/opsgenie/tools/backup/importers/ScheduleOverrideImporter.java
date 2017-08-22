package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleOverrideApi;
import com.opsgenie.client.model.*;

public class ScheduleOverrideImporter extends BaseImporter<ScheduleOverride> {

    private static ScheduleOverrideApi scheduleOverrideApi = new ScheduleOverrideApi();

    public ScheduleOverrideImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected ScheduleOverride checkEntityWithName(ScheduleOverride entity) throws ApiException {
        final GetScheduleOverrideRequest getScheduleOverrideRequest = new GetScheduleOverrideRequest()
                .scheduleIdentifierType(GetScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME)
                .alias(entity.getAlias())
                .identifier(entity.getParent().getName());
        return scheduleOverrideApi.getScheduleOverride(getScheduleOverrideRequest).getData();
    }

    @Override
    protected ScheduleOverride checkEntityWithId(ScheduleOverride entity) throws ApiException {
        final GetScheduleOverrideRequest getScheduleOverrideRequest = new GetScheduleOverrideRequest()
                .alias(entity.getAlias())
                .identifier(entity.getParent().getId());
        return scheduleOverrideApi.getScheduleOverride(getScheduleOverrideRequest).getData();
    }

    @Override
    protected ScheduleOverride getNewInstance() {
        return new ScheduleOverride();
    }

    @Override
    protected String getImportDirectoryName() {
        return "scheduleOverrides";
    }

    @Override
    protected void createEntity(ScheduleOverride entity) throws ApiException {
        CreateScheduleOverridePayload payload = new CreateScheduleOverridePayload();
        payload.setUser(entity.getUser());
        payload.setAlias(entity.getAlias());
        payload.setEndDate(entity.getEndDate());
        payload.setStartDate(entity.getStartDate());
        payload.setRotations(entity.getRotations());

        CreateScheduleOverrideRequest request = new CreateScheduleOverrideRequest();
        request.setIdentifier(entity.getParent().getId());
        request.setScheduleIdentifierType(CreateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        request.setBody(payload);

        scheduleOverrideApi.createScheduleOverride(request);
    }

    @Override
    protected void updateEntity(ScheduleOverride entity, EntityStatus entityStatus) throws ApiException {
        UpdateScheduleOverridePayload payload = new UpdateScheduleOverridePayload();
        payload.setUser(entity.getUser());
        payload.setEndDate(entity.getEndDate());
        payload.setStartDate(entity.getStartDate());
        payload.setRotations(entity.getRotations());

        UpdateScheduleOverrideRequest request = new UpdateScheduleOverrideRequest();
        request.setAlias(entity.getAlias());
        request.setBody(payload);
        request.setIdentifier(entity.getParent().getId());
        request.setScheduleIdentifierType(UpdateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);

        scheduleOverrideApi.updateScheduleOverride(request);
    }

    @Override
    protected String getEntityIdentifierName(ScheduleOverride scheduleOverride) {
        return "Schedule Override for user  " + scheduleOverride.getUser() + " for schedule " + scheduleOverride.getParent().getName();
    }
}
