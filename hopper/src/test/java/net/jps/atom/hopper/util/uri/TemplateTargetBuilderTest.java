package net.jps.atom.hopper.util.uri;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class TemplateTargetBuilderTest {

    public static final Object ARCHIVE = new Object(), COLLECTION = new Object();

    public static class WhenGeneratingFeedTemplates {

        private TemplateTargetBuilder targetBuilder;
        private RequestContext requestContext;

        @Before
        public void standUp() {
            requestContext = mock(RequestContext.class);
            when(requestContext.getTargetBasePath()).thenReturn("/root/context");

            targetBuilder = new TemplateTargetBuilder();
        }

        @Test
        public void shouldGenerateFeedURLWithDefaults() {
            final URITemplateBuilder templateBuilder = new URITemplateBuilder("domain.com");
            templateBuilder.setWorkspaceResource("a");
            templateBuilder.setFeedResource("b");

            targetBuilder.setTemplate(TemplateTargetKey.FEED, templateBuilder.toFeedTemplate().toString());

            final String expected = "http://domain.com/root/context/a/b/";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, TemplateTargetKey.FEED, null));
        }

        @Test
        public void shouldGenerateFeedURLWithMarker() {
            final URITemplateBuilder templateBuilder = new URITemplateBuilder("domain.com");
            templateBuilder.setWorkspaceResource("a");
            templateBuilder.setFeedResource("b");

            targetBuilder.setTemplate(TemplateTargetKey.FEED, templateBuilder.toFeedTemplate().toString());

            final URITemplateParameters<TemplateTargetKey> params = new URITemplateParameters<TemplateTargetKey>(TemplateTargetKey.FEED);
            params.setMarker("12345");

            final String expected = "http://domain.com/root/context/a/b/?lochint=12345";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, params.getTargetTemplateKey(), params.getParameters()));
        }

        @Test
        public void shouldGenerateFeedURLWithLimitAndMarker() {
            final URITemplateBuilder templateBuilder = new URITemplateBuilder("domain.com");
            templateBuilder.setWorkspaceResource("a");
            templateBuilder.setFeedResource("b");

            targetBuilder.setTemplate(TemplateTargetKey.FEED, templateBuilder.toFeedTemplate().toString());

            final URITemplateParameters<TemplateTargetKey> params = new URITemplateParameters<TemplateTargetKey>(TemplateTargetKey.FEED);
            params.setMarker("12345");
            params.setLimit("5");

            final String expected = "http://domain.com/root/context/a/b/?lochint=12345&limit=5";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, params.getTargetTemplateKey(), params.getParameters()));
        }
    }

    public static class WhenBuildingTemplatesManually {

        private TemplateTargetBuilder targetBuilder;
        private RequestContext requestContext;

        @Before
        public void standUp() {
            requestContext = mock(RequestContext.class);
            when(requestContext.getTargetBasePath()).thenReturn("/root/context");

            targetBuilder = new TemplateTargetBuilder();
            targetBuilder.setTemplate(ARCHIVE, "{target_base}/{workspace=a}/{feed=b}{-prefix|/|entry}/{-opt|?|categories,marker,limit}{-opt|categories=|categories}{-listjoin|;|categories}{-opt|&|categories}{-join|&|marker,limit}");
        }

        @Test
        public void shouldGenerateExpectedURLWithDefaults() {
            final Map<String, String> parameterMap = new HashMap<String, String>();
            parameterMap.put("marker", "12345");
            parameterMap.put("limit", "5");

            final String expected = "/root/context/a/b/?marker=12345&limit=5";

            assertEquals("URL built from template should match expected", expected, targetBuilder.urlFor(requestContext, ARCHIVE, parameterMap));
        }

        @Test
        public void shouldGenerateExpectedURL() {
            final Map<String, String> parameterMap = new HashMap<String, String>();
            parameterMap.put("workspace", "a");
            parameterMap.put("feed", "b");
            parameterMap.put("marker", "12345");
            parameterMap.put("entry", "c");
            parameterMap.put("limit", "5");

            final String expected = "/root/context/a/b/c/?marker=12345&limit=5";

            assertEquals("URL built from template should match expected", expected, targetBuilder.urlFor(requestContext, ARCHIVE, parameterMap));
        }

        @Test
        public void shouldGenerateExpectedURLWithCategories() {
            final Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("categories", Arrays.asList(new String[]{"cata", "catb", "catc"}));
            parameterMap.put("workspace", "a");
            parameterMap.put("feed", "b");
            parameterMap.put("marker", "12345");
            parameterMap.put("entry", "c");
            parameterMap.put("limit", "5");

            final String expected = "/root/context/a/b/c/?categories=cata;catb;catc&marker=12345&limit=5";

            assertEquals("URL built from template should match expected", expected, targetBuilder.urlFor(requestContext, ARCHIVE, parameterMap));
        }
    }
}
