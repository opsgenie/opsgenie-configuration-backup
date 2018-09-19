package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.EscalationApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.EscalationRetriever;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.util.concurrent.Callable;

public class EscalationImporter extends BaseImporter<Escalation> {

    private static EscalationApi api = new EscalationApi();

    public EscalationImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityRetriever<Escalation> initializeEntityRetriever() {
        return new EscalationRetriever();
    }

    @Override
    protected EntityStatus checkEntity(Escalation entity) throws ApiException {
        for (Escalation escalation : currentConfigs) {
            if (escalation.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (escalation.getName().equals(entity.getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected Escalation getNewInstance() {
        return new Escalation();
    }

    @Override
    protected String getImportDirectoryName() {
        return "escalations";
    }

    @Override
    protected void createEntity(Escalation entity) throws Exception {
        final CreateEscalationPayload payload = new CreateEscalationPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription())) {
            payload.setDescription(entity.getDescription());
        }

        for (EscalationRule escalationRule : entity.getRules()) {
            escalationRule.getRecipient().setId(null);
        }

        payload.setRepeat(entity.getRepeat());
        payload.setOwnerTeam(entity.getOwnerTeam());
        payload.setRules(entity.getRules());

        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.createEscalation(payload);
            }
        });

    }

    @Override
    protected void updateEntity(Escalation entity, EntityStatus entityStatus) throws Exception {

        UpdateEscalationPayload payload = new UpdateEscalationPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription())) {
            payload.setDescription(entity.getDescription());
        }

        payload.setOwnerTeam(entity.getOwnerTeam());

        for (EscalationRule escalationRule : entity.getRules()) {
            escalationRule.getRecipient().setId(null);
        }

        if (entity.getRepeat() == null) {
            EscalationRepeat repeat = new EscalationRepeat();
            repeat.setWaitInterval(0);
            entity.setRepeat(repeat);
        }
        payload.setRepeat(entity.getRepeat());
        payload.setRules(entity.getRules());

        final UpdateEscalationRequest request = new UpdateEscalationRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(entity.getId());
            request.setIdentifierType(UpdateEscalationRequest.IdentifierTypeEnum.ID);
        } else {
            request.setIdentifier(entity.getName());
            request.setIdentifierType(UpdateEscalationRequest.IdentifierTypeEnum.NAME);
        }
        request.setBody(payload);
        RetryPolicyAdapter.invoke(new Callable<SuccessResponse>() {
            @Override
            public SuccessResponse call() throws Exception {
                return api.updateEscalation(request);
            }
        });

    }

    @Override
    protected String getEntityIdentifierName(Escalation entity) {
        return "Escalation " + entity.getName();
    }

    @Override
    protected void updateTeamIds(Escalation entity) {}

}
