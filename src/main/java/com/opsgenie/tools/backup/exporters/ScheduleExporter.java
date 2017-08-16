package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.Schedule;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class ScheduleExporter extends BaseExporter<Schedule> {

    private static ScheduleApi scheduleApi = new ScheduleApi();

    public ScheduleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "schedules");
    }

    @Override
    protected String getBeanFileName(Schedule bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Schedule> retrieveEntities() throws ParseException, IOException, ApiException {
        return scheduleApi.listSchedules(Collections.singletonList("rotation")).getData();
    }
}
