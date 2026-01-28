package org.atomhopper.auth;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Test role conflict detection logic
 */
public class RoleConflictFilterTest {

    private RoleConflictFilter filter;

    @Before
    public void setUp() {
        filter = new RoleConflictFilter();
    }

    @Test
    public void testConflictingAdminObserverRoles() throws Exception {
        assertTrue(hasRoleConflicts("admin,observer"));
    }

    @Test
    public void testConflictingServiceAdminRoles() throws Exception {
        assertTrue(hasRoleConflicts("service-admin,observer"));
    }

    @Test
    public void testConflictingPublisherRoles() throws Exception {
        assertTrue(hasRoleConflicts("cadf-publisher,admin"));
    }

    @Test
    public void testConflictingCustomPublisherRoles() throws Exception {
        assertTrue(hasRoleConflicts("custom-publisher,observer"));
    }

    @Test
    public void testSingleServiceRole() throws Exception {
        assertFalse(hasRoleConflicts("service-admin"));
    }

    @Test
    public void testSingleAdminRole() throws Exception {
        assertFalse(hasRoleConflicts("admin"));
    }

    @Test
    public void testSingleObserverRole() throws Exception {
        assertFalse(hasRoleConflicts("observer"));
    }

    @Test
    public void testSingleCadfPublisherRole() throws Exception {
        assertFalse(hasRoleConflicts("cadf-publisher"));
    }

    @Test
    public void testSingleCustomPublisherRole() throws Exception {
        assertFalse(hasRoleConflicts("custom-publisher"));
    }

    @Test
    public void testCaseInsensitiveRoleMatching() throws Exception {
        assertTrue(hasRoleConflicts("ADMIN,OBSERVER"));
    }

    @Test
    public void testNoValidRoles() throws Exception {
        assertFalse(hasRoleConflicts("invalid-role"));
    }

    @Test
    public void testEmptyRoles() throws Exception {
        assertFalse(hasRoleConflicts(""));
        assertFalse(hasRoleConflicts(null));
    }

    @Test
    public void testMultipleConflictingRoles() throws Exception {
        assertTrue(hasRoleConflicts("service-admin,admin,observer,cadf-publisher"));
    }

    @Test
    public void testWhitespaceHandling() throws Exception {
        assertTrue(hasRoleConflicts(" admin , observer "));
    }

    private boolean hasRoleConflicts(String roles) throws Exception {
        Method method = RoleConflictFilter.class.getDeclaredMethod("hasRoleConflicts", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(filter, roles);
    }
}