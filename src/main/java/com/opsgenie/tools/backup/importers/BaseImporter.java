package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Bean;
import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.BackupUtils;
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
    private boolean addEntity;
    private boolean updateEntitiy;

    public BaseImporter(OpsGenieClient opsGenieClient, String backupRootDirectory, boolean addEntity, boolean updateEntitiy) {
        this.addEntity = addEntity;
        this.updateEntitiy = updateEntitiy;
        this.opsGenieClient = opsGenieClient;
        this.importDirectory = new File(backupRootDirectory + "/" + getImportDirectoryName() + "/");
    }

    public void restore() {
        logger.info("Restoring " + getImportDirectoryName() + " operation is started");
        List<T> backups = new ArrayList<T>();
        String[] files = BackupUtils.getFileListOf(importDirectory);
        for (String fileName : files) {
            try {
                String beanJson = BackupUtils.readFileAsJson(importDirectory.getAbsolutePath() + "/" + fileName);
                T bean = getBean();
                JsonUtils.fromJson(bean, beanJson);
                backups.add(bean);
            } catch (Exception e) {
                logger.error("Error at reading " + getImportDirectoryName() + " file " + fileName, e);
            }
        }
        try {
            importEntities(backups, retrieveEntities());
        } catch (Exception e) {
            logger.error("Error at listing " + getImportDirectoryName(), e);
        }
        logger.info("Restoring " + getImportDirectoryName() + " operation is finished");
    }

    protected abstract int checkEntities(T oldEntity, T currentEntity);

    protected void importEntities(List<T> backupList, List<T> currentList) {
        for (T backupBean : backupList) {
            boolean notExist = true;
            for (T current : currentList) {
                int checkResult = checkEntities(backupBean, current);
                if (checkResult == 0) {
                    notExist = false;
                    break;
                } else if (checkResult == 1) {
                    notExist = false;
                    if (updateEntitiy)
                        try {
                            updateBean(backupBean);
                            logger.info(getEntityIdentifierName(backupBean) + " updated");
                        } catch (Exception e) {
                            logger.error("Error at updating " + getEntityIdentifierName(backupBean), e);
                        }
                    break;
                }
            }
            if (notExist && addEntity)
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

    protected File getImportDirectory() {
        return importDirectory;
    }

    protected boolean isSame(T oldEntity, T currentEntity) {
        return oldEntity.toString().equals(currentEntity.toString());
    }

    protected abstract String getEntityIdentifierName(T entitiy);
}
