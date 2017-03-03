package com.opsgenie.tools.backup;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

    @Parameter(names = {"--apiKey"}, required = true, description = "Opsgenie Integration API Key")
    private String apiKey;

    @Parameter(names = {"--backupPath"}, description = "Backup directory")
    private String backupPath = System.getProperty("user.dir");

    @Parameter(names = {"--opsgenieHost"}, description = "OpsGenie host to use")
    private String opsGenieHost = "https://api.opsgenie.com";

    @Parameter(names = {"--sshKeyPath"}, description = "Ssh key path")
    private String sshKeyPath;

    @Parameter(names = {"--sshPassPhrase"}, password = true, description = "Ssh pass phrase")
    private String sshPassphrase;

    @Parameter(names = {"--sshUrl"}, description = "Git ssh url")
    private String gitSshUrl;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getOpsGenieHost() {
        return opsGenieHost;
    }

    public void setOpsGenieHost(String opsGenieHost) {
        this.opsGenieHost = opsGenieHost;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public void setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
    }

    public String getSshPassphrase() {
        return sshPassphrase;
    }

    public void setSshPassphrase(String sshPassphrase) {
        this.sshPassphrase = sshPassphrase;
    }

    public String getGitSshUrl() {
        return gitSshUrl;
    }

    public void setGitSshUrl(String gitSshUrl) {
        this.gitSshUrl = gitSshUrl;
    }
}
