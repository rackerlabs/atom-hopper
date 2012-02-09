package org.atomhopper;

import org.apache.log4j.PropertyConfigurator;

import javax.servlet.http.HttpServlet;
import java.io.File;

public class Log4jInit extends HttpServlet {

    public void init() {

        String configFilePath = getInitParameter("log4j-config-file");
        File file = new File(configFilePath);

        if (file.canRead()) {
            PropertyConfigurator.configure(configFilePath);
        }
    }
}