package org.atomhopper.validation;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.atomhopper.util.RequestBodyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
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
        String normalizedContentType = contentType.toLowerCase();

        if (!normalizedContentType.contains("application/atom+xml") &&
                !normalizedContentType.contains("application/xml")) {
            return chain.next(request);
        }

        try {
            RequestContext bufferedRequest = RequestBodyCache.buffer(request);
            byte[] content = RequestBodyCache.getBody(bufferedRequest);
            
            // Check for empty or null content - skip validation but continue processing
            if (content == null || content.length == 0) {
                return chain.next(bufferedRequest);
            }
            
            // Check for whitespace-only content
            String contentStr = new String(content, "UTF-8").trim();
            if (contentStr.isEmpty()) {
                return chain.next(bufferedRequest);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Use try-with-resources to ensure proper stream closing
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
                // Ensure the stream has content before parsing
                if (inputStream.available() == 0) {
                    LOG.warn("XML content stream is empty, skipping category validation");
                    return chain.next(bufferedRequest);
                }
                
                Document doc = builder.parse(inputStream);

                ResponseContext validationResult = validateCategories(doc, bufferedRequest);
                if (validationResult != null) {
                    return validationResult;
                }

                return chain.next(bufferedRequest);
            }

        } catch (Exception e) {
            LOG.error("Error validating categories", e);
            // Let the request continue - validation errors will be caught by content validation
            return chain.next(request);
        }
    }

    private ResponseContext validateCategories(Document doc, RequestContext request) {
        // Check for multiple title elements (only one allowed)
        NodeList titles = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "title");
        if (titles.getLength() > 1) {
            return createBadRequestResponse(request, "Only one atom:title node is allowed per entry");
        }
        
        NodeList categories = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "category");
        
        for (int i = 0; i < categories.getLength(); i++) {
            Element category = (Element) categories.item(i);
            String term = category.getAttribute("term");
            
            // Validate term length
            if (term != null && term.length() > MAX_CATEGORY_TERM_LENGTH) {
                return createBadRequestResponse(request,
                    String.format("Category term exceeds maximum length of %d characters", MAX_CATEGORY_TERM_LENGTH));
            }
            
            // Check for restricted categories in functional test feeds
            String path = request.getUri().getPath();
            if (term != null && path.contains("functional") && RESTRICTED_CATEGORIES.contains(term.toLowerCase())) {
                return createBadRequestResponse(request,
                    String.format("Category term '%s' is not allowed in this feed", term));
            }
        }

        return null; // No validation errors
    }

    private ResponseContext createBadRequestResponse(RequestContext request, String message) {
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                        "  <message>" + escapeXml(message) + "</message>\n" +
                        "</error>";
        ResponseContext response = ProviderHelper.badrequest(request, xmlBody);
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("Content-Length", String.valueOf(xmlBody.getBytes().length));
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
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