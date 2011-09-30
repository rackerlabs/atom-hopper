package org.atomhopper;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * User: sbrayman
 * Date: Sep 29, 2011
 *
 * Assert not equals extension to junit.
 */

public final class TestHelper {

    private TestHelper() {}

    public static void assertNotEquals(Object shouldNotBe, Object actual) {
        assertNotEquals(null, shouldNotBe, actual);
    }

    public static void assertNotEquals(String message, Object shouldNotBe, Object actual) {

        String formatted = "";

        if (message != null) {
            formatted = message + " ";
        }

        boolean isEqual;

        try {
            assertEquals(shouldNotBe, actual);
            isEqual = true;
        } catch (Throwable valuesNotEqual) {
            return;
        }

        if (isEqual) {
            fail(formatted + "expected:<" + shouldNotBe + "> equaled:<" + actual + ">");
        }
    }
}
