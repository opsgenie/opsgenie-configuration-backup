package com.opsgenie.tools.backup;

/**
 * Config parameters for import procedure.
 *
 * @author Mehmet Mustafa Demir
 */
public class ImportConfig {
    private boolean addNewHeartbeats = true, updateExistingHeartbeats = true;

    private boolean addNewUsers = true, updateExistingUsers = true;

    private boolean addNewGroups = true, updateExistingGroups = true;

    private boolean addNewTeams = true, updateExistingTeams = true;

    private boolean addNewSchedules = true, updateExistingSchedules = true;

    private boolean addNewEscalations = true, updateExistingEscalations = true;

    private boolean addNewNotifications = true, updateExistingNotifications = true;

    private boolean addNewTeamRoutingRules = true, updateExistingTeamRoutingRules = true;

    private boolean addNewUserForwarding = true, updateExistingUserForwarding = true;

    private boolean addNewScheduleOverrides = true, updateExistingScheduleOverrides = true;

    private boolean addNewIntegrations = true, updateExistingIntegrations = true;

    public void setAllFalse() {
        addNewHeartbeats = false;
        updateExistingHeartbeats = false;
        addNewUsers = false;
        updateExistingUsers = false;
        addNewGroups = false;
        updateExistingGroups = false;
        addNewTeams = false;
        updateExistingTeams = false;
        addNewSchedules = false;
        updateExistingSchedules = false;
        addNewEscalations = false;
        updateExistingEscalations = false;
        addNewNotifications = false;
        updateExistingNotifications = false;
        addNewTeamRoutingRules = false;
        updateExistingTeamRoutingRules = false;
        addNewUserForwarding = false;
        updateExistingUserForwarding = false;
        addNewScheduleOverrides = false;
        updateExistingScheduleOverrides = false;
        addNewIntegrations = false;
        updateExistingIntegrations = false;
    }

    public boolean isAddNewHeartbeats() {
        return addNewHeartbeats;
    }

    public ImportConfig setAddNewHeartbeats(boolean addNewHeartbeats) {
        this.addNewHeartbeats = addNewHeartbeats;
        return this;
    }

    public boolean isUpdateExistingHeartbeats() {
        return updateExistingHeartbeats;
    }

    public ImportConfig setUpdateExistingHeartbeats(boolean updateExistingHeartbeats) {
        this.updateExistingHeartbeats = updateExistingHeartbeats;
        return this;
    }

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

    public boolean isAddNewGroups() {
        return addNewGroups;
    }

    public ImportConfig setAddNewGroups(boolean addNewGroups) {
        this.addNewGroups = addNewGroups;
        return this;
    }

    public boolean isUpdateExistingGroups() {
        return updateExistingGroups;
    }

    public ImportConfig setUpdateExistingGroups(boolean updateExistingGroups) {
        this.updateExistingGroups = updateExistingGroups;
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

    public boolean isAddNewNotifications() {
        return addNewNotifications;
    }

    public ImportConfig setAddNewNotifications(boolean addNewNotifications) {
        this.addNewNotifications = addNewNotifications;
        return this;
    }

    public boolean isUpdateExistingNotifications() {
        return updateExistingNotifications;
    }

    public ImportConfig setUpdateExistingNotifications(boolean updateExistingNotifications) {
        this.updateExistingNotifications = updateExistingNotifications;
        return this;
    }

    public boolean isAddNewTeamRoutingRules() {
        return addNewTeamRoutingRules;
    }

    public ImportConfig setAddNewTeamRoutingRules(boolean addNewTeamRoutingRules) {
        this.addNewTeamRoutingRules = addNewTeamRoutingRules;
        return this;
    }

    public boolean isUpdateExistingTeamRoutingRules() {
        return updateExistingTeamRoutingRules;
    }

    public ImportConfig setUpdateExistingTeamRoutingRules(boolean updateExistingTeamRoutingRules) {
        this.updateExistingTeamRoutingRules = updateExistingTeamRoutingRules;
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
}
