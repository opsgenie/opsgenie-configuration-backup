package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.ForwardingRuleApi;
import com.opsgenie.oas.sdk.model.ForwardingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ForwardingRetriever implements EntityRetriever<ForwardingRule> {

    private static final Logger logger = LoggerFactory.getLogger(ForwardingRetriever.class);

    private static ForwardingRuleApi forwardingRuleApi = new ForwardingRuleApi();

    @Override
    public List<ForwardingRule> retrieveEntities() {
        logger.info("Retrieving current forwardings");
        return forwardingRuleApi.listForwardingRules().getData();
    }
}
