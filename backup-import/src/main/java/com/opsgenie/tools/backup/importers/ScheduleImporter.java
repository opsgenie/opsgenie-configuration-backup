package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ScheduleApi;
import com.opsgenie.oas.sdk.api.ScheduleOverrideApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.dto.ScheduleConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.ScheduleRetriever;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ScheduleImporter extends BaseImporterWithRateLimiting<ScheduleConfig> {

    private static ScheduleApi scheduleApi = new ScheduleApi();
    private static ScheduleOverrideApi overrideApi = new ScheduleOverrideApi();

    public ScheduleImporter(String backupRootDirectory, RateLimitManager rateLimitManager, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, rateLimitManager, addEntity, updateEntitiy);
    }

    @Override
    protected EntityRetriever<ScheduleConfig> initializeEntityRetriever() {
        return new ScheduleRetriever(true);
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
    protected void createEntity(ScheduleConfig scheduleConfig) throws ApiException {
        throw new IllegalStateException("This should not happen because schedule template importer should create all schedules");
    }

    @Override
    protected void updateEntity(ScheduleConfig scheduleConfig, EntityStatus entityStatus) throws Exception {
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

        final UpdateScheduleRequest request = new UpdateScheduleRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(schedule.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setIdentifierType(UpdateScheduleRequest.IdentifierTypeEnum.NAME);
            request.setIdentifier(schedule.getName());
        }
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<UpdateScheduleResponse>() {
            @Override
            public UpdateScheduleResponse call() throws Exception {
                return scheduleApi.updateSchedule(request);
            }
        });


        List<ScheduleOverride> overridesFromConfig = scheduleConfig.getScheduleOverrideList();
        if (overridesFromConfig != null && !overridesFromConfig.isEmpty()) {
            logger.info("Importing schedule overrides for " + scheduleConfig.getSchedule().getName());
            ArrayList<ScheduleOverride> overridesReversed = new ArrayList<ScheduleOverride>(overridesFromConfig);
            Collections.reverse(overridesReversed);

            for (ScheduleOverride override : overridesReversed) {
                if (findOverrideByAlias(scheduleConfig.getSchedule().getName(), override) != null) {
                    updateScheduleOverride(scheduleConfig.getSchedule().getName(), override);
                } else {
                    createScheduleOverride(scheduleConfig.getSchedule().getName(), override);
                }
            }
        }
    }

    private ScheduleOverride findOverrideByAlias(String scheduleName, ScheduleOverride override) {

        for (ScheduleConfig scheduleConfig : currentConfigs) {
            if (scheduleName.equals(scheduleConfig.getSchedule().getName())) {
                for (ScheduleOverride currentConfigOverride : scheduleConfig.getScheduleOverrideList()) {
                    if (currentConfigOverride.getAlias().equals(override.getAlias())) {
                        return currentConfigOverride;
                    }
                }
            }
        }
        return null;
    }

    private void createScheduleOverride(String scheduleName, ScheduleOverride override) throws Exception {
        deleteRotationIds(override.getRotations());
        CreateScheduleOverridePayload createPayload = new CreateScheduleOverridePayload();
        createPayload.setRotations(override.getRotations());
        createPayload.setAlias(override.getAlias());
        createPayload.setEndDate(override.getEndDate());
        createPayload.setStartDate(override.getStartDate());
        createPayload.setUser(override.getUser());

        final CreateScheduleOverrideRequest createRequest = new CreateScheduleOverrideRequest();
        createRequest.setIdentifier(scheduleName);
        createRequest.setScheduleIdentifierType(CreateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        createRequest.setBody(createPayload);
        RetryPolicyAdapter.invoke(new Callable<CreateScheduleOverrideResponse>() {
            @Override
            public CreateScheduleOverrideResponse call() throws Exception {
                return overrideApi.createScheduleOverride(createRequest);
            }
        });

    }

    private void updateScheduleOverride(String scheduleName, ScheduleOverride override) throws Exception {
        deleteRotationIds(override.getRotations());
        UpdateScheduleOverridePayload updatePayload = new UpdateScheduleOverridePayload();
        updatePayload.setEndDate(override.getEndDate());
        updatePayload.setStartDate(override.getStartDate());
        updatePayload.setRotations(override.getRotations());
        updatePayload.setUser(override.getUser());

        final UpdateScheduleOverrideRequest updateRequest = new UpdateScheduleOverrideRequest();
        updateRequest.setAlias(override.getAlias());
        updateRequest.setIdentifier(scheduleName);
        updateRequest.setScheduleIdentifierType(UpdateScheduleOverrideRequest.ScheduleIdentifierTypeEnum.NAME);
        updateRequest.setBody(updatePayload);
        RetryPolicyAdapter.invoke(new Callable<UpdateScheduleOverrideResponse>() {
            @Override
            public UpdateScheduleOverrideResponse call() throws Exception {
                return overrideApi.updateScheduleOverride(updateRequest);
            }
        });

    }

    private void deleteRotationIds(List<ScheduleOverrideRotation> rotations) {
        for (ScheduleOverrideRotation rotation : rotations) {
            rotation.setId("");
        }
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

    @Override
    protected void updateTeamIds(ScheduleConfig entity) throws Exception {
        Map<String, String> teamIdMap = new TeamIdMapper(rateLimitManager).getTeamIdMap();
        if(entity.getSchedule() != null){
            TeamMeta ownerTeam = entity.getSchedule().getOwnerTeam();
            if(ownerTeam != null){
                String newTeamId = teamIdMap.get(ownerTeam.getName());
                if(newTeamId != null) {
                    ownerTeam.setId(newTeamId);
                }
            }
        }
    }
}
