package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.api.ScheduleOverrideApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleOverrideImporter extends BaseImporter<ScheduleOverride> {

    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi scheduleOverrideApi = new ScheduleOverrideApi();
    private final Logger logger = LogManager.getLogger(ScheduleOverrideImporter.class);
    private String scheduleName;

    public ScheduleOverrideImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(ScheduleOverride oldEntity, ScheduleOverride currentEntity) {
        if (oldEntity.getAlias().equals(currentEntity.getAlias())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected ScheduleOverride getBean() {
        return new ScheduleOverride();
    }

    @Override
    protected String getImportDirectoryName() {
        return "scheduleOverrides";
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
            return scheduleApi.listSchedules(Collections.singletonList("rotation")).getData();
        } catch (Exception e) {
            throw new RestoreException("Error at listing schedules for schedule overrides", e);
        }

    }

    @Override
    protected void addBean(ScheduleOverride bean) throws ApiException {
        CreateScheduleOverridePayload payload = new CreateScheduleOverridePayload();
        payload.setUser(bean.getUser());
        payload.setAlias(bean.getAlias());
        payload.setEndDate(bean.getEndDate());
        payload.setStartDate(bean.getStartDate());
        payload.setRotations(bean.getRotations());

        CreateScheduleOverrideRequest request = new CreateScheduleOverrideRequest();
        request.setIdentifier(scheduleName);
        request.setScheduleIdentifierType(CreateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        request.setBody(payload);

        scheduleOverrideApi.createScheduleOverride(request);
    }

    @Override
    protected void updateBean(ScheduleOverride bean) throws ApiException {
        UpdateScheduleOverridePayload payload = new UpdateScheduleOverridePayload();
        payload.setUser(bean.getUser());
        payload.setEndDate(bean.getEndDate());
        payload.setStartDate(bean.getStartDate());
        payload.setRotations(bean.getRotations());

        UpdateScheduleOverrideRequest request = new UpdateScheduleOverrideRequest();
        request.setAlias(bean.getAlias());
        request.setBody(payload);
        request.setIdentifier(scheduleName);
        request.setScheduleIdentifierType(UpdateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);

        scheduleOverrideApi.updateScheduleOverride(request);
    }


    @Override
    protected List<ScheduleOverride> retrieveEntities() throws ApiException {
        ListScheduleOverridesRequest listScheduleOverridesRequest = new ListScheduleOverridesRequest();
        listScheduleOverridesRequest.setIdentifier(scheduleName);
        listScheduleOverridesRequest.setScheduleIdentifierType(ListScheduleOverridesRequest.ScheduleIdentifierTypeEnum.NAME);
        return scheduleOverrideApi.listScheduleOverride(listScheduleOverridesRequest).getData();
    }


    @Override
    protected String getEntityIdentifierName(ScheduleOverride bean) {
        return "Schedule Override for user  " + bean.getUser() + " for schedule " + scheduleName;
    }
}
