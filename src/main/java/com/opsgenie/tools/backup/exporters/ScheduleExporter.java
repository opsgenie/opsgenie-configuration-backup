package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Schedule;
import com.ifountain.opsgenie.client.model.schedule.ListSchedulesRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Schedules from Opsgenie account to local directory called schedules
 *
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class ScheduleExporter extends BaseExporter<Schedule> {
    public ScheduleExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "schedules");
    }

    @Override
    protected String getBeanFileName(Schedule bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Schedule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListSchedulesRequest request = new ListSchedulesRequest();
        return getOpsGenieClient().schedule().listSchedules(request).getSchedules();
    }
}
