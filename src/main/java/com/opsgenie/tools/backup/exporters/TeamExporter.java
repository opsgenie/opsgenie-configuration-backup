package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.TeamApi;
import com.opsgenie.client.model.Team;

import java.util.Collections;
import java.util.List;

public class TeamExporter extends BaseExporter<Team> {

    private static TeamApi teamApi = new TeamApi();

    public TeamExporter(String backupRootDirectory) {
        super(backupRootDirectory, "teams");
    }

    @Override
    protected String getBeanFileName(Team bean) {
        return bean.getName() + "-" + bean.getId();
    }


    @Override
    protected List<Team> retrieveEntities() throws ApiException {
        return teamApi.listTeams(Collections.<String>emptyList()).getData();
    }
}
