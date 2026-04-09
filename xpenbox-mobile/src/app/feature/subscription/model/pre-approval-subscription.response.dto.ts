/**
 * Data Transfer Object (DTO) for the response of a pre-approval subscription request.
 * @param initPointUrl - The URL where the user can complete the subscription process.
 */
export interface PreApprovalSubscriptionResponseDTO {
    initPointUrl: string;
}