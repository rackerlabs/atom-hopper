package org.atomhopper.abdera.filter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.atomhopper.JettyIntegrationTestHarness;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * User: shin4590
 * Date: 7/3/14
 */
@RunWith(Enclosed.class)
public class SelectiveURIJSONFilterTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static class WhenGettingFeedsWithFormatJsonWhereJsonIsAllowed {

        @Test
        public void shouldReturnOK() throws Exception {
            final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace2/feed2/?format=json");
            httpClient.executeMethod(getFeedMethod);
            assertEquals("Response code: ", 200, getFeedMethod.getStatusCode());
        }
    }

    public static class WhenGettingFeedsWithFormatJsonWhereJsonIsNotAllowed {

        @Test
        public void shouldReturnOK() throws Exception {
            final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace6/feed6/?format=json");
            httpClient.executeMethod(getFeedMethod);
            assertEquals("Response code: ", 400, getFeedMethod.getStatusCode());
        }
    }
}
