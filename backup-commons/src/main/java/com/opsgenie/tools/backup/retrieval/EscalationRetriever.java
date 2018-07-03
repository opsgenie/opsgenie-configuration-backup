package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.EscalationApi;
import com.opsgenie.oas.sdk.model.Escalation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class EscalationRetriever implements EntityRetriever<Escalation>{

    private static final Logger logger = LoggerFactory.getLogger(EscalationRetriever.class);

    private static final EscalationApi escalationApi = new EscalationApi();

    @Override
    public List<Escalation> retrieveEntities() throws Exception {
        logger.info("Retrieving current escalation configurations");
        return apiAdapter.invoke(new Callable<List<Escalation>>() {
            @Override
            public List<Escalation> call() throws Exception {
                return escalationApi.listEscalations().getData();
            }
        });

    }
}
