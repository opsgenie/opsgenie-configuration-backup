# OpsGenie Configuration Backup

OpsGenie customers can back up their account configuration and restore it later using this script.

You can download the executable jars from [releases]( https://github.com/opsgenie/opsgenie-configuration-backup/releases).

## Parameters
  Options:
  * --apiKey
       Opsgenie Integration API Key. (API Key is mandatory)  
       + Access Rights: [Read and Configuration Access](https://docs.opsgenie.com/docs/api-key-management#section-access-rights-)
  *  --backupPath
       Backup directory
       + Default: /Users/baris/git/client-configuration-backup
  *  --opsgenieHost
       OpsGenie host to use
       + Default: https://api.opsgenie.com
  *  --sshKeyPath
       Ssh key path
  *  --sshPassPhrase
       Ssh pass phrase
  *  --sshUrl
       Git ssh url

Possible run configurations for  OpsGenieExportUtil:
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

Possible run configurations for OpsGenieImportUtil:
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

# Export
During export, these features are exported to a local directory or a git repository:

```
* Custom User Roles
* Users and Notification Rules
* Forwarding Rules
* Teams
* Escalations
* Schedules and Schedule Overrides
* Alert Policies (old version)
* Integrations and Integration Actions
* Policies
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
*policiesV2
```

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
Since entities data is stored in JSON format, users can change it manually but they should not change the file format.

The default restore operation imports everything in your backup folder to your OpsGenie account.
In order to configure your restore settings you can use restoreConfig.properties file.
OpsGenieImportUtil uses restoreConfig.properties file if there is any in run directory (the directory which the jar file is located in).

## Update current data
The script adds missing entities or updates current entities.
This can be set by user by using the import configs.
For example if OpsGenie account owner or admin deletes a user after the backup, the script detects this deleted user and adds it to the current system, if addNewUsers parameter is set to true.
If the addNewUsers parameter is set to false, the script won't add the deleted users.
If the updateExistUsers parameter is set to false, the script won't update the modified users.

## Relation Between Entities
If the owner does not import missing users (which he/she can do it with ImportConfig) and tries to add a team or schedule which includes those users, the script will give an error message.
Therefore, the owner should be careful about changing ImportConfig.
Another word, if the owner only imports some entities, he/she should consider the relations with the other entities.
If the script encounters such an error, it will generate a logger error and simply skip this entity and continue to import other entities.

## Entity Orders
Orders are preserved while importing Alert Policies. 
If new alert policies were added after the backup, then the imported ones are added to the end with their orders preserved.

# Current Limitations
## Transient Data
Exporting dynamic data like alerts, incidents, alert and customer logs, notifications etc. is not possible and will not be implemented.

## Account Configuration
Exporting account configuration like sso settings, password policy, central notification template is not supported since there are no public api endpoints for them 

## Heartbeats
Exporting heartbeats is not possible since listing heartbeats is not possible at api level and will not be implemented.

## Services
Exporting services is not supported right now but it will be implemented eventually

## Integrations
Exporting some integration types is not possible since they are not supported at api level.

Not supported: Incoming Call, PingdomWebHook, Nagios, Observium, Hipchat
Update Only: Slack, HipChat, FlowDockV2