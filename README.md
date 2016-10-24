# OpsGenie Configuration Backup

Opsgenie customers backup their account configurations and restore it later with using this script.

## Export
In the export part those features will be exported to local directory or remote git branch

User informations (username-fullname-skype username-timezone-userRole-user Contacts)
Group information (name - users- description)
Team information (name- members and their roles - description)
Escalation information (name - rules - team -description- repeat interval)
Schedule information (name - timeZone - rotations - team - enabled- description)
Heartbeat information (name-description- interval and interval unit- enabled)

The script will export data to a folder named OpsgenieBackups.
There will be a 6 sub folder inside this main folder.
Users
Groups
Teams
Escalations
Schedules
Heartbeats
The script will export the data to those sub folders according to their types.

While exporting those entities script will create a unique file for each entity which contains json data for the entity.
For example if the account have 80 users script will have 80 different file with name “(username of the user)-(id of user)” and extension “.json” in the sub folder named users.
Such as mehmetdemircs@gmail.com-54fc708c-2324-48c7-bb7f-ee08063f729a.jsonfile in the path “OpsgenieBackups/users/”.


### Separated Files
Script will use separate file for each entity.
The reason is when the script try to update the file it will only update the updated entity’s file.
For example an account have 80 users in the system and script already take backup for the all system to the git.
After the backup if owner update 2 users from the opsgenie system and try to backup again to the same git repository the script will update only 2 of the user files.
This will help user to keep track of the entities.

### Backup Over Another Backup
The script will delete old backup files in order to backup current system configurations.
This will provide to delete old entity files and automatically update current entities.

### File Format
The script is using opsgenie java sdk to exports and imports.
When it trys to export it will store models as json and save those as files.
In the import part script will read the files and import it to given api key’s opsgenie account.

### Deleted Entities
Deleted entities will be deleted from backup too.

### Git Limitations
Currently Git does not allow to push empty directors. For example If the system does not have any schedules there will be no schedule file under schedule folder.
When program try to push this schedule folder to the remote git it won’t be pushed. Another word empty folders won’t be pushed to git.


## Import
In the import part script will read the data from git repository or local file system.
This include :

User informations (username-fullname-skype username-timezone-userRole-user Contacts)
Group information (name - users- description)
Team information (name- members and their roles - description)
Escalation information (name - rules - team -description- repeat interval)
Schedule information (name - timeZone - rotations - team - enabled- description)
Heartbeat information (name-description- interval and interval unit- enabled)


The file formats should be same as export file formats.
Since entities data store in json format user can change it manually.
However he/she should not change the file format. There should be one main folder named opsgenieBackups and 6 subfolders.

### Update current data
Script will add missing entities or update current entities.
This can be set by user with using import configs.
For example if a user deleted after the backup the script will detect this deleted user and add it to the current system if addNewUsers parameter set true.
If the addNewUsers parameter set false the script won't add the deleted users.
If the updateExistUsers parameter set false the script won't update the changed users.

Relation Between Entities
If the user does not import a missing users (which he/she can do it with ImportConfig) and try to add a group or schedule which include this user will give error.
Therefore user should be careful about changing ImportConfig.
Another word if user only import some entities he/she should think about the relations to other entities.
If the script encounter such an error it will write a logger error and simply skip this entity and continue to import other entities.


The project includes:

* Java SDK
* Marid

## Java SDK For Maven and Gradle


### Maven

You can add OpsGenie Repository and SDK as dependency. Example:

```
<dependencies>
  	<dependency>
  		<groupId>com.opsgenie.integration</groupId>
  		<artifactId>sdk</artifactId>
  		<version>[2.0.0,)</version>
  	</dependency>
  </dependencies>
```

### Gradle

You can add OpsGenie Repository and SDK as dependeny. Example:

```
dependencies {
	compile "com.opsgenie.integration:sdk:2+"
}
```


## Build

**Requires JDK 1.6**

This is a gradle project so you can build by running `build` task

Unix:
``./gradlew build``

Windows:
``gradlew.bat build``
