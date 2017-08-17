package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.CreateSchedulePayload;
import com.opsgenie.client.model.GetScheduleRequest;
import com.opsgenie.client.model.Schedule;

import java.util.Collections;
import java.util.List;

public class ScheduleTemplateImporter extends BaseImporter<Schedule> {

    private static ScheduleApi api = new ScheduleApi();

    public ScheduleTemplateImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected void getEntityWithId(Schedule schedule) throws ApiException {
        api.getSchedule(new GetScheduleRequest().identifier(schedule.getId()));
    }

    @Override
    protected Schedule getBean() {
        return new Schedule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void addBean(Schedule bean) throws ApiException {
        CreateSchedulePayload payload = new CreateSchedulePayload();
        payload.setName(bean.getName());
        api.createSchedule(payload);
    }

    @Override
    protected void updateBean(Schedule bean) {

    }

    @Override
    protected String getEntityIdentifierName(Schedule entitiy) {
        return "Schedule " + entitiy.getName();
    }
}
