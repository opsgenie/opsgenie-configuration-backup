package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ScheduleApi;
import com.opsgenie.oas.sdk.model.CreateSchedulePayload;
import com.opsgenie.oas.sdk.model.Schedule;
import com.opsgenie.tools.backup.dto.ScheduleConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.ScheduleRetriever;

public class ScheduleTemplateImporter extends BaseImporter<ScheduleConfig> {

    private static ScheduleApi api = new ScheduleApi();

    public ScheduleTemplateImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityRetriever<ScheduleConfig> initializeEntityRetriever() {
        return new ScheduleRetriever();
    }

    @Override
    protected EntityStatus checkEntity(ScheduleConfig entity) {
        for (ScheduleConfig scheduleConfig : currentConfigs) {
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
