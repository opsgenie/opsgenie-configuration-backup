package com.opsgenie.tools.backup.importers;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.api.ForwardingRuleApi;
import com.opsgenie.oas.sdk.model.CreateForwardingRulePayload;
import com.opsgenie.oas.sdk.model.ForwardingRule;
import com.opsgenie.oas.sdk.model.UpdateForwardingRulePayload;
import com.opsgenie.oas.sdk.model.UpdateForwardingRuleRequest;
import com.opsgenie.tools.backup.EntityListService;

import java.util.ArrayList;
import java.util.List;

;

public class UserForwardingImporter extends BaseImporter<ForwardingRule> {

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();
    private List<ForwardingRule> currentForwardingRules = new ArrayList<ForwardingRule>();

    public UserForwardingImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected EntityStatus checkEntity(ForwardingRule entity) {
        for (ForwardingRule forwardingRule : currentForwardingRules) {
            if (forwardingRule.getId().equals(entity.getId())) {
                return EntityStatus.EXISTS_WITH_ID;
            } else if (forwardingRule.getToUser().equals(entity.getToUser())) {
                return EntityStatus.EXISTS_WITH_NAME;
            }
        }
        return EntityStatus.NOT_EXIST;
    }

    @Override
    protected void populateCurrentEntityList() throws ApiException {
        currentForwardingRules = EntityListService.listForwardingRules();
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
        if (EntityStatus.EXISTS_WITH_ID.equals(entityStatus)) {
            request.setIdentifier(entity.getId());
        } else if (EntityStatus.EXISTS_WITH_NAME.equals(entityStatus)) {
            request.setIdentifier(findForwardingRuleId(entity));
        }
        request.setBody(payload);

        forwardingRuleApi.updateForwardingRule(request);
    }

    private String findForwardingRuleId(ForwardingRule forwardingRuleToImport) {
        for (ForwardingRule forwardingRule : currentForwardingRules) {
            if (forwardingRule.getToUser().equals(forwardingRuleToImport.getToUser())) {
                return forwardingRule.getId();
            }
        }
        return null;
    }

    @Override
    protected String getEntityIdentifierName(ForwardingRule forwardingRule) {
        return "Forwarding from user " + forwardingRule.getFromUser().getUsername();
    }
}
