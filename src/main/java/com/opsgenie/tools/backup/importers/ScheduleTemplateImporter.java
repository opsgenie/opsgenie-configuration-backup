package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.CreateSchedulePayload;
import com.opsgenie.client.model.Schedule;

import java.util.Collections;
import java.util.List;

public class ScheduleTemplateImporter extends BaseImporter<Schedule> {

    private static ScheduleApi api = new ScheduleApi();

    public ScheduleTemplateImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
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
    protected Schedule getBean() {
        return new Schedule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void addBean(Schedule bean) throws ApiException {
        CreateSchedulePayload payload = new CreateSchedulePayload();
        payload.setName(bean.getName());
        api.createSchedule(payload);
    }

    @Override
    protected void updateBean(Schedule bean) {

    }

    @Override
    protected List<Schedule> retrieveEntities() throws ApiException {
        return api.listSchedules(Collections.singletonList("rotation")).getData();
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
