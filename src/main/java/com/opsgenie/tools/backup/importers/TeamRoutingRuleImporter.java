package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.*;
import com.ifountain.opsgenie.client.model.team.ListTeamsRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.AddTeamRoutingRuleRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.ListTeamRoutingRulesRequest;
import com.ifountain.opsgenie.client.model.team.routing_rule.UpdateTeamRoutingRuleRequest;
import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.BackupUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class imports Team Routing rules from local directory called teamRoutingRules to Opsgenie account.
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
    protected int checkEntities(TeamRoutingRule oldEntity, TeamRoutingRule currentEntity) {
        if (oldEntity.getId().equals(currentEntity.getId())) {
            if (!isSame(oldEntity, currentEntity))
                return 1;
            return 0;
        }
        return -1;
    }

    @Override
    protected TeamRoutingRule getBean() throws IOException, ParseException {
        return new TeamRoutingRule();
    }

    @Override
    protected String getImportDirectoryName() {
        return "teamRoutingRules";
    }

    public void restore() {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");
        try {
            ListTeamsRequest listTeamsRequest = new ListTeamsRequest();
            List<Team> teamList = getOpsGenieClient().team().listTeams(listTeamsRequest).getTeams();
            File[] fileList = getImportDirectory().listFiles();
            for (File teamDirectory : fileList) {
                if (teamDirectory.exists() && teamDirectory.isDirectory()) {
                    for (Team team : teamList) {
                        if (team.getName().equals(teamDirectory.getName())) {
                            teamName = team.getName();
                            List<TeamRoutingRule> backups = new ArrayList<TeamRoutingRule>();
                            String[] files = BackupUtils.getFileListOf(teamDirectory);
                            for (String fileName : files) {
                                try {
                                    String beanJson = BackupUtils.readFileAsJson(teamDirectory.getAbsolutePath() + "/" + fileName);
                                    TeamRoutingRule bean = getBean();
                                    JsonUtils.fromJson(bean, beanJson);
                                    backups.add(bean);
                                } catch (Exception e) {
                                    logger.error("Error at reading team routing rule for team " + teamName + " file name " + fileName, e);
                                }
                            }
                            try {
                                importEntities(backups, retrieveEntities());
                            } catch (Exception e) {
                                logger.error("Error at restoring " + getImportDirectoryName() + " for team " + teamName, e);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error at restoring " + getImportDirectoryName(), e);

        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
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
