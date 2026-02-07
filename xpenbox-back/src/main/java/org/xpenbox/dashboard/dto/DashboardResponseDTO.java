package org.xpenbox.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for the overall dashboard response.
 * Combines current period data and period filter data.
 * @param current The current period dashboard data.
 * @param period The period filter dashboard data.
 */
@RegisterForReflection
public record DashboardResponseDTO(

    @JsonProperty("current")
    DashboardCurrentPeriodDTO current,

    @JsonProperty("period")
    DashboardPeriodFilterDTO period
) { }
