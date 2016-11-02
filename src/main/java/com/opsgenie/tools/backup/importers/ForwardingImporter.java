package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Forwarding;
import com.ifountain.opsgenie.client.model.user.forward.AddForwardingRequest;
import com.ifountain.opsgenie.client.model.user.forward.ListForwardingsRequest;
import com.ifountain.opsgenie.client.model.user.forward.UpdateForwardingRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports forwarding from local directory called forwardings to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class ForwardingImporter extends BaseImporter<Forwarding> {
    public ForwardingImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(Forwarding oldEntity, Forwarding currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected Forwarding getBean() throws IOException, ParseException {
        return new Forwarding();
    }

    @Override
    protected String getImportDirectoryName() {
        return "forwardings";
    }

    @Override
    protected void addBean(Forwarding bean) throws ParseException, OpsGenieClientException, IOException {
        if (bean.getEndDate() != null && bean.getEndDate().getTime() > System.currentTimeMillis()) {
            AddForwardingRequest request = new AddForwardingRequest();
            request.setFromUser(bean.getFromUser());
            request.setToUser(bean.getToUser());
            request.setStartDate(bean.getStartDate());
            request.setEndDate(bean.getEndDate());
            request.setTimeZone(bean.getTimeZone());
            request.setAlias(bean.getAlias());
            getOpsGenieClient().user().addForwarding(request);
        }
    }

    @Override
    protected void updateBean(Forwarding bean) throws ParseException, OpsGenieClientException, IOException {
        if (bean.getEndDate() != null && bean.getEndDate().getTime() > System.currentTimeMillis()) {
            UpdateForwardingRequest request = new UpdateForwardingRequest();
            request.setFromUser(bean.getFromUser());
            request.setToUser(bean.getToUser());
            request.setStartDate(bean.getStartDate());
            request.setEndDate(bean.getEndDate());
            request.setTimeZone(bean.getTimeZone());
            request.setAlias(bean.getAlias());
            request.setId(bean.getId());
            getOpsGenieClient().user().updateForwarding(request);
        }
    }

    @Override
    protected List<Forwarding> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListForwardingsRequest request = new ListForwardingsRequest();
        return getOpsGenieClient().user().listForwardings(request).getForwardings();
    }

    @Override
    protected String getEntityIdentifierName(Forwarding entitiy) {
        return "Forwarding from user " + entitiy.getFromUser();
    }
}
