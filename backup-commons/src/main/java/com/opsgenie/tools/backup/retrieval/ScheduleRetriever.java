package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.ScheduleApi;
import com.opsgenie.oas.sdk.api.ScheduleOverrideApi;
import com.opsgenie.oas.sdk.model.Schedule;
import com.opsgenie.tools.backup.dto.ScheduleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleRetriever implements EntityRetriever<ScheduleConfig> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRetriever.class);

    private static final ScheduleApi scheduleApi = new ScheduleApi();
    private static final ScheduleOverrideApi overrideApi = new ScheduleOverrideApi();

    @Override
    public List<ScheduleConfig> retrieveEntities() {
        logger.info("------------------------------------");
        logger.info("Retrieving current schedule configurations");
        List<ScheduleConfig> scheduleConfigs = new ArrayList<ScheduleConfig>();
        final List<Schedule> schedules = scheduleApi.listSchedules(Collections.singletonList("rotation")).getData();
        for (Schedule schedule : schedules) {
            ScheduleConfig scheduleConfig = new ScheduleConfig();
            scheduleConfig.setSchedule(schedule);
            scheduleConfigs.add(scheduleConfig);
        }
        return scheduleConfigs;

    }
}
