package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.EscalationApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;

import java.util.List;

public class EscalationImporter extends BaseImporter<Escalation> {

    private static EscalationApi api = new EscalationApi();

    public EscalationImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected void getEntityWithId(Escalation escalation) throws ApiException {
        api.getEscalation(new GetEscalationRequest().identifier(escalation.getId()));
    }

    @Override
    protected Escalation getBean() {
        return new Escalation();
    }

    @Override
    protected String getImportDirectoryName() {
        return "escalations";
    }

    @Override
    protected void addBean(Escalation escalation) throws ApiException {
        CreateEscalationPayload payload = new CreateEscalationPayload();
        payload.setName(escalation.getName());

        if (BackupUtils.checkValidString(escalation.getDescription())){
            payload.setDescription(escalation.getDescription());
        }

        for (EscalationRule rule : escalation.getRules()) {
            rule.getRecipient().setId(null);
        }

        payload.setOwnerTeam(escalation.getOwnerTeam());
        payload.setRules(escalation.getRules());

        api.createEscalation(payload);
    }

    @Override
    protected void updateBean(Escalation bean) throws ApiException {

        UpdateEscalationPayload payload = new UpdateEscalationPayload();
        payload.setName(bean.getName());

        if (BackupUtils.checkValidString(bean.getDescription()))
            payload.setDescription(bean.getDescription());

        payload.setOwnerTeam(bean.getOwnerTeam());

        for (EscalationRule escalationRule: bean.getRules()) {
            escalationRule.getRecipient().setId(null);
        }

        payload.setRules(bean.getRules());

        UpdateEscalationRequest request = new UpdateEscalationRequest();
        request.setIdentifier(bean.getId());
        request.setIdentifierType(UpdateEscalationRequest.IdentifierTypeEnum.ID);
        request.setBody(payload);

        api.updateEscalation(request);
    }

    @Override
    protected String getEntityIdentifierName(Escalation entitiy) {
        return "Escalation " + entitiy.getName();
    }

}
