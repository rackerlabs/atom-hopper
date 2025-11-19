package org.atomhopper.validation;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.ProviderHelper;
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
 * Filter that validates request content for proper format and structure
 */
public class ContentValidationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ContentValidationFilter.class);
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    
    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
        // Security settings
        try {
            DOCUMENT_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DOCUMENT_BUILDER_FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false);
            DOCUMENT_BUILDER_FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            LOG.warn("Could not configure XML security features: {}", e.getMessage());
        }
    }

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        // Only validate POST and PUT requests with content
        String method = request.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method)) {
            return chain.next(request);
        }

        String contentType = request.getContentType() != null ? 
                           request.getContentType().toString() : "";

        // Check for obvious content type mismatches
        if (contentType.isEmpty()) {
            return createBadRequestResponse("Content-Type header is required for POST/PUT requests");
        }
        
        // Validate based on content type
        if (contentType.contains("application/json")) {
            return validateJsonContent(request, chain);
        } else if (contentType.contains("application/xml") || contentType.contains("application/atom+xml")) {
            return validateXmlContent(request, chain);
        }
        
        // For other content types, let the request continue
        return chain.next(request);
    }

    private ResponseContext validateXmlContent(RequestContext request, FilterChain chain) {
        try {
            // Read the request body
            InputStream inputStream = request.getInputStream();
            if (inputStream == null) {
                return createBadRequestResponse("Request body is empty");
            }

            // Read the content into a byte array so we can validate it without consuming the stream
            byte[] content = readInputStreamToByteArray(inputStream);
            if (content.length == 0) {
                return createBadRequestResponse("Request body is empty");
            }

            // Validate XML well-formedness
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(content));
            
            // Additional Atom-specific validations
            String rootElement = doc.getDocumentElement().getLocalName();
            if (!"entry".equals(rootElement) && !"feed".equals(rootElement)) {
                return createBadRequestResponse("Invalid Atom document. Root element must be 'entry' or 'feed'");
            }

            // Validate required Atom elements
            if ("entry".equals(rootElement)) {
                if (!hasRequiredAtomElements(doc)) {
                    return createBadRequestResponse("Invalid Atom entry. Missing required elements");
                }
            }

        } catch (ParserConfigurationException e) {
            LOG.error("XML parser configuration error", e);
            return createBadRequestResponse("XML parser configuration error");
        } catch (SAXException e) {
            LOG.warn("Invalid XML content: {}", e.getMessage());
            return createBadRequestResponse("Invalid XML: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Error reading request content", e);
            return createBadRequestResponse("Error reading request content");
        }

        return chain.next(request);
    }

    private ResponseContext validateJsonContent(RequestContext request, FilterChain chain) {
        try {
            // Read the request body
            InputStream inputStream = request.getInputStream();
            if (inputStream == null) {
                return createBadRequestResponse("Request body is empty");
            }

            // Read the content into a byte array so we can validate it without consuming the stream
            byte[] content = readInputStreamToByteArray(inputStream);
            if (content.length == 0) {
                return createBadRequestResponse("Request body is empty");
            }

            String jsonContent = new String(content, "UTF-8");
            
            // Validate JSON syntax
            if (!isValidJsonSyntax(jsonContent)) {
                return createBadRequestResponse("Invalid JSON syntax");
            }

        } catch (IOException e) {
            LOG.error("Error reading JSON content", e);
            return createBadRequestResponse("Error reading request content");
        }

        return chain.next(request);
    }

    private boolean hasRequiredAtomElements(Document doc) {
        // Check for required Atom entry elements
        return doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "title").getLength() > 0 &&
               doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "id").getLength() > 0 &&
               doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "updated").getLength() > 0;
    }

    private boolean isValidJsonSyntax(String json) {
        String payload = json == null ? "" : json.trim();
        if (payload.isEmpty()) {
            return false;
        }

        try {
            JsonParser.parseString(payload);
            return true;
        } catch (JsonSyntaxException ex) {
            LOG.warn("Invalid JSON payload: {}", ex.getMessage());
            return false;
        }
    }

    private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
    }

    private ResponseContext createBadRequestResponse(String message) {
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                        "  <message>" + escapeXml(message) + "</message>\n" +
                        "</error>";
        ResponseContext response = ProviderHelper.badrequest(null, xmlBody);
        response.setContentType("application/xml; charset=utf-8");
        return response;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}