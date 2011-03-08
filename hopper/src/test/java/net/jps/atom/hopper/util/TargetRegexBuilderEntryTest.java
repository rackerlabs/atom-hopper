package net.jps.atom.hopper.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * @author zinic
 */
@RunWith(Enclosed.class)
public class TargetRegexBuilderEntryTest {

    public static class WhenBuildingEntryRegexes extends TargetRegexBuilderTestParent {

        @Test
        public void shouldMatchAllEntryVariations() {
            final TargetRegexBuilder target = feedRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toEntryPattern());

            assertTrue("Should match plain entry URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ENTRY).matches());
            assertTrue("Should match plain entry URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ENTRY)).matches());
            assertTrue("Should match entry URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ENTRY, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match entry URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ENTRY, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match entry URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ENTRY, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match entry URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ENTRY, DEFAULT_CATEGORIES_LONG))).matches());
        }

        @Test
        public void shouldMatchWithNonRootContextPath() {
            final TargetRegexBuilder target = feedRegexBuilder();
            target.setContextPath(CONTEXT_PATH);

            final Pattern targetRegex = Pattern.compile(target.toEntryPattern());

            assertTrue("Should match entry URI with a context root - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addContextRoot(ENTRY)).matches());
        }
    }

}
