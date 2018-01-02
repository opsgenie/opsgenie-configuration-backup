# OpsGenie Configuration Backup

OpsGenie customers can back up their account configuration and restore it later using this script.

# Export
During export, these features are exported to a local directory or remote Git branch:

```
* Custom User Roles
* Users and Notification Rules
* Forwarding Rules
* Teams
* Escalations
* Schedules and Schedule Overrides
* Alert Policies
* Integrations and Integration Actions
```

The script exports data to a folder named OpsGenieBackups.
There are 9 sub-folders inside this main folder.

```
* customUserRoles
* users
* forwardings
* teams
* escalations
* schedules
* policies
* orders
* integrations
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
The script uses OpsGenie Java SDK and Integrations API to export and import.
When it exports, it stores the entities as JSON and saves those as separate files.
When it imports, it reads the JSON files and imports them to the given API key’s OpsGenie account.

## Git Limitations
Currently Git does not allow to push empty directories. For example, if the system does not have any schedules there will be no schedule file under schedule directory.
When the script tries to push the schedule directory to the remote Git, it won’t be pushed. Another word, empty directories won’t be pushed to Git.

# Import
While importing, the script reads the data from remote Git repository or local path.

The file formats should be the same as export file formats.
Since entities data is stored in JSON format, users can change it manually.
However they should not change the file format. There should be one main folder named OpsGenieBackups and 9 sub-folders.

## Update current data
The script adds missing entities or updates current entities.
This can be set by user by using the import configs.
For example if OpsGenie account owner or admin deletes a user after the backup, the script detects this deleted user and adds it to the current system, if addNewUsers parameter is set to true.
If the addNewUsers parameter is set to false, the script won't add the deleted users.
If the updateExistUsers parameter is set to false, the script won't update the modified users.

##Relation Between Entities
If the owner does not import missing users (which he/she can do it with ImportConfig) and tries to add a team or schedule which includes those users, the script will give an error message.
Therefore, the owner should be careful about changing ImportConfig.
Another word, if the owner only imports some entities, he/she should consider the relations with the other entities.
If the script encounters such an error, it will generate a logger error and simply skip this entity and continue to import other entities.

##Entity Orders
Orders are preserved while importing Alert Policies. If new alert policies were added after the backup, then the imported ones are added to the end with their orders preserved.

##Current Limitations
Exporting some integration types is not possible since they are not supported at api level

Not supported: PingdomWebHook, Nagios, Observium, NagiosXI, Hipchat, Incoming Call   
Update Only: Slack App Integration, Slack, HipChat, FlowDockV2

# Installing

##Executable Jars
We build 2 different executable jars for ease of use:
`OpsGenieExportUtil-*.jar` backs up your OpsGenie configuration data.
OpsGenieRestoreExecutable.jar restores your OpsGenie configuration from backup.


You can download the executable jars from [releases]( https://github.com/opsgenie/opsgenie-configuration-backup/releases).

Possible run configurations for OpsGenieBackupExecutable:
```
1. Run only with apiKey parameter.
This option simply exports the OpsGenie configuration to run path (the directory which the jar file is located in) in a folder named OpsGenieBackups.

java -jar OpsGenieExportUtil-*.jar --apiKey YOUR_API_KEY


2. Run with apiKey and backupPath parameters.
This option extracts the OpsGenie configuration to the given path.

java -jar OpsGenieExportUtil-*.jar --apiKey YOUR_API_KEY --backupPath EXTRACT_PATH


3. Run with apiKey, git sshUrl and  sshKeyPath parameters.
This command clones the remote repository into a directory called OpsGenieBackupGitRepository.
After the cloning process the export jar backs up the data into this directory under a folder called OpsGenieBackups.

java -jar OpsGenieExportUtil-*.jar --apiKey YOUR_API_KEY --sshUrl GIT_SSH_URL -sshKeyPath GIT_SSH_PATH 


4. Run with apiKey, git sshUrl,  sshKeyPath and backupPAth parameters.
This option clones the remote git to the given path.

java -jar OpsGenieExportUtil-*.jar --apiKey YOUR_API_KEY --sshUrl GIT_SSH_URL -sshKeyPath GIT_SSH_PATH --backupPath EXTRACT_PATH

```

Usage: OpsGenieConfigExporter [options]
  Options:
  * --apiKey
       Opsgenie Integration API Key. (API Key is mandatory)
  *  --backupPath
       Backup directory
       Default: /Users/baris/git/client-configuration-backup
  *  --opsgenieHost
       OpsGenie host to use
       Default: https://api.opsgenie.com
  *  --sshKeyPath
       Ssh key path
  *  --sshPassPhrase
       Ssh pass phrase
  *  --sshUrl
       Git ssh url

Possible run configurations for OpsGenieRestoreExecutable:
```
1. Run only with apiKey parameter.
This option searches for OpsGenieBackups folder in run path (the directory which the jar file is located in)
If it finds the folder it restores the configurations to the OpsGenie account whose API key is given.

java -jar OpsGenieImportUtil-*.jar --apiKey YOUR_API_KEY


2. Run with apiKey and backupPath parameters.
This option searches for OpsGenieBackup folder in given path
If it finds the folder named OpsGenieBackups it restores the configurations to the OpsGenie account whose API key is given.

java -jar OpsGenieImportUtil-*.jar --apiKey YOUR_API_KEY --backupPath EXTRACT_PATH


3. Run with apiKey, sshUrl and  sshKeyPath parameters.
This option clones the remote git to the run path.
After cloning it searches the folder named OpsGenieBackups
If it finds the folder, the restore operation  is performed

java -jar OpsGenieImportUtil-*.jar --apiKey YOUR_API_KEY --sshUrl GIT_SSH_URL -sshKeyPath GIT_SSH_PATH 


4. Run with apiKey, sshUrl,  sshKeyPath and  backupPath parameters.
This configuration clones the remote git to a folder named OpsGenieBackupGitRepository under the given path.

java -jar OpsGenieImportUtil-*.jar --apiKey YOUR_API_KEY --sshUrl GIT_SSH_URL -sshKeyPath GIT_SSH_PATH --backupPath EXTRACT_PATH

```

The default restore operation imports everything in your backup folder to your OpsGenie account.
In order to configure your restore settings you can use restoreConfig.properties file.
OpsGenieRestoreExecutable.jar uses restoreConfig.properties file if there is any in run directory (the directory which the jar file is located in).

##Add as dependency
As an alternative to using the executible jars, you can use Maven or Gradle dependencies.

## Maven
You can add OpsGenie Configuration Backup as a Maven dependency. Example:

```xml
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

```groovy
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

```java
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");
Exporter exporter = new Exporter(properties);
exporter.export();
        
```

##Export to Remote Git

```java
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

```java
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");

Importer importer = new Importer(properties);
importer.restore();
        
```

##Import from Remote Git

```java
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

```java
BackupProperties properties = new BackupProperties();
properties.setApiKey("1b2f5900-dasd-4055-214s-dsad21asd");
properties.setPath("/home/user/Desktop/BackupFolder");

ImportConfig config = new ImportConfig();
config.setAllFalse();
config.setAddNewUsers(true).setAddNewTeams(true).setUpdateExistSchedules(true);

Importer importer = new Importer(properties,config);
importer.restore();
        
```
