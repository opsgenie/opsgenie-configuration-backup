package com.opsgenie.tools.backup;

/**
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class BackupProperties {
    private String apiKey;
    private String gitSshUri;
    private String path;
    private String sshKeyPath;
    private boolean gitEnabled = false;

    public void validate() throws IllegalArgumentException {
        if (BackupUtils.isEmptyString(apiKey)) {
            throw new IllegalArgumentException("invalid api Key");
        }

        if (BackupUtils.isEmptyString(path)) {
            throw new IllegalArgumentException("invalid path");
        }


        if (gitEnabled) {

            if (BackupUtils.isEmptyString(gitSshUri)) {
                throw new IllegalArgumentException("invalid gitSshUri");
            }

            if (BackupUtils.isEmptyString(sshKeyPath)) {
                throw new IllegalArgumentException("invalid sshKeyPath");
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public BackupProperties setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getGitSshUri() {
        return gitSshUri;
    }

    public BackupProperties setGitSshUri(String gitSshUri) {
        this.gitSshUri = gitSshUri;
        return this;
    }

    public String getPath() {
        return path;
    }

    public BackupProperties setPath(String path) {
        this.path = path;
        return this;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public BackupProperties setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
        return this;
    }

    public boolean isGitEnabled() {
        return gitEnabled;
    }

    public BackupProperties setGitEnabled(boolean gitEnabled) {
        this.gitEnabled = gitEnabled;
        return this;
    }
}
