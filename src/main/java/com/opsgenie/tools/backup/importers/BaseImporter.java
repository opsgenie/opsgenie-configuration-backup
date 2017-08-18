package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

    public void restore() throws RestoreException, ApiException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!importDirectory.exists()) {
            logger.warn("Warning: " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        if (!addEntityEnabled && !updateEntityEnabled) {
            logger.info("Skipping importing " + getImportDirectoryName() + " because both add and update is disabled");
            return;
        }

        String[] files = BackupUtils.getFileListOf(importDirectory);
        if (files == null || files.length == 0) {
            logger.warn("Warning: " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }

        List<T> backups = new ArrayList<T>();
        for (String fileName : files) {
            T bean = readEntity(fileName);
            if (bean != null) {
                backups.add(bean);
            }
        }

        for (T t : backups) {
            importEntity(t);
        }

        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    protected T readEntity(String fileName) {
        try {
            String beanJson = BackupUtils.readFile(importDirectory.getAbsolutePath() + "/" + fileName);
            T bean = getBean();
            BackupUtils.fromJson(bean, beanJson);
            return bean;
        } catch (Exception e) {
            logger.error("Error at reading " + getImportDirectoryName() + " file " + fileName, e);
            return null;
        }
    }

    protected abstract void getEntityWithId(T entity) throws ApiException;

    protected BeanStatus checkEntity(T t) throws ApiException {
        try {
            getEntityWithId(t);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return BeanStatus.NOT_EXIST;
            }

            // TODO
        }
        return BeanStatus.MODIFIED;
    }

    protected void importEntity(T backupBean) throws ApiException {
        BeanStatus result = checkEntity(backupBean);

        if (result == BeanStatus.MODIFIED && updateEntityEnabled) {
            try {
                updateBean(backupBean);
                logger.info(getEntityIdentifierName(backupBean) + " updated");
            } catch (Exception e) {
                logger.error("Error at updating " + getEntityIdentifierName(backupBean)+ "." + e.getMessage());
            }

            return;
        }

        if (result == BeanStatus.NOT_EXIST && addEntityEnabled) {
            try {
                addBean(backupBean);
                logger.info(getEntityIdentifierName(backupBean) + " added");
            } catch (Exception e) {
                logger.error("Error at adding " + getEntityIdentifierName(backupBean) +". "+e.getMessage());
            }
        }
    }

    protected abstract T getBean() throws IOException, ParseException;

    protected abstract String getImportDirectoryName();

    protected abstract void addBean(T bean) throws ParseException, IOException, ApiException;

    protected abstract void updateBean(T bean) throws ParseException, IOException, ApiException;

    File getImportDirectory() {
        return importDirectory;
    }

    protected abstract String getEntityIdentifierName(T entitiy);

}
