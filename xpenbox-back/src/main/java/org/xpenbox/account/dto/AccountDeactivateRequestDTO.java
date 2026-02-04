package org.xpenbox.account.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for deactivating an Account.
 * @param accountResourceCodeToTransfer Resource code of the account to transfer remaining balance to.
 */
@RegisterForReflection
public record AccountDeactivateRequestDTO(

    @Size(min = 1, max = 100, message = "Account resource code for transfer to must be between 1 and 100 characters")
    String accountResourceCodeToTransfer
) { }
