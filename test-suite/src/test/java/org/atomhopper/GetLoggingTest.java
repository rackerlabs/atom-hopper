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
public class GetLoggingTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getLogTestMethod() {
        return new GetMethod(urlAndPort + "/logtest");
    }

    public static class WhenGettingVersionInfo {

        private final HttpMethod getLogTestMethod = getLogTestMethod();

        @Test
        public void shouldReturnHTTP200AndJSON() throws Exception {
            assertEquals("Getting the log test should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getLogTestMethod));
            assertTrue(new String(getLogTestMethod.getResponseBody()).contains("The following are messages entered into logging"));
            assertTrue(new String(getLogTestMethod.getResponseBody()).contains("Test entry for INFO logging"));
            assertTrue(new String(getLogTestMethod.getResponseBody()).contains("Test entry for WARN logging"));
            assertTrue(new String(getLogTestMethod.getResponseBody()).contains("Test entry for DEBUG logging"));
            assertTrue(new String(getLogTestMethod.getResponseBody()).contains("Test entry for ERROR logging"));
        }
    }
}