package net.jps.atom.hopper.util;

import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * TODO: Execute these tests when we implement the feature to query for archive markers based on date
 */
@RunWith(Enclosed.class)
public class TargetRegexBuilderArchiveQueryTest {

    public static class WhenBuildingArchiveRegexes extends TargetRegexBuilderTestParent {

        @Ignore
        public void shouldMatchAllArchiveVariations() {
            final TargetRegexBuilder target = feedRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toArchivesPattern());

            assertTrue("Should match plain year scoped archive URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_YEAR).matches());
            assertTrue("Should match plain year scoped archive URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_YEAR)).matches());
            assertTrue("Should match year scoped archive URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_YEAR, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match year scoped archive URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_YEAR, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match year scoped archive URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_YEAR, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match year scoped archive URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_YEAR, DEFAULT_CATEGORIES_LONG))).matches());

            assertTrue("Should match plain month scoped archive URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_MONTH).matches());
            assertTrue("Should match plain month scoped archive URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_MONTH)).matches());
            assertTrue("Should match month scoped archive URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MONTH, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match month scoped archive URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MONTH, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match month scoped archive URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MONTH, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match month scoped archive URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MONTH, DEFAULT_CATEGORIES_LONG))).matches());

            assertTrue("Should match plain day scoped archive URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_DAY).matches());
            assertTrue("Should match plain day scoped archive URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_DAY)).matches());
            assertTrue("Should match day scoped archive URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_DAY, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match day scoped archive URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_DAY, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match day scoped archive URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_DAY, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match day scoped archive URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_DAY, DEFAULT_CATEGORIES_LONG))).matches());

            assertTrue("Should match plain hour scoped archive URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_HOUR).matches());
            assertTrue("Should match plain hour scoped archive URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_HOUR)).matches());
            assertTrue("Should match hour scoped archive URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_HOUR, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match hour scoped archive URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_HOUR, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match hour scoped archive URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_HOUR, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match hour scoped archive URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_HOUR, DEFAULT_CATEGORIES_LONG))).matches());

            assertTrue("Should match plain minute scoped archive URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_MINUTE).matches());
            assertTrue("Should match plain minute scoped archive URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_MINUTE)).matches());
            assertTrue("Should match minute scoped archive URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MINUTE, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match minute scoped archive URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MINUTE, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match minute scoped archive URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MINUTE, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match minute scoped archive URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MINUTE, DEFAULT_CATEGORIES_LONG))).matches());
        }

        @Ignore
        public void shouldMatchWithNonRootContextPath() {
            final TargetRegexBuilder target = feedRegexBuilder();
            target.setContextPath(CONTEXT_PATH);

            final Pattern targetRegex = Pattern.compile(target.toArchivesPattern());

            assertTrue("Should match archive URI with a context root - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addContextRoot(ARCHIVE_MINUTE)).matches());
        }
    }

    public static final String
            ARCHIVE_YEAR = "/workspace/feed/archives/2011",
            ARCHIVE_MONTH = "/workspace/feed/archives/2011/01",
            ARCHIVE_DAY = "/workspace/feed/archives/2011/01/01",
            ARCHIVE_HOUR = "/workspace/feed/archives/2011/01/01/01:00",
            ARCHIVE_MINUTE = "/workspace/feed/archives/2011/01/01/01:35";

}
