package com.opsgenie.tools.backup;

/**
 * Config parameters for import procedure.
 *
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class ImportConfig {
    private boolean addNewHeartbeats = true, updateExistHeartbeats = true;

    private boolean addNewUsers = true, updateExistUsers = true;

    private boolean addNewGroups = true, updateExistGroups = true;

    private boolean addNewTeams = true, updateExistTeams = true;

    private boolean addNewSchedules = true, updateExistSchedules = true;

    private boolean addNewEscalations = true, updateExistEscalations = true;

    public boolean isAddNewHeartbeats() {
        return addNewHeartbeats;
    }

    public ImportConfig setAddNewHeartbeats(boolean addNewHeartbeats) {
        this.addNewHeartbeats = addNewHeartbeats;
        return this;
    }

    public boolean isUpdateExistHeartbeats() {
        return updateExistHeartbeats;
    }

    public ImportConfig setUpdateExistHeartbeats(boolean updateExistHeartbeats) {
        this.updateExistHeartbeats = updateExistHeartbeats;
        return this;
    }

    public boolean isAddNewUsers() {
        return addNewUsers;
    }

    public ImportConfig setAddNewUsers(boolean addNewUsers) {
        this.addNewUsers = addNewUsers;
        return this;
    }

    public boolean isUpdateExistUsers() {
        return updateExistUsers;
    }

    public ImportConfig setUpdateExistUsers(boolean updateExistUsers) {
        this.updateExistUsers = updateExistUsers;
        return this;
    }

    public boolean isAddNewGroups() {
        return addNewGroups;
    }

    public ImportConfig setAddNewGroups(boolean addNewGroups) {
        this.addNewGroups = addNewGroups;
        return this;
    }

    public boolean isUpdateExistGroups() {
        return updateExistGroups;
    }

    public ImportConfig setUpdateExistGroups(boolean updateExistGroups) {
        this.updateExistGroups = updateExistGroups;
        return this;
    }

    public boolean isAddNewTeams() {
        return addNewTeams;
    }

    public ImportConfig setAddNewTeams(boolean addNewTeams) {
        this.addNewTeams = addNewTeams;
        return this;
    }

    public boolean isUpdateExistTeams() {
        return updateExistTeams;
    }

    public ImportConfig setUpdateExistTeams(boolean updateExistTeams) {
        this.updateExistTeams = updateExistTeams;
        return this;
    }

    public boolean isAddNewSchedules() {
        return addNewSchedules;
    }

    public ImportConfig setAddNewSchedules(boolean addNewSchedules) {
        this.addNewSchedules = addNewSchedules;
        return this;
    }

    public boolean isUpdateExistSchedules() {
        return updateExistSchedules;
    }

    public ImportConfig setUpdateExistSchedules(boolean updateExistSchedules) {
        this.updateExistSchedules = updateExistSchedules;
        return this;
    }

    public boolean isAddNewEscalations() {
        return addNewEscalations;
    }

    public ImportConfig setAddNewEscalations(boolean addNewEscalations) {
        this.addNewEscalations = addNewEscalations;
        return this;
    }

    public boolean isUpdateExistEscalations() {
        return updateExistEscalations;
    }

    public ImportConfig setUpdateExistEscalations(boolean updateExistEscalations) {
        this.updateExistEscalations = updateExistEscalations;
        return this;
    }
}
