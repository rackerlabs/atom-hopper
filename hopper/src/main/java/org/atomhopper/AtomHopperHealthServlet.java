package org.atomhopper;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Health check servlet for ECS load balancers and monitoring systems.
 * Returns a lightweight JSON response with service status information.
 */
public class AtomHopperHealthServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperHealthServlet.class);
    private static final String POM_PROPERTIES_LOCATION = "META-INF/maven/org.atomhopper/atomhopper/pom.properties";

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            try (InputStream inStream = getServletContext().getResourceAsStream(POM_PROPERTIES_LOCATION)) {
                if (inStream != null) {
                    properties.load(inStream);
                }
            }
        } catch (Exception e){
            LOG.debug("Unable to load pom.properties, using defaults", e);
        }
        return properties;
    }

    private Map<String, String> getHealthResponse(){
        Properties properties = loadProperties();
        Map<String, String> healthResponse = new HashMap<String, String>();
        
        // Set required health check fields
        healthResponse.put("service", "atomhopper");
        healthResponse.put("status", "ok");
        
        // Get version from properties, fallback to default if not available
        String version = properties.getProperty("version", "1.0");
        healthResponse.put("version", version);
        
        return healthResponse;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        PrintWriter out = response.getWriter();
        try {
            out.println(new Gson().toJson(getHealthResponse()));
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Support HEAD requests for health checks
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public String getServletInfo() {
        return "Health check endpoint for ECS load balancers - returns service status in JSON format";
    }
}