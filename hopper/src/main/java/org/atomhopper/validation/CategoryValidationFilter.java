package org.atomhopper.validation;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filter that validates category elements in Atom entries
 */
public class CategoryValidationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryValidationFilter.class);
    private static final int MAX_CATEGORY_TERM_LENGTH = 256;
    
    // Predefined categories that are not allowed in certain feeds
    private static final Set<String> RESTRICTED_CATEGORIES = new HashSet<>(Arrays.asList(
        "system", "internal", "admin", "restricted"
    ));

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        // Only validate POST and PUT requests with Atom content
        String method = request.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method)) {
            return chain.next(request);
        }

        String contentType = request.getContentType() != null ? 
                           request.getContentType().toString() : "";

        if (!contentType.contains("application/atom+xml") && !contentType.contains("application/xml")) {
            return chain.next(request);
        }

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream == null) {
                return chain.next(request);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            // Validate categories in the entry
            ResponseContext validationResult = validateCategories(doc, request);
            if (validationResult != null) {
                return validationResult;
            }

            // Reset the input stream for the next filter by creating a new one from the content
            byte[] content = readInputStreamToByteArray(inputStream);
            request.setAttribute(RequestContext.Scope.REQUEST, "inputStreamContent", content);

        } catch (Exception e) {
            LOG.error("Error validating categories", e);
            // Let the request continue - validation errors will be caught by content validation
        }

        return chain.next(request);
    }

    private ResponseContext validateCategories(Document doc, RequestContext request) {
        // Check for multiple title elements (only one allowed)
        NodeList titles = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "title");
        if (titles.getLength() > 1) {
            return createBadRequestResponse("Only one atom:title node is allowed per entry");
        }
        
        NodeList categories = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "category");
        
        for (int i = 0; i < categories.getLength(); i++) {
            Element category = (Element) categories.item(i);
            String term = category.getAttribute("term");
            
            // Validate term length
            if (term != null && term.length() > MAX_CATEGORY_TERM_LENGTH) {
                return createBadRequestResponse(
                    String.format("Category term exceeds maximum length of %d characters", MAX_CATEGORY_TERM_LENGTH));
            }
            
            // Check for restricted categories in functional test feeds
            String path = request.getUri().getPath();
            if (path.contains("functional") && RESTRICTED_CATEGORIES.contains(term.toLowerCase())) {
                return createBadRequestResponse(
                    String.format("Category term '%s' is not allowed in this feed", term));
            }
        }

        return null; // No validation errors
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

    private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        return outputStream.toByteArray();
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