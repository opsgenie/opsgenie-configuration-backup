package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.api.ScheduleOverrideApi;
import com.opsgenie.client.model.ListScheduleOverridesRequest;
import com.opsgenie.client.model.Schedule;
import com.opsgenie.client.model.ScheduleOverride;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class ScheduleOverrideExporter extends BaseExporter<ScheduleOverride> {

    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi overrideApi = new ScheduleOverrideApi();

    private static String scheduleId;

    public ScheduleOverrideExporter(String backupRootDirectory) {
        super(backupRootDirectory, "scheduleOverrides");
    }

    @Override
    protected String getBeanFileName(ScheduleOverride bean) {
        return bean.getUser() + "-" + bean.getAlias();
    }

    @Override
    public void export() {
        try {
            List<Schedule> scheduleList = scheduleApi.listSchedules(Collections.<String>emptyList()).getData();
            for (Schedule schedule : scheduleList) {
                try {
                    scheduleId = schedule.getId();
                    List<ScheduleOverride> overrides = retrieveEntities();
                    if (overrides != null && overrides.size() > 0) {
                        File scheduleDirectory = new File(getExportDirectory().getAbsolutePath() + "/" + schedule.getName());
                        scheduleDirectory.mkdirs();
                        for (ScheduleOverride override : overrides) {
                            exportFile(getExportDirectory().getAbsolutePath() + "/" + schedule.getName() + "/" + getBeanFileName(override) + ".json", override);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error at Listing schedule overrides for schedule " + schedule.getName(), e);
                }

            }
        } catch (Exception e) {
            logger.error("Error at Listing schedules for schedule overrides", e);
        }

    }

    @Override
    protected List<ScheduleOverride> retrieveEntities() throws ParseException, IOException, ApiException {
        return overrideApi.listScheduleOverride(new ListScheduleOverridesRequest().identifier(scheduleId)).getData();
    }
}
