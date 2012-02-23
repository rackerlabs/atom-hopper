package org.atomhopper.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(Enclosed.class)
public class TargetRegexBuilderTest {

    public static class WhenCopyingTargetRegexBuilders extends TargetRegexBuilderTestParent {

        @Test
        public void shouldCopyBuilderElements() {
            final TargetRegexBuilder expected = feedRegexBuilder();
            final TargetRegexBuilder actual = new TargetRegexBuilder(expected);

            assertEquals("TargetRegexBuilder copy must populate the context path", expected.getContextPath(), actual.getContextPath());
            assertEquals("TargetRegexBuilder copy must populate the workspace path", expected.getWorkspaceResource(), actual.getWorkspaceResource());
            assertEquals("TargetRegexBuilder copy must populate the feed path", expected.getFeedResource(), actual.getFeedResource());
        }
    }
}
