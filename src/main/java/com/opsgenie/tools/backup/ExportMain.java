package com.opsgenie.tools.backup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoRequest;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author Mehmet Mustafa Demir
 */
public class ExportMain {
    private final static Logger logger = LogManager.getLogger(ExportMain.class);

    public static void main(String[] args) throws IOException, GitAPIException, ParseException, OpsGenieClientException {
        logger.info("OpsGenieBackupExecutable is started!");

        String path = System.getProperty("user.dir");
        logger.info("Running path is = " + path);
        logger.info("Argument number is = " + args.length);

        for (int i = 0; i < args.length; i++) {
            logger.info(i + ". argument is = " + args[i]);
        }
        logger.info("Possible run configurations are:\n" +
                "java -jar OpsGenieBackupExecutable apiKey\n" +
                "java -jar OpsGenieBackupExecutable apiKey extractPath\n" +
                "java -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath\n" +
                "java -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath extractPath");

        String extractPath = null;
        String gitSSHURI = null;
        String sshKeyPath = null;
        String apiKey = null;

        if (args.length == 1) {
            logger.info("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey");
            apiKey = args[0];
        } else if (args.length == 2) {
            logger.info("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey extractPath");
            apiKey = args[0];
            extractPath = args[1];
        } else if (args.length == 3) {
            logger.info("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
        } else if (args.length == 4) {
            logger.info("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath extractPath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
            extractPath = args[3];
        } else {
            logger.error("Your paramater number " + args.length + " is invalid.");
            logger.error("Please execute with a valid configuration.");
            throw new RuntimeException("Invalid parameter number!");
        }


        GetAccountInfoRequest getAccountInfoRequest = new GetAccountInfoRequest();
        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(apiKey);

        GetAccountInfoResponse response = opsGenieClient.account().getAccount(getAccountInfoRequest);
        logger.info("Account name is " + response.getAccount().getName());


        BackupProperties properties = new BackupProperties();

        logger.info("The api Key is = " + apiKey);
        properties.setApiKey(apiKey);

        if (extractPath != null) {
            path = extractPath;
        }

        logger.info("The extract path is = " + path);
        properties.setPath(path);

        if (gitSSHURI != null && sshKeyPath != null) {
            properties.setGitEnabled(true);
            properties.setGitSshUri(gitSSHURI);
            properties.setSshKeyPath(sshKeyPath);
            logger.info("Export the git is enabled.");
            logger.info("The git SSH URI = " + gitSSHURI);
            logger.info("The SSH key path = " + sshKeyPath);
        }

        Exporter exporter = new Exporter(properties);
        exporter.export();

        logger.info("End");

    }
}
