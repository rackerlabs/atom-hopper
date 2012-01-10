package org.atomhopper;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AtomHopperVersionServlet extends HttpServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperVersionServlet.class);
    private final String POM_PROPERTIES_LOCATION = "META-INF/maven/org.atomhopper/ah-war/pom.properties";

    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream inStream = getServletContext().getResourceAsStream(POM_PROPERTIES_LOCATION);
            properties.load(inStream);
            inStream.close();            
        } catch (Exception e){
            LOG.error("Unable to load pom.properties", e);
        }
        return properties;
    }

    private Properties getProperties(){
        return loadProperties();
    }    

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            out.println(new Gson().toJson(getProperties()));
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
        return "Returns the current Atom Hopper pom.properties in JSON format";
    }
}
