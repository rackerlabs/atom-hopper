package org.atomhopper.server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStream;

import org.atomhopper.jetty.AtomHopperJettyServerBuilder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AtomHopperServerControl {
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServerControl.class);

    private Server serverInstance;
    private CommandLineArguments commandLineArgs;
    private static final String LOCALHOST_IP = "127.0.0.1";

    public AtomHopperServerControl(CommandLineArguments commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public void startAtomHopper() {

        try {
            if (commandLineArgs.configFile == null) {
                serverInstance = new AtomHopperJettyServerBuilder(getPort()).newServer();
            } else {
                serverInstance = new AtomHopperJettyServerBuilder(getPort(), commandLineArgs.configFile).newServer();
            }
            serverInstance.setStopAtShutdown(true);
            serverInstance.start();
            Thread monitor = new MonitorThread(serverInstance, getStopPort(), LOCALHOST_IP);
            monitor.start();
            LOG.info("Atom Hopper running and listening on port: " + Integer.toString(commandLineArgs.port));
        } catch (Exception e) {
            LOG.error("Atom Hopper could not be started: " + e.getMessage());
        }
    }

    public void stopAtomHopper() {
        try {
            Socket s = new Socket(InetAddress.getByName(LOCALHOST_IP), commandLineArgs.stopport);
            OutputStream out = s.getOutputStream();
            LOG.info("Sending Atom Hopper stop request");
            out.write(("\r\n").getBytes());
            out.flush();
            s.close();
        } catch (IOException ioex) {
            LOG.error("An error occured while attempting to stop Atom Hopper: " + ioex.getMessage());
        }
    }

    private int getPort() {
        return commandLineArgs.port;
    }

    private int getStopPort() {
        return commandLineArgs.stopport;
    }
}
