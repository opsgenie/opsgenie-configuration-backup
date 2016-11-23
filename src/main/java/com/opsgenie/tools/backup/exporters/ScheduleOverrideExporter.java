package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Schedule;
import com.ifountain.opsgenie.client.model.beans.ScheduleOverride;
import com.ifountain.opsgenie.client.model.schedule.ListScheduleOverridesRequest;
import com.ifountain.opsgenie.client.model.schedule.ListSchedulesRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Schedule overrides from OpsGenie account to local directory called
 * scheduleOverrides
 *
 * @author Mehmet Mustafa Demir
 */
public class ScheduleOverrideExporter extends BaseExporter<ScheduleOverride> {
    private final Logger logger = LogManager.getLogger(ScheduleOverrideExporter.class);
    private ListScheduleOverridesRequest listScheduleOverridesRequest = null;

    public ScheduleOverrideExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "scheduleOverrides");
    }

    @Override
    protected String getBeanFileName(ScheduleOverride bean) {
        return bean.getUser() + "-" + bean.getAlias();
    }

    @Override
    public void export() {
        try {
            ListSchedulesRequest listSchedulesRequest = new ListSchedulesRequest();
            List<Schedule> scheduleList = getOpsGenieClient().schedule().listSchedules(listSchedulesRequest).getSchedules();
            listScheduleOverridesRequest = new ListScheduleOverridesRequest();
            for (Schedule schedule : scheduleList) {
                try {
                    listScheduleOverridesRequest.setSchedule(schedule.getName());
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
    protected List<ScheduleOverride> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        return getOpsGenieClient().schedule().listScheduleOverrides(listScheduleOverridesRequest).getOverrides();
    }
}
