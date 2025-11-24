package org.atomhopper.abdera;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;
import org.atomhopper.config.v1_0.HostConfiguration;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.atomhopper.util.uri.template.URITemplateParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkspaceProviderUrlGenerationTest {

    private WorkspaceProvider workspaceProvider;
    private RequestContext mockRequestContext;
    private HostConfiguration hostConfiguration;
    private Target mockTarget;

    @Before
    public void setup() {
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setDomain("fallback.example.com");
        hostConfiguration.setScheme("http");
        
        workspaceProvider = new WorkspaceProvider(hostConfiguration);
        mockRequestContext = mock(RequestContext.class);
        mockTarget = mock(Target.class);
        
        // Mock basic request context behavior
        when(mockRequestContext.getTarget()).thenReturn(mockTarget);
        when(mockRequestContext.getUri()).thenReturn(new IRI("http://test.example.com/path"));
        when(mockRequestContext.getTargetBasePath()).thenReturn("/root/context");
        
        // Mock target behavior
        when(mockTarget.getType()).thenReturn(TargetType.TYPE_COLLECTION);
        when(mockTarget.getParameter("workspace")).thenReturn("testworkspace");
        when(mockTarget.getParameter("feed")).thenReturn("testfeed");
    }

    @After
    public void cleanup() {
        // Clear environment variables after each test
        System.clearProperty("AH_DOMAIN_MODE");
        System.clearProperty("AH_EXTERNAL_DOMAIN");
        System.clearProperty("AH_EXTERNAL_SCHEME");
    }

    @Test
    public void shouldUseFallbackDomainWhenModeIsExternal() {
        // Set environment for external mode
        System.setProperty("AH_DOMAIN_MODE", "external");
        
        EnumKeyedTemplateParameters<URITemplate> params = new EnumKeyedTemplateParameters<>(URITemplate.FEED);
        params.set(URITemplateParameter.WORKSPACE_RESOURCE, "testworkspace");
        params.set(URITemplateParameter.FEED_RESOURCE, "testfeed");
        
        String url = workspaceProvider.urlFor(mockRequestContext, URITemplate.FEED, params);
        
        assertTrue("URL should contain fallback domain", url.contains("fallback.example.com"));
        assertTrue("URL should use fallback scheme", url.startsWith("http://"));
    }

    @Test
    public void shouldUseRequestHostHeaderWhenModeIsRequestBased() {
        // Set environment for request-based mode
        System.setProperty("AH_DOMAIN_MODE", "request-based");
        System.setProperty("AH_EXTERNAL_DOMAIN", "external.example.com");
        System.setProperty("AH_EXTERNAL_SCHEME", "https");
        
        // Mock Host header
        when(mockRequestContext.getHeader("Host")).thenReturn("internal.cloudfeeds.local:8080");
        
        EnumKeyedTemplateParameters<URITemplate> params = new EnumKeyedTemplateParameters<>(URITemplate.FEED);
        params.set(URITemplateParameter.WORKSPACE_RESOURCE, "testworkspace");
        params.set(URITemplateParameter.FEED_RESOURCE, "testfeed");
        
        String url = workspaceProvider.urlFor(mockRequestContext, URITemplate.FEED, params);
        
        assertTrue("URL should contain request Host header domain", url.contains("internal.cloudfeeds.local") && (url.contains(":8080") || url.contains("%3A8080")));
    }

    @Test
    public void shouldUseXForwardedProtoHeaderForScheme() {
        // Set environment for request-based mode
        System.setProperty("AH_DOMAIN_MODE", "request-based");
        System.setProperty("AH_EXTERNAL_DOMAIN", "external.example.com");
        System.setProperty("AH_EXTERNAL_SCHEME", "http");
        
        // Mock headers
        when(mockRequestContext.getHeader("Host")).thenReturn("api.example.com");
        when(mockRequestContext.getHeader("X-Forwarded-Proto")).thenReturn("https");
        
        EnumKeyedTemplateParameters<URITemplate> params = new EnumKeyedTemplateParameters<>(URITemplate.FEED);
        params.set(URITemplateParameter.WORKSPACE_RESOURCE, "testworkspace");
        params.set(URITemplateParameter.FEED_RESOURCE, "testfeed");
        
        String url = workspaceProvider.urlFor(mockRequestContext, URITemplate.FEED, params);
        
        assertTrue("URL should use X-Forwarded-Proto scheme", url.startsWith("https://"));
        assertTrue("URL should contain request Host header domain", url.contains("api.example.com"));
    }

    @Test
    public void shouldFallbackToExternalDomainWhenHostHeaderMissing() {
        // Set environment for request-based mode
        System.setProperty("AH_DOMAIN_MODE", "request-based");
        System.setProperty("AH_EXTERNAL_DOMAIN", "external.example.com");
        System.setProperty("AH_EXTERNAL_SCHEME", "https");
        
        // No Host header
        when(mockRequestContext.getHeader("Host")).thenReturn(null);
        
        EnumKeyedTemplateParameters<URITemplate> params = new EnumKeyedTemplateParameters<>(URITemplate.FEED);
        params.set(URITemplateParameter.WORKSPACE_RESOURCE, "testworkspace");
        params.set(URITemplateParameter.FEED_RESOURCE, "testfeed");
        
        String url = workspaceProvider.urlFor(mockRequestContext, URITemplate.FEED, params);
        
        assertTrue("URL should contain external domain fallback", url.contains("external.example.com"));
        assertTrue("URL should use external scheme fallback", url.startsWith("https://"));
    }

    @Test
    public void shouldUseRequestUriScheme() {
        // Set environment for request-based mode
        System.setProperty("AH_DOMAIN_MODE", "request-based");
        System.setProperty("AH_EXTERNAL_DOMAIN", "external.example.com");
        System.setProperty("AH_EXTERNAL_SCHEME", "http");
        
        // Mock request with HTTPS URI
        when(mockRequestContext.getHeader("Host")).thenReturn("secure.example.com");
        when(mockRequestContext.getHeader("X-Forwarded-Proto")).thenReturn(null);
        when(mockRequestContext.getUri()).thenReturn(new IRI("https://secure.example.com/path"));
        
        EnumKeyedTemplateParameters<URITemplate> params = new EnumKeyedTemplateParameters<>(URITemplate.FEED);
        params.set(URITemplateParameter.WORKSPACE_RESOURCE, "testworkspace");
        params.set(URITemplateParameter.FEED_RESOURCE, "testfeed");
        
        String url = workspaceProvider.urlFor(mockRequestContext, URITemplate.FEED, params);
        
        assertTrue("URL should use https from request URI", url.startsWith("https://"));
        assertTrue("URL should contain request Host header domain", url.contains("secure.example.com"));
    }
}