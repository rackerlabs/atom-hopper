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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class AtomHopperVersionServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperVersionServlet.class);
    private static final String POM_PROPERTIES_LOCATION = "META-INF/maven/org.atomhopper/atomhopper/pom.properties";

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream inStream = getServletContext().getResourceAsStream(POM_PROPERTIES_LOCATION);
            if (inStream != null) {
                properties.load(inStream);
                inStream.close();
            } else {
                LOG.warn("Could not find pom.properties at {}", POM_PROPERTIES_LOCATION);
            }
        } catch (Exception e){
            LOG.error("Unable to load pom.properties", e);
        }
        return properties;
    }

    private Map<String, Object> getBuildInfo(){
        Properties properties = loadProperties();
        Map<String, Object> buildInfo = new HashMap<>();
        
        buildInfo.put("service", "atomhopper");
        buildInfo.put("version", properties.getProperty("version", "unknown"));
        buildInfo.put("build", properties.getProperty("buildNumber", "unknown"));
        buildInfo.put("status", "healthy");
        buildInfo.put("timestamp", Instant.now().toString());
        
        return buildInfo;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        try {
            Map<String, Object> buildInfo = getBuildInfo();
            out.println(new Gson().toJson(buildInfo));
            LOG.debug("Served buildinfo request successfully");
        } catch (Exception e) {
            LOG.error("Error processing buildinfo request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Internal server error\"}");
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
    public String getServletInfo() {
        return "Returns the current Atom Hopper build information in JSON format";
    }
}
