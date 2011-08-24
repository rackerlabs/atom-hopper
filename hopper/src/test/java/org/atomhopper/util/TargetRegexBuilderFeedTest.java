package net.jps.atom.hopper.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(Enclosed.class)
public class TargetRegexBuilderFeedTest {

    public static class WhenBuildingFeedRegexes extends TargetRegexBuilderTestParent {

        @Test
        public void shouldMatchAllFeedVariations() {
            final TargetRegexBuilder target = feedRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toFeedPattern());

            assertTrue("Should match plain feed URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(FEED).matches());
            assertTrue("Should match plain feed URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(FEED)).matches());
            assertTrue("Should match feed URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(FEED, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match feed URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(FEED, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match feed URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(FEED, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match feed URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(FEED, DEFAULT_CATEGORIES_LONG))).matches());
        }

        @Test
        public void shouldMatchWithNonRootContextPath() {
            final TargetRegexBuilder target = feedRegexBuilder();
            target.setContextPath(CONTEXT_PATH);

            final Pattern targetRegex = Pattern.compile(target.toFeedPattern());

            assertTrue("Should match feed URI with a context root - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addContextRoot(FEED)).matches());
        }

        @Test(expected = IllegalStateException.class)
        public void shouldFailToBuildRegexWhenFeedIsNotSet() {
            new TargetRegexBuilder().toFeedPattern();
        }
    }

    public static class WhenBuildingCategoryRegexes extends TargetRegexBuilderTestParent {

        @Test
        public void shouldMatchAllCategoryVariations() {
            final TargetRegexBuilder target = feedRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toCategoriesPattern());

            assertTrue("Should match plain categories URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(CATEGORIES).matches());
            assertTrue("Should match plain categories URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(CATEGORIES)).matches());
        }

        @Test
        public void shouldMatchWithNonRootContextPath() {
            final TargetRegexBuilder target = feedRegexBuilder();
            target.setContextPath(CONTEXT_PATH);

            final Pattern targetRegex = Pattern.compile(target.toCategoriesPattern());

            assertTrue("Should match categories URI with a context root - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addContextRoot(CATEGORIES)).matches());
        }
    }

}
