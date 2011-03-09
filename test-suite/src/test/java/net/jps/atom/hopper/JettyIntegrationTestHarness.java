/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper;

import net.jps.atom.hopper.jetty.AtomHopperJettyServerBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author zinic
 */
public class JettyIntegrationTestHarness {

    public static Server serverInstance;

    @BeforeClass
    public static void startServer() throws Exception {
        serverInstance = new AtomHopperJettyServerBuilder(8080).newServer();
        serverInstance.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (serverInstance != null) {
            serverInstance.stop();
        }
    }
}
