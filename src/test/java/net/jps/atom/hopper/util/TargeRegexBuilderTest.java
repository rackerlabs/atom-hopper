/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util;

import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author zinic
 */
@RunWith(Enclosed.class)
public class TargeRegexBuilderTest {

    public static class WhenBuildingWorkspaceRegexes {

        @Test
        public void shouldMatchAllWorkspaceVariations() {
            final TargetRegexBuilder target = new TargetRegexBuilder();
            target.setWorkspace("workspace");

            final Pattern targetRegex = target.toWorkspacePattern();
            
            assertTrue("Should match plain workspace URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(WORKSPACE).matches());
            assertTrue("Should match plain workspace URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(WORKSPACE)).matches());
        }
    }

    public static class WhenBuildingFeedRegexes {

        @Test
        public void shouldMatchAllFeedVariations() {
            final TargetRegexBuilder target = new TargetRegexBuilder();
            target.setWorkspace("workspace");
            target.setFeed("feed");

            final Pattern targetRegex = target.toFeedPattern();

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
    }

    public static class WhenBuildingEntryRegexes {

        @Test
        public void shouldMatchAllEntryVariations() {
            final TargetRegexBuilder target = new TargetRegexBuilder();
            target.setWorkspace("workspace");
            target.setFeed("feed");

            final Pattern targetRegex = target.toEntryPattern();

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
    }

    public static class WhenBuildingArchiveRegexes {

        @Test
        public void shouldMatchAllArchiveVariations() {
            final TargetRegexBuilder target = new TargetRegexBuilder();
            target.setWorkspace("workspace");
            target.setFeed("feed");

            final Pattern targetRegex = target.toArchivePattern();

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
    }
    public static final String[] DEFAULT_CATEGORIES_SHORT = new String[]{"category_1", "category_2"},
            DEFAULT_CATEGORIES_LONG = new String[]{"category_a", "category_b", "category_c", "category_d", "category_e"};
    
    public static final String WORKSPACE = "/workspace",
            CATEGORIES = "/workspace/categories",
            FEED = "/workspace/feed",
            ENTRY = "/workspace/feed/entries/tag:domain.com,2011-01-01:entry-id",
            ARCHIVE_YEAR = "/workspace/feed/archives/2011",
            ARCHIVE_MONTH = "/workspace/feed/archives/2011/01",
            ARCHIVE_DAY = "/workspace/feed/archives/2011/01/01",
            ARCHIVE_HOUR = "/workspace/feed/archives/2011/01/01/01",
            ARCHIVE_MINUTE = "/workspace/feed/archives/2011/01/01/01:30";

    public static String withCategories(String base, String[] categories) {
        final StringBuilder uri = new StringBuilder(base);
        uri.append("?categories=");

        if (categories.length >= 1) {
            uri.append(categories[0]);

            for (int i = 1; i < categories.length; i++) {
                uri.append(";").append(categories[i]);
            }
        }

        return uri.toString();
    }

    public static String addTrailingSlash(String base) {
        return base + "/";
    }
}
