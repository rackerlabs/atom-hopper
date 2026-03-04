package org.atomhopper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class GetVersionPathTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getVersionPathMethod() {
        return new GetMethod(urlAndPort + "/buildinfo");
    }

    public static class WhenGettingVersionInfo {
        
        private final HttpMethod getVersionMethod = getVersionPathMethod();

        @Test
        public void shouldReturnHTTP200AndBuildInfo() throws Exception {
            assertEquals("Getting the version should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getVersionMethod));
            // Should return structured build info JSON
            String responseBody = new String(getVersionMethod.getResponseBody());
            assertTrue("Response should contain service field", responseBody.contains("\"service\":\"atomhopper\""));
            assertTrue("Response should contain status field", responseBody.contains("\"status\":\"healthy\""));
            assertTrue("Response should contain version field", responseBody.contains("\"version\""));
        }
    }
}
