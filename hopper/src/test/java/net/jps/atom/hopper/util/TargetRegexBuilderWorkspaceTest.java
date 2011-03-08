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
public class TargetRegexBuilderWorkspaceTest {

    public static class WhenBuildingWorkspaceRegexes extends TargetRegexBuilderTestParent {

        @Test
        public void shouldMatchAllWorkspaceVariations() {
            final TargetRegexBuilder target = workspaceRegexBuilder();
            final Pattern targetRegex = Pattern.compile(target.toWorkspacePattern());

            assertTrue("Should match plain workspace URI - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(WORKSPACE).matches());
            assertTrue("Should match plain workspace URI with a slash - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addTrailingSlash(WORKSPACE)).matches());
        }

        @Test
        public void shouldMatchWithNonRootContextPath() {
            final TargetRegexBuilder target = workspaceRegexBuilder();
            target.setContextPath(CONTEXT_PATH);

            final Pattern targetRegex = Pattern.compile(target.toWorkspacePattern());

            assertTrue("Should match workspace URI with a context root - regex is: " + targetRegex.pattern(),
                    targetRegex.matcher(addContextRoot(WORKSPACE)).matches());
        }

        @Test(expected = IllegalStateException.class)
        public void shouldFailToBuildRegexWhenWorkspaceIsNotSet() {
            new TargetRegexBuilder().toWorkspacePattern();
        }
    }

}
