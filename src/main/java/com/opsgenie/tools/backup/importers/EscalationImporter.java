package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Escalation;
import com.ifountain.opsgenie.client.model.escalation.AddEscalationRequest;
import com.ifountain.opsgenie.client.model.escalation.ListEscalationsRequest;
import com.ifountain.opsgenie.client.model.escalation.UpdateEscalationRequest;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports Escalations from local directory called escalations to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class EscalationImporter extends BaseImporter<Escalation> {
    public EscalationImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected int checkEntities(Escalation oldEntity, Escalation currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        } else if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        }
        return -1;
    }

    @Override
    protected Escalation getBean() throws IOException, ParseException {
        return new Escalation();
    }

    @Override
    protected String getImportDirectoryName() {
        return "escalations";
    }

    @Override
    protected void addBean(Escalation bean) throws ParseException, OpsGenieClientException, IOException {
        AddEscalationRequest request = new AddEscalationRequest();
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setTeam(bean.getTeam());
        request.setRepeatInterval(bean.getRepeatInterval());
        request.setRules(bean.getRules());
        getOpsGenieClient().escalation().addEscalation(request);
    }

    @Override
    protected void updateBean(Escalation bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateEscalationRequest request = new UpdateEscalationRequest();
        request.setId(bean.getId());
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setTeam(bean.getTeam());
        request.setRepeatInterval(bean.getRepeatInterval());
        request.setRules(bean.getRules());
        getOpsGenieClient().escalation().updateEscalation(request);
    }

    @Override
    protected List<Escalation> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListEscalationsRequest listEscalationsRequest = new ListEscalationsRequest();
        return getOpsGenieClient().escalation().listEscalations(listEscalationsRequest).getEscalations();
    }

    @Override
    protected String getEntityIdentifierName(Escalation entitiy) {
        return "Escalation " + entitiy.getName();
    }

}
