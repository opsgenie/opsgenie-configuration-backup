package com.opsgenie.tools.backup;

import com.opsgenie.tools.backup.util.BackupUtils;

/**
 * Necessary parameters for export and import methods.
 *
 * @author Mehmet Mustafa Demir
 */
public class BackupProperties {
    private String apiKey;
    private String opsgenieUrl;
    private String gitSshUri;
    private String path;
    private String sshKeyPath;
    private String passphrase;
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

    /**
     * api key of the opsgenie account
     *
     * @return String apiKey
     */

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Set the apiKey of the opsgenie account
     *
     * @param apiKey of the opsgenie account
     */
    public BackupProperties setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * SSH URI of the remote git
     *
     * @return String git SSH URI
     */
    public String getGitSshUri() {
        return gitSshUri;
    }

    /**
     * Sets the remote git SSH URI
     *
     * @param gitSshUri SSH URI
     */
    public BackupProperties setGitSshUri(String gitSshUri) {
        this.gitSshUri = gitSshUri;
        return this;
    }

    /**
     * Local part for export and import procedure.
     *
     * @return localPath
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the local path
     *
     * @param path localPath
     */
    public BackupProperties setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * SSH KEY file localation
     *
     * @return sshKeyPath
     */

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    /**
     * set SSH Key file location
     *
     * @param sshKeyPath location
     */
    public BackupProperties setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
        return this;
    }

    /**
     * git enabled settings
     *
     * @return gitEnabled boolean value
     */

    public boolean isGitEnabled() {
        return gitEnabled;
    }

    /**
     * Sets the git enabled setting
     *
     * @param gitEnabled boolean value
     */
    public BackupProperties setGitEnabled(boolean gitEnabled) {
        this.gitEnabled = gitEnabled;
        return this;
    }

    /**
     * ssh file passphrase
     *
     * @return String passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Sets ssh file passphrase
     *
     * @param passphrase String value
     */
    public BackupProperties setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public String getOpsgenieUrl() {
        return opsgenieUrl;
    }

    public BackupProperties setOpsgenieUrl(String opsgenieUrl) {
        this.opsgenieUrl = opsgenieUrl;
        return this;
    }
}
