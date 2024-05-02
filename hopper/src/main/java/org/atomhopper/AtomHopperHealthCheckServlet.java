package org.atomhopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;


public class AtomHopperHealthCheckServlet extends HttpServlet {
    
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperHealthCheckServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        super.doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Returns the health state of the Atom Hopper Servlet";
    }

}
