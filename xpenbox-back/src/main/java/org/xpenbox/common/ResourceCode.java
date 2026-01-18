package org.xpenbox.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.jboss.logging.Logger;

/**
 * Utility class for generating resource codes.
 */
public class ResourceCode {
    public static final Logger LOG = Logger.getLogger(ResourceCode.class);
    
    private static final String RESOURCE_CODE = "rc:xpenbox";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Enum representing different resource types.
     */
    private enum ResourceType {
        TOKEN("token"),
        ACCOUNT("account"),
        CATEGORY("category"),
        CREDIT_CARD("creditcard"),
        INCOME("income"),
        TRANSACTION("transaction");

        private final String value;

        ResourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Generate a resource code based on the specified resource type.
     *
     * @param resourceType the type of resource
     * @return the generated resource code
     */
    private static String generateResourceCode(ResourceType resourceType) {
        LOG.debug("Generating token resource code");

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString();

        return String.format("%s:%s:%s:%s", 
                RESOURCE_CODE, 
                resourceType.getValue(), 
                timestamp, 
                uuid); 
    }

    /**
     * Generate a token resource code.
     *
     * @return the generated token resource code
     */
    public static String generateTokenResourceCode() {
        return generateResourceCode(ResourceType.TOKEN);
    }

    /**
     * Generate an account resource code.
     *
     * @return the generated account resource code
     */
    public static String generateAccountResourceCode() {
        return generateResourceCode(ResourceType.ACCOUNT);
    }

    /**
     * Generate a category resource code.
     *
     * @return the generated category resource code
     */
    public static String generateCategoryResourceCode() {
        return generateResourceCode(ResourceType.CATEGORY);
    }
    
    /**
     * Generate a credit card resource code.
     *
     * @return the generated credit card resource code
     */
    public static String generateCreditCardResourceCode() {
        return generateResourceCode(ResourceType.CREDIT_CARD);
    }

    /**
     * Generate an income resource code.
     *
     * @return the generated income resource code
     */
    public static String generateIncomeResourceCode() {
        return generateResourceCode(ResourceType.INCOME);
    }

    /**
     * Generate a transaction resource code.
     *
     * @return the generated transaction resource code
     */
    public static String generateTransactionResourceCode() {
        return generateResourceCode(ResourceType.TRANSACTION);
    }
}