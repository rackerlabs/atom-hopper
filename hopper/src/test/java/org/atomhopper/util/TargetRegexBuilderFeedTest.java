package org.atomhopper.util;

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

}
