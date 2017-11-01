package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.ScheduleConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.ScheduleRetriever;

public class ScheduleExporter extends BaseExporter<ScheduleConfig> {

    public ScheduleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "schedules");
    }

    @Override
    protected EntityRetriever<ScheduleConfig> initializeEntityRetriever() {
        return new ScheduleRetriever();
    }

    @Override
    protected String getEntityFileName(ScheduleConfig scheduleConfig) {
        return scheduleConfig.getSchedule().getName() + "-" + scheduleConfig.getSchedule().getId();
    }
}
