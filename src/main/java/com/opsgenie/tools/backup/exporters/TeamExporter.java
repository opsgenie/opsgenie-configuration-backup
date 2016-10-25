package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Team;
import com.ifountain.opsgenie.client.model.team.ListTeamsRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * This class exports Teams from Opsgenie account to local directory called teams
 *
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class TeamExporter extends BaseExporter<Team> {
    public TeamExporter(OpsGenieClient opsGenieClient, String backupRootDirectory) {
        super(opsGenieClient, backupRootDirectory, "teams");
    }

    @Override
    protected String getBeanFileName(Team bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Team> retrieveEntities() throws ParseException, OpsGenieClientException, IOException {
        ListTeamsRequest request = new ListTeamsRequest();
        return getOpsGenieClient().team().listTeams(request).getTeams();
    }
}
