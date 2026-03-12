package org.atomhopper;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AtomHopperHealthServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperHealthServlet.class);

    private Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("service", "atomhopper");
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        
        // Basic health checks
        Map<String, String> checks = new HashMap<>();
        checks.put("application", "UP");
        checks.put("database", "UP"); // Could be enhanced with actual DB connectivity check
        health.put("checks", checks);
        
        return health;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        try {
            Map<String, Object> healthStatus = getHealthStatus();
            out.println(new Gson().toJson(healthStatus));
            LOG.debug("Served health check request successfully");
        } catch (Exception e) {
            LOG.error("Error processing health check request", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("service", "atomhopper");
            errorHealth.put("status", "DOWN");
            errorHealth.put("timestamp", Instant.now().toString());
            errorHealth.put("error", "Internal server error");
            out.println(new Gson().toJson(errorHealth));
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
        return "Returns the current Atom Hopper health status in JSON format";
    }
}