package org.xpenbox.email.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

/**
 * Client for sending emails through an external email API. This class is responsible for constructing the HTTP request,
 * handling the response, and logging the process. It uses Java's HttpClient for making requests and Jackson for JSON processing.
 */
@Singleton
public class EmailApiClient {
    private static final Logger LOG = Logger.getLogger(EmailApiClient.class);

    @ConfigProperty(name = "email.api.url")
    private String emailApiUrl;

    @ConfigProperty(name = "email.api.timeout")
    private Integer timeout;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        validateConfiguration();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeout))
            .followRedirects(HttpClient.Redirect.NORMAL) // Sigue redirects (302) automáticamente
            .build();
        this.objectMapper = new ObjectMapper();
        LOG.infof("EmailApiClient inicializado con endpoint: %s y timeout: %dms", emailApiUrl, timeout);
    }

    /**
     * Envía un correo electrónico utilizando la API externa. Este método construye la solicitud HTTP, maneja la respuesta y registra el proceso.
     * @param receiver El destinatario del correo electrónico
     * @param subject El asunto del correo electrónico
     * @param htmlContent El contenido HTML del correo electrónico
     * @return true si el correo se envió exitosamente, false en caso contrario
     */
    public boolean sendEmail(String receiver, String subject, String htmlContent) {
        long startTime = System.currentTimeMillis();
        LOG.infof("Sending email to %s with subject '%s'", receiver, subject);

        try {
            long buildStartTime = System.currentTimeMillis();
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("receiver", receiver);
            payload.put("subject", subject);
            payload.put("htmlBody", htmlContent);

            String requestBody = objectMapper.writeValueAsString(payload);
            long buildEndTime = System.currentTimeMillis();
            LOG.debugf("[Receiver: %s] Email request built in %d ms with length %d", receiver, (buildEndTime - buildStartTime), requestBody.length());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(emailApiUrl))
                .timeout(Duration.ofMillis(timeout))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            long sendStartTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long sendEndTime = System.currentTimeMillis() - sendStartTime;
            
            String responseBody = response.body();
            LOG.debugf("[Receiver: %s] Response received in %dms (size: %d bytes)", 
                      receiver, sendEndTime, responseBody.length());
            LOG.debugf("[Receiver: %s] Email API response: %s", receiver, responseBody);

            long parseStartTime = System.currentTimeMillis();
            ObjectNode responseJson = (ObjectNode) objectMapper.readTree(responseBody);
            long parseEndTime = System.currentTimeMillis() - parseStartTime;
            LOG.debugf("[Receiver: %s] Response parsed in %d ms", receiver, parseEndTime);

            if (responseJson.has("status") && responseJson.get("status").asInt() == 200) {
                long duration = System.currentTimeMillis() - startTime;
                LOG.infof("Email sent successfully to %s in %d ms", receiver, duration);
                return true;
            } else {
                String errorMsg = responseJson.has("message") 
                    ? responseJson.get("message").asText() 
                    : "Unknown error";
                long duration = System.currentTimeMillis() - startTime;
                LOG.errorf("Failed to send email to %s in %d ms. Error: %s", receiver, duration, errorMsg);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOG.errorf(e, "Exception occurred while sending email to %s after %d ms", receiver, duration);
        }
        return false;
    }

    /**
     * Validates the email API configuration properties. This method checks that the email API URL is provided and well-formed,
     * and that the timeout value is a positive integer. If any validation fails, an IllegalStateException is thrown with a descriptive message.
     */
    private void validateConfiguration() {
        if (emailApiUrl == null || emailApiUrl.isBlank()) {
            throw new IllegalStateException("email.api.url is required. " +
                "Please set the environment variable EMAIL_API_URL or provide it in application.properties");
        }
        
        try {
            URI.create(emailApiUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("email.api.url is not a valid URL: " + emailApiUrl, e);
        }
        
        if (timeout == null || timeout <= 0) {
            throw new IllegalStateException("email.api.timeout must be a positive value. " +
                "Current value: " + timeout);
        }
        
        LOG.infof("Email configuration validated successfully: endpoint=%s, timeout=%dms", 
            emailApiUrl, timeout);
    }
}
