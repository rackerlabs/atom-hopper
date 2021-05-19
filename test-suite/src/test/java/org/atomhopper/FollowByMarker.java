package org.atomhopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;

public class FollowByMarker implements Runnable {
    private final String url;
    private final String prefix;
    private final Abdera abdera = new Abdera();
    private final AbderaClient client;


    public static void main(String[] args) throws IOException {
        final String url = args.length >= 1 ? args[0] : "http://localhost:8080/namespace/feed";
        final String prefix = args.length >= 2 ? args[1] : "fe3b98c9-d86a-4d93-afd5-c8d10c542882";

        FollowByMarker followByMarker = new FollowByMarker(url, prefix);
        followByMarker.run();
    }

    public FollowByMarker(String url, String prefix) {
        this.url = url;
        this.prefix = prefix;
        client = new AbderaClient(abdera);
    }

    boolean shouldRun;
    Thread thread;
    IRI marker = null;
    List<String> seen = new ArrayList<String>();

    public Thread start(String name) {
        shouldRun = true;
        thread = new Thread(this, name);
        thread.start();
        return thread;
    }

    public List<String> stop() throws InterruptedException {
        shouldRun = false;
        thread.interrupt();
        thread.join();
        fetchOnce();
        return seen;
    }

    public void run() {

        System.out.println("Looking for first entry with prefix " + prefix + " on " + url);
        while (shouldRun && marker == null) {
            System.out.println("GET " + url);
            ClientResponse resp = client.get(url + "?limit=1000", new RequestOptions(true));
            if (resp.getType() == Response.ResponseType.SUCCESS) {
                Document<Feed> doc = resp.getDocument();
                System.out.println("Got " + doc.getRoot().getEntries().size() + " entries");
                if (!doc.getRoot().getEntries().isEmpty()) {
                    System.out.println("First title: " + doc.getRoot().getEntries().get(0).getTitle());
                }
                for (Entry entry : doc.getRoot().getEntries()) {
                    if (!entry.getTitle().startsWith(prefix)) {
                        continue;
                    }
                    if (marker == null) {
                        marker = entry.getId();
                    }
                    seen.add(entry.getTitle());
                }
            } else {
                System.out.println(resp.getType().name());
            }
        }

        System.out.println("Starting at marker " + marker);

        long lastPrintout = System.nanoTime();

        while (shouldRun) {
            long now = System.nanoTime();
            if (now > lastPrintout + 1000000000) {
                System.out.printf("%s: %d\n", new Date(), seen.size());
                lastPrintout = now;
            }

            fetchOnce();
        }
    }

    public void fetchOnce() {
        String withMarker = url + "?marker=" + marker.toString();
        //System.out.println("Fetching " + withMarker);
        ClientResponse resp = client.get(withMarker, new RequestOptions(true));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            Document<Feed> doc = resp.getDocument();
            final List<Entry> entries = doc.getRoot().getEntries();
            if (entries.isEmpty()) {
                return;
            }
            marker = entries.get(0).getId();
            for (Entry entry : entries) {
                if (!entry.getTitle().startsWith(prefix)) {
                    continue;
                }
                seen.add(entry.getTitle());
            }
        } else {
            System.out.println(resp.getType().name());
        }
    }
}
