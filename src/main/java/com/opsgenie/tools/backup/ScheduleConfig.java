package com.opsgenie.tools.backup;

import com.opsgenie.oas.sdk.model.Schedule;
import com.opsgenie.oas.sdk.model.ScheduleOverride;

import java.util.List;

public class ScheduleConfig {

    private Schedule schedule;
    private List<ScheduleOverride> scheduleOverrideList;

    public Schedule getSchedule() {
        return schedule;
    }

    public ScheduleConfig setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public List<ScheduleOverride> getScheduleOverrideList() {
        return scheduleOverrideList;
    }

    public ScheduleConfig setScheduleOverrideList(List<ScheduleOverride> scheduleOverrideList) {
        this.scheduleOverrideList = scheduleOverrideList;
        return this;
    }
}
