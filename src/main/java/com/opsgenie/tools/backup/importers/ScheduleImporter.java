package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.api.ScheduleOverrideApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;
import com.opsgenie.tools.backup.ScheduleConfig;

import java.util.ArrayList;
import java.util.List;

public class ScheduleImporter extends BaseImporter<ScheduleConfig> {

    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi scheduleOverrideApi = new ScheduleOverrideApi();
    private List<ScheduleConfig> currentSchedules = new ArrayList<ScheduleConfig>();

    public ScheduleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(ScheduleConfig entity) {
        for (ScheduleConfig scheduleConfig : currentSchedules) {
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
        currentSchedules = EntityListService.listSchedules();
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
        CreateSchedulePayload payload = new CreateSchedulePayload();
        final Schedule schedule = scheduleConfig.getSchedule();
        payload.setName(schedule.getName());

        if (BackupUtils.checkValidString(schedule.getDescription()))
            payload.setDescription(schedule.getDescription());

        payload.setTimezone(schedule.getTimezone());
        payload.setEnabled(schedule.isEnabled());
        payload.setOwnerTeam(schedule.getOwnerTeam());
        payload.setRotations(constructCreateScheduleRotationPayloads(schedule));

        scheduleApi.createSchedule(payload);
        for (ScheduleOverride override : scheduleConfig.getScheduleOverrideList()) {
            createScheduleOverride(override);
        }
    }

    private void createScheduleOverride(ScheduleOverride scheduleOverride) throws ApiException {
        CreateScheduleOverridePayload payload = new CreateScheduleOverridePayload();
        payload.setUser(scheduleOverride.getUser());
        payload.setAlias(scheduleOverride.getAlias());
        payload.setEndDate(scheduleOverride.getEndDate());
        payload.setStartDate(scheduleOverride.getStartDate());
        payload.setRotations(scheduleOverride.getRotations());

        CreateScheduleOverrideRequest request = new CreateScheduleOverrideRequest();
        request.setIdentifier(scheduleOverride.getParent().getId());
        request.setScheduleIdentifierType(CreateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        request.setBody(payload);

        scheduleOverrideApi.createScheduleOverride(request);
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

        for (ScheduleOverride override : scheduleConfig.getScheduleOverrideList()) {
            updateScheduleOverride(override);
        }
    }

    private void updateScheduleOverride(ScheduleOverride scheduleOverride) throws ApiException {
        UpdateScheduleOverridePayload payload = new UpdateScheduleOverridePayload();
        payload.setUser(scheduleOverride.getUser());
        payload.setEndDate(scheduleOverride.getEndDate());
        payload.setStartDate(scheduleOverride.getStartDate());
        payload.setRotations(scheduleOverride.getRotations());

        UpdateScheduleOverrideRequest request = new UpdateScheduleOverrideRequest();
        request.setAlias(scheduleOverride.getAlias());
        request.setBody(payload);
        request.setIdentifier(scheduleOverride.getParent().getId());
        request.setScheduleIdentifierType(UpdateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);

        scheduleOverrideApi.updateScheduleOverride(request);
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
