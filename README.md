# OpsGenie Configuration Backup

OpsGenie customers can backup their account configurations and restore it later using this script.

# Export
During export, this features are exported to local directory or remote Git branch:

```
User information (username-fullname-skypeUsername-timezone-userRole-userContacts-notificationRules)
Group information (name -users-description)
Team information (name-members-memberRoles-description)
Escalation information (name-rules-team-description-repeatInterval)
Schedule information (name-timeZone-rotations-team -enabled-description)
Heartbeat information (name-description-interval-intervalUnit-enabled)
        
```

The script exports data to a folder named OpsGenieBackups.
There are 7 sub-folders inside this main folder.

```
users
groups
teams
escalations
schedules
heartbeats
notifications
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
config.setAddNewUsers(false);
config.setAddNewTeams(false);
config.setUpdateExistSchedules(false);

Importer importer = new Importer(properties,config);
importer.restore();
        
```

# Maven
You can add OpsGenie Configuration Backup as dependency. Example:

```
<dependencies>
  	<dependency>
  		<groupId>com.opsgenie.tools</groupId>
  		<artifactId>configuration-backup</artifactId>
  		<version>[0.1.0,)</version>
  	</dependency>
  </dependencies>
```

# Gradle

You can add OpsGenie Configuration Backup as dependency. Example:

```
dependencies {
	compile "com.opsgenie.tools:configuration-backup:+"
}
```


## Build

**Requires JDK 1.6**

This is a gradle project so you can build by running `build` task

Unix:
``./gradlew build``

Windows:
``gradlew.bat build``

## Authors

* **Mehmet Mustafa Demir <mehmetdemircs@gmail.com>** - *Initial work* 

