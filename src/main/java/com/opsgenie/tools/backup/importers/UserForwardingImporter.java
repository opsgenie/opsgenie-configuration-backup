package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ForwardingRuleApi;
import com.opsgenie.client.model.*;

;

public class UserForwardingImporter extends BaseImporter<ForwardingRule> {

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();

    public UserForwardingImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected ForwardingRule checkEntityWithName(ForwardingRule forwardingRule) throws ApiException {
        final GetForwardingRuleRequest getForwardingRuleRequest = new GetForwardingRuleRequest().identifierType(GetForwardingRuleRequest.IdentifierTypeEnum.ALIAS).identifier(forwardingRule.getAlias());
        return forwardingRuleApi.getForwardingRule(getForwardingRuleRequest).getData();
    }

    @Override
    protected ForwardingRule checkEntityWithId(ForwardingRule forwardingRule) throws ApiException {
        final GetForwardingRuleRequest getForwardingRuleRequest = new GetForwardingRuleRequest().identifier(forwardingRule.getId());
        return forwardingRuleApi.getForwardingRule(getForwardingRuleRequest).getData();
    }

    @Override
    protected ForwardingRule getNewInstance() {
        return new ForwardingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "forwardings";
    }

    @Override
    protected void createEntity(ForwardingRule entity) throws ApiException {
        if (entity.getEndDate() != null && entity.getEndDate().getMillis() < System.currentTimeMillis()) {
            logger.warn(getEntityIdentifierName(entity) + " end date is in the past.");
            return;
        }

        CreateForwardingRulePayload payload = new CreateForwardingRulePayload();
        payload.setFromUser(entity.getFromUser());
        payload.setToUser(entity.getToUser());
        payload.setStartDate(entity.getStartDate());
        payload.setEndDate(entity.getEndDate());
        payload.setAlias(entity.getAlias());

        forwardingRuleApi.createForwardingRule(payload);
    }

    @Override
    protected void updateEntity(ForwardingRule entity, EntityStatus entityStatus) throws ApiException {
        if (entity.getEndDate() != null && entity.getEndDate().getMillis() < System.currentTimeMillis()) {
            logger.warn(getEntityIdentifierName(entity) + " end date is in the past.");
            return;
        }

        UpdateForwardingRulePayload payload = new UpdateForwardingRulePayload();
        payload.setFromUser(entity.getFromUser());
        payload.setToUser(entity.getToUser());
        payload.setStartDate(entity.getStartDate());
        payload.setEndDate(entity.getEndDate());

        UpdateForwardingRuleRequest request = new UpdateForwardingRuleRequest();
        request.setIdentifier(entity.getId());
        request.setIdentifierType(UpdateForwardingRuleRequest.IdentifierTypeEnum.ID);
        request.setBody(payload);

        forwardingRuleApi.updateForwardingRule(request);
    }

    @Override
    protected String getEntityIdentifierName(ForwardingRule entitiy) {
        return "Forwarding from user " + entitiy.getFromUser();
    }
}
