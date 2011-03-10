package net.jps.atom.hopper;

import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class GetFeedIntegrationTest extends JettyIntegrationTestHarness {

    public static class WhenGettingFeeds {

        final HttpClient c = new HttpClient();

        @Test
        public void shouldReturnEmptyFeed() throws Exception {
            final GetMethod method = new GetMethod("http://localhost:" + getPort() + "/namespace/feed");
            assertEquals("Getting a feed should return a 200", HttpStatusCode.OK.intValue(), c.executeMethod(method));
        }
    }
}
