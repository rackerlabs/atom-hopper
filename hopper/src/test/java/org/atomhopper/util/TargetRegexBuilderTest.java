package org.atomhopper.util;

import static org.junit.Assert.assertTrue;
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

    public static class WhenResourceAttributeHasBackslashesInRegex {

        private TargetRegexBuilder targetRegexBuilder;

        @Test
        public void shouldRetainTheBackslashInRegex() {
            String workspaceResource = "usagetest\\d{1,2}";
            String feedResource = "events";
            String contextPath = "";

            targetRegexBuilder = new TargetRegexBuilder();
            targetRegexBuilder.setContextPath(contextPath);
            targetRegexBuilder.setWorkspace(workspaceResource);
            targetRegexBuilder.setFeed(feedResource);

            assertTrue("should contain the workspaceResource regex",targetRegexBuilder.toEntryPattern().contains(workspaceResource));

        }
    }
}
