package org.atomhopper;

import org.atomhopper.jetty.AtomHopperJettyServerBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *  Note: This test harness expects to have the statically configured port
 * available.
 *
 * TODO: Make the port configurable?
 */
public class JettyIntegrationTestHarness {

    private static Server serverInstance;

    @BeforeClass
    public static void startServer() throws Exception {
        System.out.println("=== STARTING SERVER ON PORT " + getPort() + " ===");

        java.net.URL atomServerConfig = JettyIntegrationTestHarness.class.getResource("/META-INF/atom-server.cfg.xml");
        java.net.URL appContextConfig = JettyIntegrationTestHarness.class.getResource("/META-INF/application-context.xml");

        System.out.println("atom-server.cfg.xml found: " + (atomServerConfig != null));
        System.out.println("application-context.xml found: " + (appContextConfig != null));

        try {
            serverInstance = new AtomHopperJettyServerBuilder(getPort()).newServer();
            System.out.println("=== SERVER INSTANCE CREATED ===");
            serverInstance.start();
            System.out.println("=== SERVER STARTED SUCCESSFULLY ===");

            System.out.println("Server is running: " + serverInstance.isRunning());
            System.out.println("Server is started: " + serverInstance.isStarted());


        } catch (Exception e) {
            System.err.println("=== SERVER STARTUP FAILED ===");
            e.printStackTrace();
            throw e;
        }
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (serverInstance != null) {
            serverInstance.stop();
        }
    }

    public static int getPort() {
        return 24156;
    }
}

// added line 20 , try and catch block sop lines
//line 34 and 38