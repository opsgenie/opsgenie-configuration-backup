package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Team;
import com.ifountain.opsgenie.client.model.beans.TeamRoutingRule;
import com.ifountain.opsgenie.client.model.team.ListTeamsRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.AddTeamRoutingRuleRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.ListTeamRoutingRulesRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.UpdateTeamRoutingRuleRequest;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class imports Team Routing rules from local directory called teamRoutingRules to Opsgenie
 * account.
 *
 * @author Mehmet Mustafa Demir
 */
public class TeamRoutingRuleImporter extends BaseImporter<TeamRoutingRule> {
    private final Logger logger = LogManager.getLogger(TeamRoutingRuleImporter.class);
    private String teamName;

    public TeamRoutingRuleImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(opsGenieClient, backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(TeamRoutingRule oldEntity, TeamRoutingRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected TeamRoutingRule getBean() throws IOException, ParseException {
        return new TeamRoutingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teamRoutingRules";
    }

    public void restore() throws RestoreException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!getImportDirectory().exists()) {
            logger.error("Error : " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipeed");
            return;
        }

        File[] fileList = getImportDirectory().listFiles();
        if (fileList == null || fileList.length == 0) {
            logger.error("Error : " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        List<Team> teamList = retrieveTeamList();

        for (File teamDirectory : fileList) {
            Team team = findTeam(teamDirectory, teamList);
            if (team != null) {
                importRoutingRulesForTeam(team, teamDirectory);
            }
        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    private Team findTeam(File teamDirectory, List<Team> teamList) {
        if (teamDirectory.exists() && teamDirectory.isDirectory()) {
            return null;
        }
        for (Team team : teamList) {
            if (team.getName().equals(teamDirectory.getName())) {
                return team;
            }
        }
        return null;
    }

    private void importRoutingRulesForTeam(Team team, File teamDirectory) {
        teamName = team.getName();
        List<TeamRoutingRule> backups = new ArrayList<TeamRoutingRule>();
        String[] files = BackupUtils.getFileListOf(teamDirectory);

        for (String fileName : files) {
            TeamRoutingRule bean = readEntity(fileName);
            if (bean != null) {
                backups.add(bean);
            }
        }

        try {
            importEntities(backups, retrieveEntities());
        } catch (Exception e) {
            logger.error("Error at restoring " + getImportDirectoryName() + " for team " + teamName, e);
        }
    }

    private List<Team> retrieveTeamList() throws RestoreException {
        try {
            ListTeamsRequest listTeamsRequest = new ListTeamsRequest();
            return getOpsGenieClient().team().listTeams(listTeamsRequest).getTeams();
        } catch (Exception e) {
            throw new RestoreException("Error at listing teams for team routing rules", e);
        }

    }

    @Override
    protected void addBean(TeamRoutingRule bean) throws ParseException, OpsGenieClientException, IOException {
        AddTeamRoutingRuleRequest request = new AddTeamRoutingRuleRequest();
        request.setTeamName(teamName);
        request.setApplyOrder(bean.getApplyOrder());
        request.setConditionMatchType(bean.getConditionMatchType());
        request.setConditions(bean.getConditions());
        request.setName(bean.getName());
        request.setNotify(bean.getNotify());
        request.setRestrictions(bean.getRestrictions());
        getOpsGenieClient().team().addTeamRoutingRule(request);
    }

    @Override
    protected void updateBean(TeamRoutingRule bean) throws ParseException, OpsGenieClientException, IOException {
        UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setTeamName(teamName);
        request.setConditionMatchType(bean.getConditionMatchType());
        request.setConditions(bean.getConditions());
        request.setName(bean.getName());
        request.setRestrictions(bean.getRestrictions());
        request.setNotify(bean.getNotify());
        getOpsGenieClient().team().updateTeamRoutingRule(request);
    }


    @Override
    protected List<TeamRoutingRule> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListTeamRoutingRulesRequest listTeamRoutingRulesRequest = new ListTeamRoutingRulesRequest();
        listTeamRoutingRulesRequest.setTeamName(teamName);
        return getOpsGenieClient().team().listTeamRoutingRules(listTeamRoutingRulesRequest).getRules();
    }


    @Override
    protected String getEntityIdentifierName(TeamRoutingRule bean) {
        return "Team Routing rule  " + bean.getName() + " for team " + teamName;
    }
}
