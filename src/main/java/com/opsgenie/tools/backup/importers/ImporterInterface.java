package com.opsgenie.tools.backup.importers;

import com.opsgenie.tools.backup.RestoreException;

/**
 * Interface for import classes
 *
 * @author Mehmet Mustafa Demir
 */
public interface ImporterInterface {
    void restore() throws RestoreException;
}
