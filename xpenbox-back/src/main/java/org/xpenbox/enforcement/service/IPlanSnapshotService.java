package org.xpenbox.enforcement.service;

import org.xpenbox.enforcement.dto.SnapshotPlanDTO;

/**
 * IPlanSnapshotService is an interface that defines the contract for managing plan snapshots for users. It provides methods to retrieve and clear plan snapshots based on user ID.
 */
public interface IPlanSnapshotService {
    /**
     * Retrieves the plan snapshot for a given user email.
     * @param email the email of the user for whom the plan snapshot is to be retrieved
     * @return a SnapshotPlanDTO containing the plan snapshot information for the specified user
     */
    SnapshotPlanDTO getPlanSnapshotByEmail(String email);

    /**
     * Clears the plan snapshot for a given user email.
     * @param email the email of the user for whom the plan snapshot is to be cleared
     */
    void clearPlanSnapshotByEmail(String email);
}
