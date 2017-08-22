package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.CreateSchedulePayload;
import com.opsgenie.client.model.GetScheduleRequest;
import com.opsgenie.client.model.Schedule;

public class ScheduleTemplateImporter extends BaseImporter<Schedule> {

    private static ScheduleApi api = new ScheduleApi();

    public ScheduleTemplateImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected Schedule checkEntityWithName(Schedule schedule) throws ApiException {
        final GetScheduleRequest identifier = new GetScheduleRequest()
                .identifierType(GetScheduleRequest.IdentifierTypeEnum.NAME)
                .identifier(schedule.getName());
        return api.getSchedule(identifier).getData();
    }

    @Override
    protected Schedule checkEntityWithId(Schedule schedule) throws ApiException {
        final GetScheduleRequest identifier = new GetScheduleRequest().identifier(schedule.getId());
        return api.getSchedule(identifier).getData();
    }

    @Override
    protected Schedule getNewInstance() {
        return new Schedule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void createEntity(Schedule entity) throws ApiException {
        CreateSchedulePayload payload = new CreateSchedulePayload();
        payload.setName(entity.getName());
        api.createSchedule(payload);
    }

    @Override
    protected void updateEntity(Schedule entity, EntityStatus entityStatus) {

    }

    @Override
    protected String getEntityIdentifierName(Schedule entitiy) {
        return "Schedule " + entitiy.getName();
    }
}
