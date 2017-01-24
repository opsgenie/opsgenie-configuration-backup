package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Schedule;
import com.ifountain.opsgenie.client.model.schedule.AddScheduleRequest;
import com.ifountain.opsgenie.client.model.schedule.ListSchedulesRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

// Creates all schedules with just their names to prevent "schedule not found" errors while importing escalations
// ScheduleImporter imports schedules after escalations
public class ScheduleTemplateImporter extends BaseImporter<Schedule> {
    public ScheduleTemplateImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(Schedule oldEntity, Schedule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
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
        getOpsGenieClient().schedule().addSchedule(request);
    }

    @Override
    protected void updateBean(Schedule bean) throws ParseException, OpsGenieClientException, IOException {

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
