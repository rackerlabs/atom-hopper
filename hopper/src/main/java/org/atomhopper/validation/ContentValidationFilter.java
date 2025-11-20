package org.atomhopper.validation;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.atomhopper.util.RequestBodyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        String normalizedContentType = contentType.toLowerCase();

        // Check for obvious content type mismatches
        if (contentType.isEmpty()) {
            return createBadRequestResponse(request, "Content-Type header is required for POST/PUT requests");
        }
        
        // Validate based on content type
        if (normalizedContentType.contains("application/json")) {
            return validateJsonContent(request, chain);
        } else if (normalizedContentType.contains("application/xml") ||
                normalizedContentType.contains("application/atom+xml")) {
            return validateXmlContent(request, chain);
        }
        
        // For other content types, let the request continue
        return chain.next(request);
    }

    private ResponseContext validateXmlContent(RequestContext request, FilterChain chain) {
        try {
            RequestContext bufferedRequest = RequestBodyCache.buffer(request);
            byte[] content = RequestBodyCache.getBody(bufferedRequest);
            if (content.length == 0) {
                return createBadRequestResponse(bufferedRequest, "Request body is empty");
            }

            // Validate XML well-formedness
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(content));
            
            // Additional Atom-specific validations
            String rootElement = doc.getDocumentElement().getLocalName();
            if (!"entry".equals(rootElement) && !"feed".equals(rootElement)) {
                return createBadRequestResponse(bufferedRequest, "Invalid Atom document. Root element must be 'entry' or 'feed'");
            }

            // Validate required Atom elements
            if ("entry".equals(rootElement)) {
                if (!hasRequiredAtomElements(doc)) {
                    return createBadRequestResponse(bufferedRequest, "Invalid Atom entry. Missing required elements");
                }
            }

            return chain.next(bufferedRequest);
        } catch (ParserConfigurationException e) {
            LOG.error("XML parser configuration error", e);
            return createBadRequestResponse(request, "XML parser configuration error");
        } catch (SAXException e) {
            LOG.warn("Invalid XML content: {}", e.getMessage());
            return createBadRequestResponse(request, "Invalid XML: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Error reading request content", e);
            return createBadRequestResponse(request, "Error reading request content");
        }
    }

    private ResponseContext validateJsonContent(RequestContext request, FilterChain chain) {
        try {
            RequestContext bufferedRequest = RequestBodyCache.buffer(request);
            byte[] content = RequestBodyCache.getBody(bufferedRequest);
            if (content.length == 0) {
                return createBadRequestResponse(bufferedRequest, "Request body is empty");
            }

            String jsonContent = new String(content, "UTF-8");
            
            // Validate JSON syntax
            if (!isValidJsonSyntax(jsonContent)) {
                return createBadRequestResponse(bufferedRequest, "Invalid JSON syntax");
            }

            return chain.next(bufferedRequest);
        } catch (IOException e) {
            LOG.error("Error reading JSON content", e);
            return createBadRequestResponse(request, "Error reading request content");
        }
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

    private ResponseContext createBadRequestResponse(RequestContext request, String message) {
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                        "  <message>" + escapeXml(message) + "</message>\n" +
                        "</error>";
        ResponseContext response = ProviderHelper.badrequest(request, xmlBody);
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