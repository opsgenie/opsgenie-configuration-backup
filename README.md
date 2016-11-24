# OpsGenie Configuration Backup

OpsGenie customers can backup their account configurations and restore it later using this script.

# Export
During export, this features are exported to local directory or remote Git branch:

```
*User information (username-fullname-skypeUsername-timezone-userRole-userContacts-notificationRules)
*Group information (name -users-description)
*Team information (name-members-memberRoles-description)
*Escalation information (name-rules-team-description-repeatInterval)
*Schedule information (name-timeZone-rotations-team -enabled-description-overrides)
*Heartbeat information (name-description-interval-intervalUnit-enabled)
        
```

The script exports data to a folder named OpsGenieBackups.
There are 8 sub-folders inside this main folder.

```
*users
*groups
*teams
*escalations
*schedules
*scheduleOverrides
*heartbeats
*notifications
```

The script exports the data to those sub-folders according to their types.

While exporting those entities, the script creates a unique file for each entity which contains JSON data for the entity.
For example if the account has 80 users, the script creates 80 different files with name “(username of the user)-(id of user)” and extension “.json” in the sub-folder named users.
Such as mehmetdemircs@gmail.com-54fc708c-2324-48c7-bb7f-ee08063f729a.json file in the path “OpsGenieBackups/users/”.


## Separate Files
The script uses separate files for each entity.
The reason is when the script tries to update the file, it only updates the modified entity’s file.
For example if an account has 80 users in the system and the script already took backup for all the system to the remote git.
After the backup is complete, if the owner updates 2 users from the OpsGenie system and tries to backup to the same git repository again, the script updates only 2 of the user files.
This helps users to keep track of the entities.

## Backup Over Another Backup
The script deletes old backup files in order to backup current system configurations.
This provides the ability to delete old entity files and automatically update current entities.

## File Format
The script uses OpsGenie Java SDK to export and import.
When it exports, it stores the entities as JSON and save those as separate files.
When it imports, it reads the JSON files and imports them to the given API key’s OpsGenie account.

## Git Limitations
Currently Git does not allow to push empty directors. For example, if the system does not have any schedules there will be no schedule file under schedule directory.
When the script tries to push this schedule directory to the remote Git, it won’t be pushed. Another word, empty directories won’t be pushed to Git.


# Import
In the import part, the script reads the data from remote Git repository or local path.

The file formats should be the same as export file formats.
Since entities data is stored in JSON format, users can change it manually.
However they should not change the file format. There should be one main folder named OpsGenieBackups and 7 sub-folders.

## Update current data
The script adds missing entities or updates current entities.
This can be set by user by using import configs.
For example if owner deletes a user after the backup, the script detects this deleted user and adds it to the current system, if addNewUsers parameter is set to true.
If the addNewUsers parameter is set to false, the script won't add the deleted users.
If the updateExistUsers parameter is set to false, the script won't update the modified users.

##Relation Between Entities
If the owner does not import missing users (which he/she can do it with ImportConfig) and tries to add a team or schedule which includes those users, the script will give an error message.
Therefore, the owner should be careful about changing ImportConfig.
Another word, if the owner only imports some entities, he/she should consider the relations with the other entities.
If the script encounters such an error, it will generate a logger error and simply skip this entity and continue to import other entities.



# Installing

##Executable Jars
We build 2 different executable jars for ease of use:
OpsGenieBackupExecutable.jar backs up your OpsGenie configuration data.
OpsGenieRestoreExecutable.jar restores your OpsGenie configuration from backup.


You can download the executable jars from [releases]( https://github.com/opsgenie/opsgenie-configuration-backup/releases).

Possible run configurations for OpsGenieBackupExecutable:
```
Run only with apiKey parameter.
This option simply exports the OpsGenie configuration to run path (the directory which the jar file is located in) in a folder named OpsGenieBackups.

java -jar OpsGenieBackupExecutable apiKey


Run with apiKey and extractPath parameters.
This option extracts the OpsGenie configuration to the given path.

java -jar OpsGenieBackupExecutable apiKey extractPath

Run with apiKey, git SSH URI and  SSH Key Path parameters.
This command clones the remote repository into a directory called OpsGenieBackupGitRepository.
After the cloning process the export jar backs up the data into this directory under a folder called OpsGenieBackups.

java -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath


Run with apiKey, git SSH URI,  SSH Key Path and GitClonePath parameters.
This option clones the remote git to the given path.

java -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath GitClonePath

Run with apiKey, git SSH URI,  SSH Key Path, sshPassphrase and GitClonePath parameters.
If the SSH key has a passphrase you need to run this configuration.

java -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath sshPassphrase GitClonePath

```

Argument | Definition
------------ | -------------
apiKey         |OpsGenie API key
extractPath    |Local path for extracting OpsGenieBackup folder
gitSSHURI      |SSH URI of the repository which OpsGenie configuration files will be pushed
SSHKeyPath     |Path of the SSH file which is storing the SSH key
sshPassphrase  |Passphrase of the given SSH key file
GitClonePath   |Local path for cloning given git repository to afolder called OpsGenieBackupGitRepository



Possible run configurations for OpsGenieRestoreExecutable:
```
Run only with apiKey parameter.
This option searches for OpsGenieBackups folder in run path (the directory which the jar file is located in)
If it finds the folder it restores the configurations to the OpsGenie account whose API key is given.

java -jar OpsGenieRestoreExecutable apiKey


Run with apiKey and extractPath parameters.
This option searches for OpsGenieBackup folder in given path
If it finds the folder named OpsGenieBackups it restores the configurations to the OpsGenie account whose API key is given.

java -jar OpsGenieRestoreExecutable apiKey OpsGenieBackupsHomePath


Run with apiKey, git SSH URI and  SSH Key Path parameters.
This option clones the remote git to the run path.
After cloning it searches the folder named OpsGenieBackups
If it finds the folder, the restore operation  is performed


java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath


Run with apiKey, git SSH URI,  SSH Key Path and  GitClonePath parameters.
This configuration clones the remote git to a folder named OpsGenieBackupGitRepository under the given path.


java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath GitClonePath


Run with apiKey, git SSH URI,  SSH Key Path, sshPassphrase and  GitClonePath parameters.
If the SSH key has a passphrase you need to run this configuration.

java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath sshPassphrase GitClonePath

```

The default restore operation imports everything in your backup folder to your OpsGenie account.
In order to configure your restore settings you can use restoreConfig.properties file.
OpsGenieRestoreExecutable.jar uses restoreConfig.properties file if there is any in run directory (the directory which the jar file is located in).

##Add as dependency
As an alternative to using the executible jars, you can use Maven or Gradle dependencies.

## Maven
You can add OpsGenie Configuration Backup as a Maven dependency. Example:

```
<dependencies>
  	<dependency>
  		<groupId>com.opsgenie.tools</groupId>
  		<artifactId>configuration-backup</artifactId>
  		<version>[0.3.0,)</version>
  	</dependency>
  </dependencies>
```

## Gradle

You can add OpsGenie Configuration Backup as a Gradle dependency. Example:

```
dependencies {
	compile "com.opsgenie.tools:configuration-backup:+"
}
```
## Build

**Requires JDK 1.6+**


This is a Gradle project so you can build by running `build` task:


Unix:
``./gradlew build``


Windows:
``gradlew.bat build``



#Example of Export

##Export to Local Path

```
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");
Exporter exporter = new Exporter(properties);
exporter.export();
        
```

##Export to Remote Git

```
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");
properties.setGitEnabled(true);
properties.setGitSshUri("git@github.com:opsgenie/OpsgenieRepository.git");
properties.setSshKeyPath("/home/user/.ssh/id_rsa");
Exporter exporter = new Exporter(properties);
exporter.export();
        
```

#Example of Import

##Import from Local Path

```
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");

Importer importer = new Importer(properties);
importer.restore();
        
```

##Import from Remote Git

```
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");
properties.setGitEnabled(true);
properties.setGitSshUri("git@github.com:opsgenie/OpsgenieRepository.git");
properties.setSshKeyPath("/home/user/.ssh/id_rsa");

Importer importer = new Importer(properties);
importer.restore();
        
```

##Import with Config

```
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");

ImportConfig config = new ImportConfig();
config.setAllFalse();
config.setAddNewUsers(true).setAddNewTeams(true).setUpdateExistSchedules(true);

Importer importer = new Importer(properties,config);
importer.restore();
        
```






## Authors

* **Mehmet Mustafa Demir <mehmetdemircs@gmail.com>** - *Initial work* 

