package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.api.TeamRoutingRuleApi;
import com.opsgenie.client.model.*;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamRoutingRuleImporter extends BaseImporter<TeamRoutingRule> {

    private static TeamApi teamApi = new TeamApi();
    private static TeamRoutingRuleApi teamRoutingRuleApi = new TeamRoutingRuleApi();
    private final Logger logger = LogManager.getLogger(TeamRoutingRuleImporter.class);
    private String teamName;

    public TeamRoutingRuleImporter(String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        super(backupRootDirectory, addEntity, updateEntitiy);
    }

    @Override
    protected BeanStatus checkEntities(TeamRoutingRule oldEntity, TeamRoutingRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            return isSame(oldEntity, currentEntity) ? BeanStatus.NOT_CHANGED : BeanStatus.MODIFIED;
        }

        return BeanStatus.NOT_EXIST;
    }

    @Override
    protected TeamRoutingRule getBean() {
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
        if (!teamDirectory.exists() || !teamDirectory.isDirectory()) {
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
            TeamRoutingRule bean = readEntity(teamDirectory.getName() + "/" + fileName);
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
           return teamApi.listTeams(Collections.singletonList("member")).getData();
        } catch (Exception e) {
            throw new RestoreException("Error at listing teams for team routing rules", e);
        }

    }

    @Override
    protected void addBean(TeamRoutingRule bean) throws ApiException {

        CreateTeamRoutingRulePayload payload = new CreateTeamRoutingRulePayload();
        payload.setCriteria(bean.getCriteria());
        payload.setName(bean.getName());
        payload.setNotify(bean.getNotify());
        payload.setOrder(bean.getOrder());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setTimezone(bean.getTimezone());

        CreateTeamRoutingRuleRequest request = new CreateTeamRoutingRuleRequest();
        request.setIdentifier(teamName);
        request.setTeamIdentifierType(CreateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.setBody(payload);

        teamRoutingRuleApi.createTeamRoutingRule(request);
    }

    @Override
    protected void updateBean(TeamRoutingRule bean) throws ApiException {
        UpdateTeamRoutingRulePayload payload = new UpdateTeamRoutingRulePayload();
        payload.setCriteria(bean.getCriteria());
        payload.setName(bean.getName());
        payload.setNotify(bean.getNotify());
        payload.setTimeRestriction(bean.getTimeRestriction());
        payload.setTimezone(bean.getTimezone());

        UpdateTeamRoutingRuleRequest request = new UpdateTeamRoutingRuleRequest();
        request.setId(bean.getId());
        request.setIdentifier(teamName);
        request.setTeamIdentifierType(UpdateTeamRoutingRuleRequest.TeamIdentifierTypeEnum.NAME);
        request.body(payload);

        teamRoutingRuleApi.updateTeamRoutingRule(request);
    }


    @Override
    protected List<TeamRoutingRule> retrieveEntities() throws ApiException {
        ListTeamRoutingRulesRequest request = new ListTeamRoutingRulesRequest();
        request.setIdentifier(teamName);
        request.setTeamIdentifierType(ListTeamRoutingRulesRequest.TeamIdentifierTypeEnum.NAME);

        return teamRoutingRuleApi.listTeamRoutingRules(request).getData();
    }


    @Override
    protected String getEntityIdentifierName(TeamRoutingRule bean) {
        return "Team Routing rule  " + bean.getName() + " for team " + teamName;
    }
}
