package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ScheduleApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.util.ArrayList;
import java.util.List;

public class ScheduleImporter extends BaseImporter<Schedule> {

    private static ScheduleApi api = new ScheduleApi();

    public ScheduleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected Schedule checkEntityWithName(Schedule schedule) throws ApiException {
        final GetScheduleRequest getScheduleRequest = new GetScheduleRequest().identifierType(GetScheduleRequest.IdentifierTypeEnum.NAME).identifier(schedule.getName());
        return api.getSchedule(getScheduleRequest).getData();
    }

    @Override
    protected Schedule checkEntityWithId(Schedule schedule) throws ApiException {
        final GetScheduleRequest getScheduleRequest = new GetScheduleRequest().identifier(schedule.getId());
        return api.getSchedule(getScheduleRequest).getData();
    }

    @Override
    protected Schedule getNewInstance() {
        return new Schedule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "schedules";
    }

    @Override
    protected void createEntity(Schedule entity) throws ApiException {
        CreateSchedulePayload payload = new CreateSchedulePayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription()))
            payload.setDescription(entity.getDescription());

        payload.setTimezone(entity.getTimezone());
        payload.setEnabled(entity.isEnabled());
        payload.setOwnerTeam(entity.getOwnerTeam());
        payload.setRotations(constructCreateScheduleRotationPayloads(entity));

        api.createSchedule(payload);
    }

    @Override
    protected void updateEntity(Schedule entity, EntityStatus entityStatus) throws ApiException {
        UpdateSchedulePayload payload = new UpdateSchedulePayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription()))
            payload.setDescription(entity.getDescription());

        payload.setTimezone(entity.getTimezone());
        payload.setEnabled(entity.isEnabled());
        payload.setOwnerTeam(entity.getOwnerTeam());
        payload.setRotations(constructCreateScheduleRotationPayloads(entity));

        UpdateScheduleRequest request = new UpdateScheduleRequest();
        request.setIdentifier(entity.getId());
        request.setIdentifierType(UpdateScheduleRequest.IdentifierTypeEnum.ID);
        request.setBody(payload);

        api.updateSchedule(request);
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
    protected String getEntityIdentifierName(Schedule entitiy) {
        return "Schedule " + entitiy.getName();
    }
}
