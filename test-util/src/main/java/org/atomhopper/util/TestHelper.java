package org.atomhopper.util;

import org.junit.Assert;

/**
 * User: sbrayman
 * Date: Sep 29, 2011
 * <p/>
 * Assert not equals extension to junit.
 */

public final class TestHelper {

    private TestHelper() {
    }

    public static void assertNotEquals(Object shouldNotBe, Object actual) {
        assertNotEquals(null, shouldNotBe, actual);
    }

    public static void assertNotEquals(String message, Object shouldNotBe, Object actual) {

        String formatted = "";

        if (message != null) {
            formatted = message + " ";
        }

        try {
            Assert.assertEquals(shouldNotBe, actual);
        } catch (Throwable valuesNotEqual) {
            return;
        }

        Assert.fail(formatted + "expected:<" + shouldNotBe + "> equaled:<" + actual + ">");
    }
}
