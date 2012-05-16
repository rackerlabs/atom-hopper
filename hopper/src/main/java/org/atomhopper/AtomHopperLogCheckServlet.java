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


public class AtomHopperLogCheckServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperLogCheckServlet.class);
    private static final String[] LOGGING_MESSAGES = {
            "The following are messages entered into logging",
            "Test entry for INFO logging",
            "Test entry for WARN logging",
            "Test entry for DEBUG logging",
            "Test entry for ERROR logging"};

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            LOG.info(LOGGING_MESSAGES[1]);
            LOG.warn(LOGGING_MESSAGES[2]);
            LOG.debug(LOGGING_MESSAGES[3]);
            LOG.error(LOGGING_MESSAGES[4]);
            out.println(new Gson().toJson(LOGGING_MESSAGES));
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
        return "Returns in JSON format the messages that will be placed into the Atom Hopper log file";
    }
}
