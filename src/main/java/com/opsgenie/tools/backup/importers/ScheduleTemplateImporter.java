package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.CreateSchedulePayload;
import com.opsgenie.client.model.Schedule;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.ScheduleConfig;

import java.util.List;

public class ScheduleTemplateImporter extends BaseImporter<ScheduleConfig> {

    private static ScheduleApi api = new ScheduleApi();
    private List<ScheduleConfig> currentScheduleList;

    public ScheduleTemplateImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(ScheduleConfig entity) {
        for (ScheduleConfig scheduleConfig : currentScheduleList) {
            final Schedule currentSchedule = scheduleConfig.getSchedule();
            if (currentSchedule.getId().equals(entity.getSchedule().getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (currentSchedule.getName().equals(entity.getSchedule().getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        currentScheduleList = EntityListService.listSchedules();
    }

    @Override
    protected ScheduleConfig getNewInstance() {
        return new ScheduleConfig();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void createEntity(ScheduleConfig entity) throws ApiException {
        CreateSchedulePayload payload = new CreateSchedulePayload();
        payload.setName(entity.getSchedule().getName());
        api.createSchedule(payload);
    }

    @Override
    protected void updateEntity(ScheduleConfig entity, EntityStatus entityStatus) {

    }

    @Override
    protected String getEntityIdentifierName(ScheduleConfig entity) {
        return "Schedule " + entity.getSchedule().getName();
    }
}
