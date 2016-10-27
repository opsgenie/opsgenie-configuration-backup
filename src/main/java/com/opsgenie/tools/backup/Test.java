package com.opsgenie.tools.backup;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * @author Mehmet Mustafa Demir <mehmetdemircs@gmail.com>
 */
public class Test {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        System.out.println("Hello");

        BackupProperties properties = new BackupProperties();
        properties.setApiKey("1b2f5900-fe9b-4055-b30c-be58fe573c76");
        properties.setPath("/home/mmd/Desktop/BackupFolder");
        properties.setGitEnabled(true);
        properties.setGitSshUri("git@github.com:mehmetdemircs/OpsgenieRepository.git");
        properties.setSshKeyPath("/home/mmd/.ssh/id_rsa");

        Exporter exporter = new Exporter(properties);
        //exporter.export();


        ImportConfig config = new ImportConfig();
        config.setAddNewUsers(false);
        config.setAddNewTeams(false);
        config.setUpdateExistSchedules(false);

        Importer importer = new Importer(properties, config);
        importer.restore();

        System.out.println("End");

    }
}
