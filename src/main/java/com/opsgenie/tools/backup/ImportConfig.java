package com.opsgenie.tools.backup;

public class ImportConfig {

    private boolean addNewUsers = true, updateExistingUsers = true;

    private boolean addNewTeams = true, updateExistingTeams = true;

    private boolean addNewSchedules = true, updateExistingSchedules = true;

    private boolean addNewEscalations = true, updateExistingEscalations = true;

    private boolean addNewUserForwarding = true, updateExistingUserForwarding = true;

    private boolean addNewScheduleOverrides = true, updateExistingScheduleOverrides = true;

    private boolean addNewPolicies = true, updateExistingPolicies = true;

    private boolean addNewIntegrations = true, updateExistingIntegrations = true;

    public boolean isAddNewUsers() {
        return addNewUsers;
    }

    public ImportConfig setAddNewUsers(boolean addNewUsers) {
        this.addNewUsers = addNewUsers;
        return this;
    }

    public boolean isUpdateExistingUsers() {
        return updateExistingUsers;
    }

    public ImportConfig setUpdateExistingUsers(boolean updateExistingUsers) {
        this.updateExistingUsers = updateExistingUsers;
        return this;
    }

    public boolean isAddNewTeams() {
        return addNewTeams;
    }

    public ImportConfig setAddNewTeams(boolean addNewTeams) {
        this.addNewTeams = addNewTeams;
        return this;
    }

    public boolean isUpdateExistingTeams() {
        return updateExistingTeams;
    }

    public ImportConfig setUpdateExistingTeams(boolean updateExistingTeams) {
        this.updateExistingTeams = updateExistingTeams;
        return this;
    }

    public boolean isAddNewSchedules() {
        return addNewSchedules;
    }

    public ImportConfig setAddNewSchedules(boolean addNewSchedules) {
        this.addNewSchedules = addNewSchedules;
        return this;
    }

    public boolean isUpdateExistingSchedules() {
        return updateExistingSchedules;
    }

    public ImportConfig setUpdateExistingSchedules(boolean updateExistingSchedules) {
        this.updateExistingSchedules = updateExistingSchedules;
        return this;
    }

    public boolean isAddNewEscalations() {
        return addNewEscalations;
    }

    public ImportConfig setAddNewEscalations(boolean addNewEscalations) {
        this.addNewEscalations = addNewEscalations;
        return this;
    }

    public boolean isUpdateExistingEscalations() {
        return updateExistingEscalations;
    }

    public ImportConfig setUpdateExistingEscalations(boolean updateExistingEscalations) {
        this.updateExistingEscalations = updateExistingEscalations;
        return this;
    }

    public boolean isAddNewUserForwarding() {
        return addNewUserForwarding;
    }

    public ImportConfig setAddNewUserForwarding(boolean addNewUserForwarding) {
        this.addNewUserForwarding = addNewUserForwarding;
        return this;
    }

    public boolean isUpdateExistingUserForwarding() {
        return updateExistingUserForwarding;
    }

    public ImportConfig setUpdateExistingUserForwarding(boolean updateExistingUserForwarding) {
        this.updateExistingUserForwarding = updateExistingUserForwarding;
        return this;
    }

    public boolean isAddNewScheduleOverrides() {
        return addNewScheduleOverrides;
    }

    public ImportConfig setAddNewScheduleOverrides(boolean addNewScheduleOverrides) {
        this.addNewScheduleOverrides = addNewScheduleOverrides;
        return this;
    }

    public boolean isUpdateExistingScheduleOverrides() {
        return updateExistingScheduleOverrides;
    }

    public ImportConfig setUpdateExistingScheduleOverrides(boolean updateExistingScheduleOverrides) {
        this.updateExistingScheduleOverrides = updateExistingScheduleOverrides;
        return this;
    }

    public boolean isAddNewIntegrations() {
        return addNewIntegrations;
    }

    public ImportConfig setAddNewIntegrations(boolean addNewIntegrations) {
        this.addNewIntegrations = addNewIntegrations;
        return this;
    }

    public boolean isUpdateExistingIntegrations() {
        return updateExistingIntegrations;
    }

    public ImportConfig setUpdateExistingIntegrations(boolean updateExistingIntegrations) {
        this.updateExistingIntegrations = updateExistingIntegrations;
        return this;
    }

    public boolean isAddNewPolicies() {
        return addNewPolicies;
    }

    public void setAddNewPolicies(boolean addNewPolicies) {
        this.addNewPolicies = addNewPolicies;
    }

    public boolean isUpdateExistingPolicies() {
        return updateExistingPolicies;
    }

    public void setUpdateExistingPolicies(boolean updateExistingPolicies) {
        this.updateExistingPolicies = updateExistingPolicies;
    }
}
