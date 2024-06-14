package org.atomhopper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.atomhopper.servlet.ServletInitParameter;
import org.atomhopper.servlet.ServletSpringContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

public class MarkerRaceConditionTest {
    public static void main(String[] args) throws Exception {
        final int port = 8080;
        final int numPosters = 20;
        final int numEntries = 10000;

        String url = "http://localhost:" + port + "/namespace/feed";
        String prefix = UUID.randomUUID().toString();
        String config = getResource("marker-race-condition/atom-server.cfg.xml");
        String context = getResource("marker-race-condition/application-context-h2.xml");
        System.setProperty("org.jboss.logging.provider", "slf4j");

        Server serverInstance = buildNewInstance(port, config, context);
        serverInstance.setStopAtShutdown(true);
        serverInstance.start();

        ensureFeedExists(url);

        final FollowByMarker follower = new FollowByMarker(url, prefix);
        follower.start("follower");

        final AtomicInteger numPosted = new AtomicInteger();
        final AtomicInteger numFailed = new AtomicInteger();
        PostInParallel posters = new PostInParallel(url, prefix, numPosted, numFailed);
        long postStart = System.nanoTime();
        posters.run(numPosters, numEntries);
        long postEnd = System.nanoTime();

        List<String> seen = follower.stop();
        Set<String> unique = new HashSet<String>(seen);
        List<String> sorted = new ArrayList<String>(seen);
        Collections.sort(sorted);
        if (unique.size() != seen.size()) {
            System.out.printf("Found %d duplicates\n", unique.size() - seen.size());
            for (String s : unique) {
                int start = Collections.binarySearch(sorted, s);
                int pos = start;
                while (pos < sorted.size() - 1 && sorted.get(pos+1).equals(s)) {
                    pos++;
                }
                if ((pos - start) > 1) {
                    System.out.printf("Found %d copies of %s\n", pos - start, s);
                }
            }
        }

        serverInstance.stop();

        System.out.printf("Posting entries took %f ms\n", (postEnd - postStart) / 1000000.0);
        System.out.printf("num posted: %d num failed: %d, num seen: %d\n", numPosted.get(), numFailed.get(), unique.size());

        for (int poster = 0; poster < numPosters; poster++) {
            for (int entry = 0; entry < numEntries; entry++) {
                String title = String.format("%s-%s-%s", prefix, entry, poster);
                if (Collections.binarySearch(sorted, title) < 0) {
                    System.out.printf("Did not find %s\n", title);
                }
            }
        }

        System.exit(numPosted.get() == unique.size() ? 0 : 1);
    }

    private static String getResource(String resource) {
        return MarkerRaceConditionTest.class.getClassLoader().getResource(resource).toString();
    }

    private static void ensureFeedExists(String url) {
        boolean posted = false;
        while (!posted) {
            final PostMethod post = new PostMethod(url);
            final String title = "[MarkerRaceConditionTest start]";
            final String body = "<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\">"
                + "<title>" + title + "</title>"
                + "<content><p>" + title + "</p></content></entry>";
            try {
                post.setRequestEntity(new StringRequestEntity(body, "application/atom+xml", "ascii"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            final HttpClient httpClient = new HttpClient();

            try {
                final int result = httpClient.executeMethod(post);
                if (result != 201) {
                    continue;
                }
                posted = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Server buildNewInstance(int portNumber, String configurationPathAndFile, String applicationContext) {
        final Server server = new Server(portNumber);
        final ServletContextHandler servletContext = new ServletContextHandler(server, "/");
        servletContext.getInitParams().put("contextConfigLocation", applicationContext);
        servletContext.addEventListener(new ContextLoaderListener());

        final ServletHolder atomHopServer = new ServletHolder(AtomHopperServlet.class);
        atomHopServer.setInitParameter(
            ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString(), ServletSpringContext.class.getName()
        );
        atomHopServer.setInitParameter(
            ServletInitParameter.CONFIGURATION_LOCATION.toString(), configurationPathAndFile
        );

        servletContext.addServlet(atomHopServer, "/*");

        return server;
    }
}
