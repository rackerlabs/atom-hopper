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

    public static Server serverInstance;

    @BeforeClass
    public static void startServer() throws Exception {
        serverInstance = new AtomHopperJettyServerBuilder(getPort()).newServer();
        serverInstance.start();
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
