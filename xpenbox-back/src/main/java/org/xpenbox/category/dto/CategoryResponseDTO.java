package org.xpenbox.category.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Category details.
 * @param resourceCode              the unique resource code of the category
 * @param name                      the name of the category
 * @param color                     the color associated with the category
 * @param lastUsedDateTimestamp     the timestamp of the last used date
 * @param usageCount                the usage count of the category
 * @param state                     the state of the category
 */
@RegisterForReflection
public record CategoryResponseDTO (
    String resourceCode,
    String name,
    String color,
    Long lastUsedDateTimestamp,
    Long usageCount,
    Boolean state
) { }
