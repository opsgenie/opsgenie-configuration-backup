package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.EscalationApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.EntityListService;

import java.util.ArrayList;
import java.util.List;

public class EscalationImporter extends BaseImporter<Escalation> {

    private static EscalationApi api = new EscalationApi();
    private List<Escalation> currentEscalations = new ArrayList<Escalation>();

    public EscalationImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(Escalation entity) throws ApiException {
        for (Escalation escalation : currentEscalations) {
            if (escalation.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (escalation.getName().equals(entity.getName())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        currentEscalations = EntityListService.listEscalations();
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
    protected void createEntity(Escalation entity) throws ApiException {
        CreateEscalationPayload payload = new CreateEscalationPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription())) {
            payload.setDescription(entity.getDescription());
        }

        for (EscalationRule escalationRule : entity.getRules()) {
            escalationRule.getRecipient().setId(null);
        }

        payload.setOwnerTeam(entity.getOwnerTeam());
        payload.setRules(entity.getRules());

        api.createEscalation(payload);
    }

    @Override
    protected void updateEntity(Escalation entity, EntityStatus entityStatus) throws ApiException {

        UpdateEscalationPayload payload = new UpdateEscalationPayload();
        payload.setName(entity.getName());

        if (BackupUtils.checkValidString(entity.getDescription())) {
            payload.setDescription(entity.getDescription());
        }

        payload.setOwnerTeam(entity.getOwnerTeam());

        for (EscalationRule escalationRule : entity.getRules()) {
            escalationRule.getRecipient().setId(null);
        }

        payload.setRules(entity.getRules());

        UpdateEscalationRequest request = new UpdateEscalationRequest();
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(entity.getId());
            request.setIdentifierType(UpdateEscalationRequest.IdentifierTypeEnum.ID);
        } else {
            request.setIdentifier(entity.getName());
            request.setIdentifierType(UpdateEscalationRequest.IdentifierTypeEnum.NAME);
        }
        request.setBody(payload);

        api.updateEscalation(request);
    }

    @Override
    protected String getEntityIdentifierName(Escalation entity) {
        return "Escalation " + entity.getName();
    }

}
