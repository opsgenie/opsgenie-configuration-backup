package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.EscalationApi;
import com.opsgenie.oas.sdk.model.Escalation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EscalationRetriever implements EntityRetriever<Escalation>{

    private static final Logger logger = LoggerFactory.getLogger(EscalationRetriever.class);

    private static final EscalationApi escalationApi = new EscalationApi();

    @Override
    public List<Escalation> retrieveEntities() {
        logger.info("------------------------------------");
        logger.info("Retrieving current escalation configurations");
        return escalationApi.listEscalations().getData();
    }
}
