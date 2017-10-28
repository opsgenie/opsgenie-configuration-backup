package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ScheduleApi;
import com.opsgenie.oas.sdk.api.ScheduleOverrideApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.util.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.dto.ScheduleConfig;

import java.util.ArrayList;
import java.util.List;

public class ScheduleImporter extends BaseImporter<ScheduleConfig> {

    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi scheduleOverrideApi = new ScheduleOverrideApi();
    private List<ScheduleConfig> currentScheduleConfigs = new ArrayList<ScheduleConfig>();

    public ScheduleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(ScheduleConfig entity) {
        for (ScheduleConfig scheduleConfig : currentScheduleConfigs) {
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
        currentScheduleConfigs = EntityListService.listSchedules();
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
    protected void createEntity(ScheduleConfig scheduleConfig) throws ApiException {
        throw new IllegalStateException("This should not happen because schedule template importer should create all schedules");
    }

    @Override
    protected void updateEntity(ScheduleConfig scheduleConfig, EntityStatus entityStatus) throws ApiException {
        UpdateSchedulePayload payload = new UpdateSchedulePayload();
        final Schedule schedule = scheduleConfig.getSchedule();
        payload.setName(schedule.getName());

        if (BackupUtils.checkValidString(schedule.getDescription())) {
            payload.setDescription(schedule.getDescription());
        }

        payload.setTimezone(schedule.getTimezone());
        payload.setEnabled(schedule.isEnabled());
        payload.setOwnerTeam(schedule.getOwnerTeam());
        payload.setRotations(constructCreateScheduleRotationPayloads(schedule));

        UpdateScheduleRequest request = new UpdateScheduleRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(schedule.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setIdentifierType(UpdateScheduleRequest.IdentifierTypeEnum.NAME);
            request.setIdentifier(schedule.getName());
        }
        request.setBody(payload);

        scheduleApi.updateSchedule(request);
        logger.info("Importing schedule overrides for " + scheduleConfig.getSchedule().getName());
    }

    private List<CreateScheduleRotationPayload> constructCreateScheduleRotationPayloads(Schedule schedule) {

        List<CreateScheduleRotationPayload> createScheduleRotationPayloadList = new ArrayList<CreateScheduleRotationPayload>();

        if (schedule.getRotations() != null && schedule.getRotations().size() > 0) {

            for (ScheduleRotation rotation : schedule.getRotations()) {
                if (rotation.getLength() == null || rotation.getLength() < 1) {
                    rotation.setLength(1);
                }

                for (Recipient recipient : rotation.getParticipants()) {
                    recipient.setId(null);
                }

                createScheduleRotationPayloadList.add(new CreateScheduleRotationPayload()
                        .name(rotation.getName())
                        .startDate(rotation.getStartDate())
                        .endDate(rotation.getEndDate())
                        .length(rotation.getLength())
                        .participants(rotation.getParticipants())
                        .timeRestriction(rotation.getTimeRestriction())
                        .type(CreateScheduleRotationPayload.TypeEnum.fromValue(rotation.getType().getValue()))
                );
            }
        }

        return createScheduleRotationPayloadList;
    }

    @Override
    protected String getEntityIdentifierName(ScheduleConfig entity) {
        return "Schedule " + entity.getSchedule().getName();
    }
}
