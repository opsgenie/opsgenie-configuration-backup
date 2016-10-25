package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Schedule;
import com.ifountain.opsgenie.client.model.beans.ScheduleRotation;
import com.ifountain.opsgenie.client.model.schedule.AddScheduleRequest;
import com.ifountain.opsgenie.client.model.schedule.ListSchedulesRequest;
import com.ifountain.opsgenie.client.model.schedule.UpdateScheduleRequest;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports Schedules from local directory called schedules to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class ScheduleImporter extends BaseImporter<Schedule> {
    public ScheduleImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected int checkEntities(Schedule oldEntity, Schedule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        } else if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        }
        return -1;
    }

    @Override
    protected Schedule getBean() throws IOException, ParseException {
        return new Schedule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void addBean(Schedule bean) throws ParseException, OpsGenieClientException, IOException {
        AddScheduleRequest request = new AddScheduleRequest();
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setTimeZone(bean.getTimeZone());
        request.setEnabled(bean.isEnabled());
        request.setTeam(bean.getTeam());
        if (bean.getRotations() != null && bean.getRotations().size() > 0) {
            for (ScheduleRotation rotation : bean.getRotations()) {
                if (rotation.getRotationLength() < 1)
                    rotation.setRotationLength(1);
            }
            request.setRotations(bean.getRotations());
        }
        getOpsGenieClient().schedule().addSchedule(request);
    }

    @Override
    protected void updateBean(Schedule bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateScheduleRequest request = new UpdateScheduleRequest();
        request.setId(bean.getId());
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setTimeZone(bean.getTimeZone());
        request.setEnabled(bean.isEnabled());
        request.setTeam(bean.getTeam());
        if (bean.getRotations() != null && bean.getRotations().size() > 0) {
            for (ScheduleRotation rotation : bean.getRotations()) {
                if (rotation.getRotationLength() < 1)
                    rotation.setRotationLength(1);
            }
            request.setRotations(bean.getRotations());
        }
        getOpsGenieClient().schedule().updateSchedule(request);
    }

    @Override
    protected List<Schedule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListSchedulesRequest listSchedulesRequest = new ListSchedulesRequest();
        return getOpsGenieClient().schedule().listSchedules(listSchedulesRequest).getSchedules();
    }

    @Override
    protected String getEntityIdentifierName(Schedule entitiy) {
        return "Schedule " + entitiy.getName();
    }

    @Override
    protected boolean isSame(Schedule oldEntity, Schedule currentEntity) {
        if (oldEntity.getRotations() != null && currentEntity.getRotations() != null
                && oldEntity.getRotations().size() == currentEntity.getRotations().size()) {
            for (int i = 0; i < oldEntity.getRotations().size(); i++) {
                oldEntity.getRotations().get(i).setId(null);
                currentEntity.getRotations().get(i).setId(null);
            }
        }
        return super.isSame(oldEntity, currentEntity);
    }
}
