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
public class TargetRegexBuilderArchiveTest {

    public static class WhenBuildingArchiveRegexes extends TargetRegexBuilderTestParent {

        @Test
        public void shouldMatchAllFeedVariations() {
            final TargetRegexBuilder target = feedRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toArchivesPattern());

            assertTrue("Should match archive feed URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE).matches());
            assertTrue("Should match archive feed URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE)).matches());
            assertTrue("Should match archive feed URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match archive feed URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match archive feed URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match archive feed URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE, DEFAULT_CATEGORIES_LONG))).matches());


            assertTrue("Should match archive marker feed URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(ARCHIVE_MARKER).matches());
            assertTrue("Should match archive marker feed URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(ARCHIVE_MARKER)).matches());
            assertTrue("Should match archive marker feed URI with a short category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MARKER, DEFAULT_CATEGORIES_SHORT)).matches());
            assertTrue("Should match archive marker feed URI with a long category list - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(withCategories(ARCHIVE_MARKER, DEFAULT_CATEGORIES_LONG)).matches());
            assertTrue("Should match archive marker feed URI with a short category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MARKER, DEFAULT_CATEGORIES_SHORT))).matches());
            assertTrue("Should match archive marker feed URI with a long category list and a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(withCategories(ARCHIVE_MARKER, DEFAULT_CATEGORIES_LONG))).matches());


        }
    }


}
