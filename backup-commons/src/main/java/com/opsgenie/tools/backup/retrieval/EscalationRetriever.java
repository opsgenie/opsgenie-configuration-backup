package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.EscalationApi;
import com.opsgenie.oas.sdk.model.Escalation;
import com.opsgenie.oas.sdk.model.EscalationRule;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class EscalationRetriever implements EntityRetriever<Escalation>{

    private static final Logger logger = LoggerFactory.getLogger(EscalationRetriever.class);

    private static final EscalationApi escalationApi = new EscalationApi();

    @Override
    public List<Escalation> retrieveEntities() throws Exception {
        logger.info("Retrieving current escalation configurations");
        List<Escalation> escalationList = RetryPolicyAdapter.invoke(new Callable<List<Escalation>>() {
            @Override
            public List<Escalation> call() {
                return escalationApi.listEscalations().getData();
            }
        });

        for (Escalation escalation : escalationList) {
            sortEscalationRules(escalation);
        }
        return escalationList;
    }

    private void sortEscalationRules(Escalation escalation) {
        Collections.sort(escalation.getRules(), new Comparator<EscalationRule>() {
            @Override
            public int compare(EscalationRule o1, EscalationRule o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
    }
}
