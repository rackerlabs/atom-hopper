package org.atomhopper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class PostInParallel {
    private final String url;
    private final String prefix;
    private final AtomicInteger numPosted;
    private final AtomicInteger numFailed;

    public static void main(String[] args) throws IOException, InterruptedException {
        final String url = args.length >= 1 ? args[0] : "http://localhost:8080/namespace/feed";
        final String prefix = args.length >= 2 ? args[1] : UUID.randomUUID().toString();
        final AtomicInteger numPosted = new AtomicInteger();
        final AtomicInteger numFailed = new AtomicInteger();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Total posted: " + numPosted.longValue());
                System.out.println("Total failed: " + numFailed.longValue());
            }
        }));


        final int numParallel = 10;

        new PostInParallel(url, prefix, numPosted, numFailed).run(numParallel, 10000);
    }

    public PostInParallel(String url, String prefix, AtomicInteger numPosted, AtomicInteger numFailed) {
        this.url = url;
        this.prefix = prefix;
        this.numPosted = numPosted;
        this.numFailed = numFailed;
    }

    public void run(int numParallel, int numBatches) throws InterruptedException {
        final CyclicBarrier barrier = new CyclicBarrier(numParallel);
        Thread threads[] = new Thread[numParallel];
        for (int i = 0; i < numParallel; i++) {
            final int threadid = i;
            threads[i] = new Thread(new PostEntries(threadid, barrier, numBatches), String.format("poster-%d", threadid));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private class PostEntries implements Runnable {
        private final int threadid;
        private final CyclicBarrier barrier;
        private final int numBatches;

        public PostEntries(int threadid, CyclicBarrier barrier, int numBatches) {
            this.threadid = threadid;
            this.barrier = barrier;
            this.numBatches = numBatches;
        }

        @Override
        public void run() {
            final HttpClient httpClient = new HttpClient();

            for (int batch = 0; batch < numBatches; batch++) {
                final String title = prefix + "-" + batch + "-" + threadid;
                final String body = "<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "<title>" + title + "</title>"
                    + "</entry>";

                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

                boolean posted = false;
                while (!posted) {
                    final PostMethod post = new PostMethod(url);
                    try {
                        post.setRequestEntity(new StringRequestEntity(body, "application/atom+xml", "ascii"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        final int result = httpClient.executeMethod(post);
                        if (result != 201) {
                            numFailed.incrementAndGet();
                            System.err.println("Failed to POST entry: " + body);
                            continue;
                        }
                    } catch (Exception e) {
                        numFailed.incrementAndGet();
                        System.err.println("Error when trying to POST entry: " + e);
                        e.printStackTrace();
                    }
                    posted = true;
                }
                int postnum = numPosted.incrementAndGet();
                if (postnum % 1000 == 0) {
                    System.out.printf("%s: %d\n", new Date(), postnum);
                }
            }

        }
    }
}
