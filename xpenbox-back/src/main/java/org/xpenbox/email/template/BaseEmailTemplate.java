package org.xpenbox.email.template;

import java.util.Map;

import org.jboss.logging.Logger;

import io.quarkus.qute.Engine;

/**
 * Base class for email templates, providing common functionality for rendering content.
 * Subclasses must implement methods to specify the template name, model data, and subject.
 */
public abstract class BaseEmailTemplate {
    private static final Logger LOG = Logger.getLogger(BaseEmailTemplate.class);

    protected final Engine quteEngine;

    protected BaseEmailTemplate(Engine quteEngine) {
        this.quteEngine = quteEngine;
    }

    /**
     * Gets the email subject line for this template.
     * @return The email subject as a string
    */
   public abstract String getSubject();
   
   /**
    * Provides the data model for rendering the email template. Subclasses should return a map of key-value pairs
    * @return A map containing the data to be used in the email template
   */
  protected abstract Map<String, Object> getTemplateModel();
  
  /**
   * Gets the name of the Qute template to use for rendering this email. Subclasses must specify the template path.
   * @return The name of the email template as a string (e.g., "email/verify-email")
   */
  protected abstract String getTemplateName();

    /**
     * Renders the email content using the specified template and model data.
     * @return The rendered email content as a string
     */
    public String getContent() {
        LOG.debugf("Rendering email template: %s", getTemplateName());
        return quteEngine.getTemplate(getTemplateName())
                .data(getTemplateModel())
                .render();
    }

}
