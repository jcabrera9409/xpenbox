package org.xpenbox.enforcement.dto;

import org.xpenbox.payment.dto.PlanResponseDTO;

/**
 * Data Transfer Object (DTO) for representing a snapshot of a user's plan. It contains the user ID and the corresponding plan information encapsulated in a PlanResponseDTO.
 * @param userId the ID of the user for whom the plan snapshot is being represented
 * @param plan the PlanResponseDTO containing the plan information for the specified user
 */
public record SnapshotPlanDTO (
    Long userId,
    PlanResponseDTO plan
) { }
