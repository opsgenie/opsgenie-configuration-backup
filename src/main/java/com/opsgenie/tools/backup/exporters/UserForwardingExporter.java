package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.ForwardingRuleApi;
import com.opsgenie.client.model.ForwardingRule;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class UserForwardingExporter extends BaseExporter<ForwardingRule> {

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();

    public UserForwardingExporter(String backupRootDirectory) {
        super(backupRootDirectory, "forwardings");
    }

    @Override
    protected String getBeanFileName(ForwardingRule bean) {
        return bean.getFromUser() + "-" + bean.getId();
    }


    @Override
    protected List<ForwardingRule> retrieveEntities() throws ParseException, IOException, ApiException {
        return forwardingRuleApi.listForwardingRules().getData();
    }
}
