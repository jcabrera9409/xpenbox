package org.xpenbox.dashboard.service;

import org.xpenbox.dashboard.dto.DashboardResponseDTO;
import org.xpenbox.dashboard.dto.PeriodFilter;

/**
 * Service interface for managing dashboard data.
 */
public interface IDashboardService {
    
    /**
     * Generates dashboard data based on the provided period filter and user email.
     * @param periodFilter The filter containing the period for which to generate the dashboard data.
     * @param userEmail The email of the user requesting the dashboard data.
     * @return A DTO containing the generated dashboard data.
     */
    DashboardResponseDTO generateDashboardData(PeriodFilter periodFilter, String userEmail);
}
