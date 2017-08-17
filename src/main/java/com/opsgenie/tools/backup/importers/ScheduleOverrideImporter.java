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
    protected void getEntityWithId(ScheduleOverride entity) throws ApiException {
        scheduleOverrideApi.getScheduleOverride(new GetScheduleOverrideRequest().alias(entity.getAlias()).identifier(entity.getParent().getId()));
    }

    @Override
    protected ScheduleOverride getBean() {
        return new ScheduleOverride();
    }

    @Override
    protected String getImportDirectoryName() {
        return "scheduleOverrides";
    }

    @Override
    protected void addBean(ScheduleOverride scheduleOverride) throws ApiException {
        CreateScheduleOverridePayload payload = new CreateScheduleOverridePayload();
        payload.setUser(scheduleOverride.getUser());
        payload.setAlias(scheduleOverride.getAlias());
        payload.setEndDate(scheduleOverride.getEndDate());
        payload.setStartDate(scheduleOverride.getStartDate());
        payload.setRotations(scheduleOverride.getRotations());

        CreateScheduleOverrideRequest request = new CreateScheduleOverrideRequest();
        request.setIdentifier(scheduleOverride.getParent().getId());
        request.setScheduleIdentifierType(CreateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        request.setBody(payload);

        scheduleOverrideApi.createScheduleOverride(request);
    }

    @Override
    protected void updateBean(ScheduleOverride scheduleOverride) throws ApiException {
        UpdateScheduleOverridePayload payload = new UpdateScheduleOverridePayload();
        payload.setUser(scheduleOverride.getUser());
        payload.setEndDate(scheduleOverride.getEndDate());
        payload.setStartDate(scheduleOverride.getStartDate());
        payload.setRotations(scheduleOverride.getRotations());

        UpdateScheduleOverrideRequest request = new UpdateScheduleOverrideRequest();
        request.setAlias(scheduleOverride.getAlias());
        request.setBody(payload);
        request.setIdentifier(scheduleOverride.getParent().getId());
        request.setScheduleIdentifierType(UpdateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);

        scheduleOverrideApi.updateScheduleOverride(request);
    }

    @Override
    protected String getEntityIdentifierName(ScheduleOverride bean) {
        return "Schedule Override for user  " + bean.getUser() + " for schedule " + bean.getParent().getName();
    }
}
