package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ForwardingRuleApi;
import com.opsgenie.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;

;

public class UserForwardingImporter extends BaseImporter<ForwardingRule> {

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();

    public UserForwardingImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected void getEntityWithId(ForwardingRule forwardingRule) throws ApiException {
        forwardingRuleApi.getForwardingRule(new GetForwardingRuleRequest().identifier(forwardingRule.getId()));
    }

    @Override
    protected ForwardingRule getBean() throws IOException, ParseException {
        return new ForwardingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "forwardings";
    }

    @Override
    protected void addBean(ForwardingRule bean) throws ApiException {
        if (bean.getEndDate() != null && bean.getEndDate().getMillis() < System.currentTimeMillis()) {
            logger.warn(getEntityIdentifierName(bean) + " end date is in the past.");
            return;
        }

        CreateForwardingRulePayload payload = new CreateForwardingRulePayload();
        payload.setFromUser(bean.getFromUser());
        payload.setToUser(bean.getToUser());
        payload.setStartDate(bean.getStartDate());
        payload.setEndDate(bean.getEndDate());
        payload.setAlias(bean.getAlias());

        forwardingRuleApi.createForwardingRule(payload);
    }

    @Override
    protected void updateBean(ForwardingRule bean) throws ApiException {
        if (bean.getEndDate() != null && bean.getEndDate().getMillis() < System.currentTimeMillis()) {
            logger.warn(getEntityIdentifierName(bean) + " end date is in the past.");
            return;
        }

        UpdateForwardingRulePayload payload = new UpdateForwardingRulePayload();
        payload.setFromUser(bean.getFromUser());
        payload.setToUser(bean.getToUser());
        payload.setStartDate(bean.getStartDate());
        payload.setEndDate(bean.getEndDate());

        UpdateForwardingRuleRequest request = new UpdateForwardingRuleRequest();
        request.setIdentifier(bean.getId());
        request.setIdentifierType(UpdateForwardingRuleRequest.IdentifierTypeEnum.ID);
        request.setBody(payload);

        forwardingRuleApi.updateForwardingRule(request);
    }

    @Override
    protected String getEntityIdentifierName(ForwardingRule entitiy) {
        return "Forwarding from user " + entitiy.getFromUser();
    }
}
