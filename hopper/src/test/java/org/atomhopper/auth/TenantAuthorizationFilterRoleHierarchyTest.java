package org.atomhopper.auth;

import org.apache.abdera.protocol.server.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test strict role validation for conflicting user permissions
 */
public class TenantAuthorizationFilterRoleHierarchyTest {

    private TenantAuthorizationFilter filter;

    @Mock
    private RequestContext mockRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        filter = new TenantAuthorizationFilter();
    }

    @Test
    public void testConflictingRolesReturnConflict() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        roles.add("observer");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("CONFLICT", effectiveRole);
    }

    @Test
    public void testServiceAdminWithOtherRolesReturnsConflict() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("service-admin");
        roles.add("observer");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("CONFLICT", effectiveRole);
    }

    @Test
    public void testSingleServiceRoleReturnsServiceAdmin() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("service-admin");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("service-admin", effectiveRole);
    }

    @Test
    public void testSingleAdminRoleReturnsAdmin() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("admin");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("admin", effectiveRole);
    }

    @Test
    public void testSingleObserverRoleReturnsObserver() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("observer");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("observer", effectiveRole);
    }

    @Test
    public void testCaseInsensitiveRoleMatching() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("OBSERVER");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("observer", effectiveRole);
    }

    @Test
    public void testNoValidRolesReturnsNull() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("invalid-role");

        String effectiveRole = getEffectiveRole(roles);
        assertNull(effectiveRole);
    }

    @Test
    public void testEmptyRolesReturnsNull() throws Exception {
        Set<String> roles = new HashSet<>();
        String effectiveRole = getEffectiveRole(roles);
        assertNull(effectiveRole);
    }

    @Test
    public void testAllThreeRolesReturnConflict() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("service-admin");
        roles.add("admin");
        roles.add("observer");

        String effectiveRole = getEffectiveRole(roles);
        assertEquals("CONFLICT", effectiveRole);
    }

    // Helper method to access private getEffectiveRole method via reflection
    private String getEffectiveRole(Set<String> roles) throws Exception {
        Method method = TenantAuthorizationFilter.class.getDeclaredMethod("getEffectiveRole", Set.class);
        method.setAccessible(true);
        return (String) method.invoke(filter, roles);
    }
}