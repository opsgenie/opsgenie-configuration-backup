package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Team;
import com.ifountain.opsgenie.client.model.team.AddTeamRequest;
import com.ifountain.opsgenie.client.model.team.ListTeamsRequest;
import com.ifountain.opsgenie.client.model.team.UpdateTeamRequest;
import com.opsgenie.tools.backup.BackupUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class imports Teams from local directory called teams to Opsgenie account.
 *
 * @author Mehmet Mustafa Demir
 */
public class TeamImporter extends BaseImporter<Team> {
    public TeamImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(Team oldEntity, Team currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        if (oldEntity.getName().equals(currentEntity.getName())) {
            oldEntity.setId(currentEntity.getId());
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected Team getBean() throws IOException, ParseException {
        return new Team();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teams";
    }

    @Override
    protected void addBean(Team bean) throws ParseException, OpsGenieClientException, IOException {
        AddTeamRequest request = new AddTeamRequest();
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setMembers(bean.getMembers());
        getOpsGenieClient().team().addTeam(request);
    }

    @Override
    protected void updateBean(Team bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setId(bean.getId());
        request.setName(bean.getName());
        if (BackupUtils.checkValidString(bean.getDescription()))
            request.setDescription(bean.getDescription());
        request.setMembers(bean.getMembers());
        getOpsGenieClient().team().updateTeam(request);
    }

    @Override
    protected List<Team> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListTeamsRequest request = new ListTeamsRequest();
        return getOpsGenieClient().team().listTeams(request).getTeams();
    }

    @Override
    protected String getEntityIdentifierName(Team entitiy) {
        return "Team " + entitiy.getName();
    }

    @Override
    protected boolean isSame(Team oldEntity, Team currentEntity) {
        oldEntity.setSchedules(null);
        oldEntity.setEscalations(null);
        currentEntity.setSchedules(null);
        currentEntity.setEscalations(null);
        return super.isSame(oldEntity, currentEntity);
    }
}
