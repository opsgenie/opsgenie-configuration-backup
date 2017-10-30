package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.dto.ScheduleConfig;

import java.util.List;

public class ScheduleExporter extends BaseExporter<ScheduleConfig> {

    public ScheduleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "schedules");
    }

    @Override
    protected String getEntityFileName(ScheduleConfig scheduleConfig) {
        return scheduleConfig.getSchedule().getName() + "-" + scheduleConfig.getSchedule().getId();
    }

    @Override
    protected List<ScheduleConfig> retrieveEntities() throws ApiException {
        return EntityListService.listSchedules();
    }
}
