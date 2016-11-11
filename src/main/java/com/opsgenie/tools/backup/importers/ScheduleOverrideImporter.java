package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Schedule;
import com.ifountain.opsgenie.client.model.beans.ScheduleOverride;
import com.ifountain.opsgenie.client.model.schedule.AddScheduleOverrideRequest;
import com.ifountain.opsgenie.client.model.schedule.ListScheduleOverridesRequest;
import com.ifountain.opsgenie.client.model.schedule.ListSchedulesRequest;
import com.ifountain.opsgenie.client.model.schedule.UpdateScheduleOverrideRequest;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class imports Team Routing rules from local directory called teamRoutingRules to Opsgenie
 * account.
 *
 * @author Mehmet Mustafa Demir
 */
public class ScheduleOverrideImporter extends BaseImporter<ScheduleOverride> {
    private final Logger logger = LogManager.getLogger(ScheduleOverrideImporter.class);
    private String scheduleName;

    public ScheduleOverrideImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(ScheduleOverride oldEntity, ScheduleOverride currentEntity) {
        if (oldEntity.getAlias().equals(currentEntity.getAlias())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected ScheduleOverride getBean() throws IOException, ParseException {
        return new ScheduleOverride();
    }

    @Override
    protected String getImportDirectoryName() {
        return "overrides";
    }

    public void restore() throws RestoreException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.error("Error : " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipeed");
            return;
        }

        File[] fileList = getImportDirectory().listFiles();
        if (fileList == null || fileList.length == 0) {
            logger.error("Error : " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        List<Schedule> scheduleList = retrieveScheduleList();

        for (File scheduleDirectory : fileList) {
            Schedule schedule = findSchedule(scheduleDirectory, scheduleList);
            if (schedule != null) {
                importOverridesForSchedule(schedule, scheduleDirectory);
            }
        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    private Schedule findSchedule(File scheduleDirectory, List<Schedule> scheduleList) {
        if (!scheduleDirectory.exists() || !scheduleDirectory.isDirectory()) {
            return null;
        }
        for (Schedule schedule : scheduleList) {
            if (schedule.getName().equals(scheduleDirectory.getName())) {
                return schedule;
            }
        }
        return null;
    }

    private void importOverridesForSchedule(Schedule schedule, File scheduleDirectory) {
        scheduleName = schedule.getName();
        List<ScheduleOverride> backups = new ArrayList<ScheduleOverride>();
        String[] files = BackupUtils.getFileListOf(scheduleDirectory);

        for (String fileName : files) {
            ScheduleOverride bean = readEntity(scheduleDirectory.getName() + "/" + fileName);
            if (bean != null) {
                backups.add(bean);
            }
        }

        try {
            importEntities(backups, retrieveEntities());
        } catch (Exception e) {
            logger.error("Error at restoring " + getImportDirectoryName() + " for schedule " + scheduleName, e);
        }
    }

    private List<Schedule> retrieveScheduleList() throws RestoreException {
        try {
            ListSchedulesRequest request = new ListSchedulesRequest();
            return getOpsGenieClient().schedule().listSchedules(request).getSchedules();
        } catch (Exception e) {
            throw new RestoreException("Error at listing schedules for schedule overrides", e);
        }

    }

    @Override
    protected void addBean(ScheduleOverride bean) throws ParseException, OpsGenieClientException, IOException {
        AddScheduleOverrideRequest request = new AddScheduleOverrideRequest();
        request.setSchedule(scheduleName);
        request.setRotationIds(bean.getRotationIds());
        request.setUser(bean.getUser());
        request.setEndDate(bean.getEndDate());
        request.setStartDate(bean.getStartDate());
        request.setTimeZone(bean.getTimeZone());
        request.setAlias(bean.getAlias());
        getOpsGenieClient().schedule().addScheduleOverride(request);
    }

    @Override
    protected void updateBean(ScheduleOverride bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateScheduleOverrideRequest request = new UpdateScheduleOverrideRequest();
        request.setSchedule(scheduleName);
        request.setRotationIds(bean.getRotationIds());
        request.setUser(bean.getUser());
        request.setEndDate(bean.getEndDate());
        request.setStartDate(bean.getStartDate());
        request.setTimeZone(bean.getTimeZone());
        request.setAlias(bean.getAlias());
        getOpsGenieClient().schedule().updateScheduleOverride(request);
    }


    @Override
    protected List<ScheduleOverride> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListScheduleOverridesRequest listScheduleOverridesRequest = new ListScheduleOverridesRequest();
        listScheduleOverridesRequest.setSchedule(scheduleName);
        return getOpsGenieClient().schedule().listScheduleOverrides(listScheduleOverridesRequest).getOverrides();
    }


    @Override
    protected String getEntityIdentifierName(ScheduleOverride bean) {
        return "Schedule Override for user  " + bean.getUser() + " for schedule " + scheduleName;
    }
}
