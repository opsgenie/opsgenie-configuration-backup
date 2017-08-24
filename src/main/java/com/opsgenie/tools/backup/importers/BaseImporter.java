package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.tools.backup.BackupUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

abstract class BaseImporter<T> implements Importer {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected File importDirectory;
    private boolean addEntityEnabled;
    private boolean updateEntityEnabled;

    BaseImporter(String backupRootDirectory, boolean addEntityEnabled, boolean updateEntityEnabled) {
        this.addEntityEnabled = addEntityEnabled;
        this.updateEntityEnabled = updateEntityEnabled;
        this.importDirectory = new File(backupRootDirectory + "/" + getImportDirectoryName() + "/");
    }

    public void restore() {
        if (!addEntityEnabled && !updateEntityEnabled) {
            logger.info("Skipping importing " + getImportDirectoryName() + " because both add and update is disabled");
            return;
        }

        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!importDirectory.exists()) {
            logger.warn("Warning: " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        String[] files = BackupUtils.getFileListOf(importDirectory);
        if (files == null || files.length == 0) {
            logger.warn("Warning: " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }
        try {
            populateCurrentEntityList();
        } catch (ApiException e) {
            logger.error("Could not get current configuration" + e.getMessage());
        }
        for (String fileName : files) {
            T entity = readEntity(fileName);
            if (entity != null) {
                importEntity(entity);
            }
        }

        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    protected T readEntity(String fileName) {
        try {
            String entityJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            T entity = getNewInstance();
            BackupUtils.fromJson(entity, entityJson);
            return entity;
        } catch (Exception e) {
            logger.error("Error at reading " + getImportDirectoryName() + " file " + fileName, e);
            return null;
        }
    }

    private void importEntity(T backupEntity) {
        EntityStatus entityStatus;
        try {
            entityStatus = checkEntity(backupEntity);
        } catch (ApiException e) {
            logger.error(e.getMessage());
            return;
        }

        if (updateEntityEnabled && (EntityStatus.EXISTS_WITH_ID.equals(entityStatus) || EntityStatus.EXISTS_WITH_NAME.equals(entityStatus))) {
            try {
                updateEntity(backupEntity, entityStatus);
                logger.info(getEntityIdentifierName(backupEntity) + " updated");
            } catch (Exception e) {
                logger.error("Error at updating " + getEntityIdentifierName(backupEntity) + "." + e.getMessage());
            }
        } else if (EntityStatus.NOT_EXIST.equals(entityStatus) && addEntityEnabled) {
            try {
                createEntity(backupEntity);
                logger.info(getEntityIdentifierName(backupEntity) + " added");
            } catch (Exception e) {
                logger.error("Error at adding " + getEntityIdentifierName(backupEntity) + ". " + e.getMessage());
            }
        }
    }

    protected abstract EntityStatus checkEntity(T entity) throws ApiException;

    protected abstract void populateCurrentEntityList() throws ApiException;

    protected abstract void createEntity(T entity) throws ParseException, IOException, ApiException;

    protected abstract void updateEntity(T entity, EntityStatus entityStatus) throws ParseException, IOException, ApiException;

    protected T getNewInstance() {
        throw new UnsupportedOperationException();
    }

    protected abstract String getEntityIdentifierName(T entity);

    protected abstract String getImportDirectoryName();

}
