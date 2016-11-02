package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Bean;
import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.RestoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mehmet Mustafa Demir
 */
abstract class BaseImporter<T extends Bean> implements ImporterInterface {
    private final Logger logger = LogManager.getLogger(BaseImporter.class);
    private OpsGenieClient opsGenieClient;
    private File importDirectory;
    private boolean addEntityEnabled;
    private boolean updateEntityEnabled;

    BaseImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntityEnabled, boolean updateEntityEnabled) {
        this.addEntityEnabled = addEntityEnabled;
        this.updateEntityEnabled = updateEntityEnabled;
        this.opsGenieClient = opsGenieClient;
        this.importDirectory = new File(backupRootDirectory + "/" + getImportDirectoryName() + "/");
    }

    public void restore() throws RestoreException {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");

        if (!importDirectory.exists()) {
            logger.error("Error : " + getImportDirectoryName() + " does not exist. Restoring " + getImportDirectoryName() + " skipeed");
            return;
        }

        String[] files = BackupUtils.getFileListOf(importDirectory);
        if (files == null || files.length == 0) {
            logger.error("Error : " + getImportDirectoryName() + " is empty. Restoring " + getImportDirectoryName() + " skipped");
            return;
        }


        List<T> backups = new ArrayList<T>();
        for (String fileName : files) {
            T bean = readEntity(fileName);
            if (bean != null) {
                backups.add(bean);
            }
        }

        try {
            importEntities(backups, retrieveEntities());
        } catch (Exception e) {
            logger.error("Error at listing " + getImportDirectoryName(), e);
        }


        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    protected T readEntity(String fileName) {
        try {
            String beanJson = BackupUtils.readFileAsJson(importDirectory.getAbsolutePath() + "/" + fileName);
            T bean = getBean();
            JsonUtils.fromJson(bean, beanJson);
            return bean;
        } catch (Exception e) {
            logger.error("Error at reading " + getImportDirectoryName() + " file " + fileName, e);
            return null;
        }
    }

    protected abstract BeanStatus checkEntities(T oldEntity, T currentEntity);

    void importEntities(List<T> backupList, List<T> currentList) {
        for (T backupBean : backupList) {
            importEntity(currentList, backupBean);
        }
    }

    private void importEntity(List<T> currentList, T backupBean) {
        for (T current : currentList) {

            BeanStatus result = checkEntities(backupBean, current);
            if (result == BeanStatus.NOT_CHANGED) {
                return;
            }

            if (result == BeanStatus.MODIFIED && updateEntityEnabled) {
                try {
                    updateBean(backupBean);
                    logger.info(getEntityIdentifierName(backupBean) + " updated");
                } catch (Exception e) {
                    logger.error("Error at updating " + getEntityIdentifierName(backupBean), e);
                }

                return;
            }
        }

        if (addEntityEnabled) {
            try {
                addBean(backupBean);
                logger.info(getEntityIdentifierName(backupBean) + " added");
            } catch (Exception e) {
                logger.error("Error at adding " + getEntityIdentifierName(backupBean), e);
            }
        }
    }

    protected abstract T getBean() throws IOException, ParseException;

    protected abstract String getImportDirectoryName();

    protected abstract void addBean(T bean) throws ParseException, OpsGenieClientException, IOException;

    protected abstract void updateBean(T bean) throws ParseException, OpsGenieClientException, IOException;

    protected abstract List<T> retrieveEntities() throws ParseException, OpsGenieClientException, IOException;

    protected OpsGenieClient getOpsGenieClient() {
        return opsGenieClient;
    }

    File getImportDirectory() {
        return importDirectory;
    }

    protected boolean isSame(T oldEntity, T currentEntity) {
        return oldEntity.toString().equals(currentEntity.toString());
    }

    protected abstract String getEntityIdentifierName(T entitiy);

}
