package org.atomhopper.server;


import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class MonitorThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServer.class);
    
    private ServerSocket socket;
    private final Server serverInstance;
    private static final String MONITOR_NAME = "StopMonitor";

    public MonitorThread(Server serverInstance, final int stopPort, final String ipAddress) {
        this.serverInstance = serverInstance;
        
        setDaemon(true);
        setName(MONITOR_NAME);
        
        try {
            socket = new ServerSocket(stopPort, 1, InetAddress.getByName(ipAddress));
        } catch (Exception e) {
            LOG.error("Fatal error while monitoring or trying to stop: " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        Socket accept;

        try {
            accept = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
            reader.readLine();
            LOG.info("Stopping Atom Hopper...");
            serverInstance.stop();
            LOG.info("Atom Hopper has been stopped");
            accept.close();
            socket.close();
        } catch (Exception e) {
            LOG.error("Fatal error while monitoring or trying to stop: " + e.getMessage(), e);
        }
    }
}
