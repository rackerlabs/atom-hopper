package org.atomhopper.util;

import org.apache.abdera.protocol.server.ResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for validating response content and ensuring proper XML formatting
 * and content type headers in error responses.
 */
public class ResponseValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseValidator.class);
    private static final String XML_CONTENT_TYPE = "application/xml";
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    
    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
        // Disable external entity processing for security
        try {
            DOCUMENT_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DOCUMENT_BUILDER_FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false);
            DOCUMENT_BUILDER_FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            LOG.warn("Could not configure XML security features: {}", e.getMessage());
        }
    }

    /**
     * Validates that a ResponseContext has the correct XML content type
     * 
     * @param response The ResponseContext to validate
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateXmlContentType(ResponseContext response) {
        if (response == null) {
            return ValidationResult.failure("Response is null");
        }

        String contentType = response.getContentType() != null ? 
                           response.getContentType().toString() : null;
        
        if (contentType == null) {
            return ValidationResult.failure("Content-Type header is missing");
        }

        if (!contentType.startsWith(XML_CONTENT_TYPE)) {
            return ValidationResult.failure(
                String.format("Expected Content-Type to start with '%s', but was '%s'", 
                             XML_CONTENT_TYPE, contentType));
        }

        return ValidationResult.success("Content-Type is valid XML");
    }

    /**
     * Validates that response body is well-formed XML
     * 
     * @param xmlContent The XML content to validate
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateXmlWellFormedness(String xmlContent) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return ValidationResult.failure("XML content is null or empty");
        }

        try {
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            try (ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
                builder.parse(xmlStream);
            }
            return ValidationResult.success("XML is well-formed");
        } catch (ParserConfigurationException e) {
            LOG.error("Parser configuration error", e);
            return ValidationResult.failure("Parser configuration error: " + e.getMessage());
        } catch (SAXException e) {
            LOG.warn("XML parsing error: {}", e.getMessage());
            return ValidationResult.failure("XML is not well-formed: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("IO error while parsing XML", e);
            return ValidationResult.failure("IO error while parsing XML: " + e.getMessage());
        }
    }

    /**
     * Validates that response body is well-formed XML from InputStream
     * 
     * @param xmlStream The XML InputStream to validate
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateXmlWellFormedness(InputStream xmlStream) {
        if (xmlStream == null) {
            return ValidationResult.failure("XML stream is null");
        }

        try {
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            builder.parse(xmlStream);
            return ValidationResult.success("XML is well-formed");
        } catch (ParserConfigurationException e) {
            LOG.error("Parser configuration error", e);
            return ValidationResult.failure("Parser configuration error: " + e.getMessage());
        } catch (SAXException e) {
            LOG.warn("XML parsing error: {}", e.getMessage());
            return ValidationResult.failure("XML is not well-formed: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("IO error while parsing XML", e);
            return ValidationResult.failure("IO error while parsing XML: " + e.getMessage());
        }
    }

    /**
     * Comprehensive validation of error response
     * 
     * @param response The ResponseContext to validate
     * @param expectedStatusCode The expected HTTP status code
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateErrorResponse(ResponseContext response, int expectedStatusCode) {
        if (response == null) {
            return ValidationResult.failure("Response is null");
        }

        // Validate status code
        if (response.getStatus() != expectedStatusCode) {
            return ValidationResult.failure(
                String.format("Expected status code %d, but was %d", 
                             expectedStatusCode, response.getStatus()));
        }

        // Validate content type
        ValidationResult contentTypeResult = validateXmlContentType(response);
        if (!contentTypeResult.isValid()) {
            return contentTypeResult;
        }

        // Note: ResponseContext doesn't provide direct access to response body stream
        // XML validation would need to be done at a higher level where the response body is available

        return ValidationResult.success("Error response is valid");
    }

    /**
     * Checks if the response indicates a server error (5xx status codes)
     * 
     * @param response The ResponseContext to check
     * @return true if response is a server error, false otherwise
     */
    public static boolean isServerError(ResponseContext response) {
        return response != null && response.getStatus() >= 500 && response.getStatus() < 600;
    }

    /**
     * Checks if the response indicates a client error (4xx status codes)
     * 
     * @param response The ResponseContext to check
     * @return true if response is a client error, false otherwise
     */
    public static boolean isClientError(ResponseContext response) {
        return response != null && response.getStatus() >= 400 && response.getStatus() < 500;
    }

    /**
     * Checks if the response indicates an error (4xx or 5xx status codes)
     * 
     * @param response The ResponseContext to check
     * @return true if response is an error, false otherwise
     */
    public static boolean isError(ResponseContext response) {
        return isClientError(response) || isServerError(response);
    }

    /**
     * Result class for validation operations
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, message='%s'}", valid, message);
        }
    }
}