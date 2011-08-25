package org.atomhopper.util.uri.template;

import org.atomhopper.util.uri.template.URITemplateParameter;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.atomhopper.util.uri.template.TemplateTargetKey;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class TemplateTargetBuilderTest {

    public static final Object ARCHIVE = new Object(), COLLECTION = new Object();

    public static class WhenGeneratingURLsFromFeedTemplates extends TestParent {

        @Test
        public void shouldGenerateFeedURLWithDefaults() {
            targetBuilder.setTemplate(TemplateTargetKey.FEED, URITemplate.FEED.toString());

            final EnumKeyedTemplateParameters<TemplateTargetKey> params = new EnumKeyedTemplateParameters<TemplateTargetKey>(TemplateTargetKey.FEED);
            params.set(URITemplateParameter.HOST_DOMAIN, "domain.com");
            params.set(URITemplateParameter.WORKSPACE_RESOURCE, "a");
            params.set(URITemplateParameter.FEED_RESOURCE, "b");

            final String expected = "http://domain.com/root/context/a/b/";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, TemplateTargetKey.FEED, params.toMap()));
        }

        @Test
        public void shouldGenerateFeedURLWithMarker() {
            targetBuilder.setTemplate(TemplateTargetKey.FEED, URITemplate.FEED.toString());

            final EnumKeyedTemplateParameters<TemplateTargetKey> params = new EnumKeyedTemplateParameters<TemplateTargetKey>(TemplateTargetKey.FEED);
            params.set(URITemplateParameter.HOST_DOMAIN, "domain.com");
            params.set(URITemplateParameter.WORKSPACE_RESOURCE, "a");
            params.set(URITemplateParameter.FEED_RESOURCE, "b");
            params.set(URITemplateParameter.MARKER, "12345");

            final String expected = "http://domain.com/root/context/a/b/?lochint=12345";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, params.getTargetTemplateKey(), params.toMap()));
        }

        @Test
        public void shouldGenerateFeedURLWithLimitAndMarker() {
            targetBuilder.setTemplate(TemplateTargetKey.FEED, URITemplate.FEED.toString());

            final EnumKeyedTemplateParameters<TemplateTargetKey> params = new EnumKeyedTemplateParameters<TemplateTargetKey>(TemplateTargetKey.FEED);
            params.set(URITemplateParameter.HOST_DOMAIN, "domain.com");
            params.set(URITemplateParameter.WORKSPACE_RESOURCE, "a");
            params.set(URITemplateParameter.FEED_RESOURCE, "b");
            params.set(URITemplateParameter.MARKER, "12345");
            params.set(URITemplateParameter.PAGE_LIMIT, "5");

            final String expected = "http://domain.com/root/context/a/b/?lochint=12345&limit=5";

            assertEquals("URL built from template should match expected feed URL",
                    expected, targetBuilder.urlFor(requestContext, params.getTargetTemplateKey(), params.toMap()));
        }
    }

    public static class WhenBuildingTemplatesManually extends TestParent {

        @Before
        public void setArchiveTemplate() {
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

    @Ignore
    public static class TestParent {

        protected TemplateTargetBuilder targetBuilder;
        protected RequestContext requestContext;

        @Before
        public void standUp() {
            requestContext = mock(RequestContext.class);
            when(requestContext.getTargetBasePath()).thenReturn("/root/context");

            targetBuilder = new TemplateTargetBuilder();
        }
    }
}
