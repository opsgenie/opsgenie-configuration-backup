package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.ScheduleApi;
import com.opsgenie.oas.sdk.api.ScheduleOverrideApi;
import com.opsgenie.oas.sdk.model.ListScheduleOverrideResponse;
import com.opsgenie.oas.sdk.model.ListScheduleOverridesRequest;
import com.opsgenie.oas.sdk.model.Schedule;
import com.opsgenie.oas.sdk.model.ScheduleOverride;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.ScheduleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleRetriever implements EntityRetriever<ScheduleConfig> {

    private final boolean includeOverrides;

    public ScheduleRetriever() {
        includeOverrides = false;
    }

    public ScheduleRetriever(boolean includeOverrides) {
        this.includeOverrides = includeOverrides;
    }

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRetriever.class);

    private static final ScheduleApi scheduleApi = new ScheduleApi();
    private static final ScheduleOverrideApi overrideApi = new ScheduleOverrideApi();

    @Override
    public List<ScheduleConfig> retrieveEntities() {
        logger.info("Retrieving current schedule configurations");
        List<ScheduleConfig> scheduleConfigs = new ArrayList<ScheduleConfig>();
        final List<Schedule> schedules = scheduleApi.listSchedules(Collections.singletonList("rotation")).getData();
        for (Schedule schedule : schedules) {
            ScheduleConfig scheduleConfig = new ScheduleConfig();
            scheduleConfig.setSchedule(schedule);
            scheduleConfigs.add(scheduleConfig);
            if (includeOverrides) {
                scheduleConfig.setScheduleOverrideList(listScheduleOverrides(schedule));
            }
        }
        return scheduleConfigs;

    }

    private List<ScheduleOverride> listScheduleOverrides(Schedule schedule) {
        try {
            ListScheduleOverridesRequest listRequest = new ListScheduleOverridesRequest();
            listRequest.setIdentifier(schedule.getName());
            listRequest.setScheduleIdentifierType(ListScheduleOverridesRequest.ScheduleIdentifierTypeEnum.NAME);
            ListScheduleOverrideResponse response = overrideApi.listScheduleOverride(listRequest);
            if (response != null) {
                return getOverridesWithRotationNames(schedule, response.getData());
            }
        } catch (Exception e) {
            logger.error("Could not list schedule overrides for " + schedule.getId());
        }
        return Collections.emptyList();
    }

    private List<ScheduleOverride> getOverridesWithRotationNames(Schedule schedule, List<ScheduleOverride> overrideList) {

        List<ScheduleOverride> overrideListWithRotationNames = new ArrayList<ScheduleOverride>();
        for (ScheduleOverride override : overrideList){

            GetScheduleOverrideRequest request = new GetScheduleOverrideRequest();
            request.setAlias(override.getAlias());
            request.setIdentifier(schedule.getName());
            request.setScheduleIdentifierType(GetScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);

            GetScheduleOverrideResponse overrideResponse = overrideApi.getScheduleOverride(request);
            if (overrideResponse != null){
                overrideListWithRotationNames.add(overrideResponse.getData());
            }

        }
        return overrideListWithRotationNames;
    }
}
