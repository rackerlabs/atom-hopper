package net.jps.atom.hopper.abdera;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(Enclosed.class)
public class TargetAwareAbstractCollectionAdapterTest {

    public static class WhenEvaluatingTargetUriAgainstRegexes {
        TestableTargetAwareAbstractCollectionAdapter targetAwareAdapter;

        @Before
        public void setup() {
            targetAwareAdapter = new TestableTargetAwareAbstractCollectionAdapter();
        }

        @Test
        public void shouldReturnTrueIfRegexMatchesTarget() {
            targetAwareAdapter.addTargetRegex(".*(atom).*");
            assertEquals("Should match targetUri with regex", true, targetAwareAdapter.canHandleTarget("myatomfeed"));
        }

        @Test
        public void shouldReturnTrueWhenOneRegexOfManyMatchesTarget() {
            targetAwareAdapter.addTargetRegex(".*(hisatomfeed)?.*");
            targetAwareAdapter.addTargetRegex(".*(atom).*");
            assertEquals("Should match targetUri with regex", true, targetAwareAdapter.canHandleTarget("myatomfeed"));
        }

        @Test
        public void shouldReturnFalseIfNoRegexes() {
            assertEquals("Should not match when no regexes set", false, targetAwareAdapter.canHandleTarget("myatomfeed"));
        }

        @Test
        public void shouldReturnFalseIfNoMatchingRegexes() {
            targetAwareAdapter.addTargetRegex("foo");
            targetAwareAdapter.addTargetRegex("bar");
            assertEquals("Should not match against regexes", false, targetAwareAdapter.canHandleTarget("myatomfeed"));
        }

    }

    private static class TestableTargetAwareAbstractCollectionAdapter extends TargetAwareAbstractCollectionAdapter {
        public TestableTargetAwareAbstractCollectionAdapter() {
            super();
        }

        @Override
        public String getAuthor(RequestContext request) throws ResponseContextException {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public String getId(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public ResponseContext postEntry(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public ResponseContext deleteEntry(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public ResponseContext getEntry(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public ResponseContext putEntry(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public ResponseContext getFeed(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }

        @Override
        public String getTitle(RequestContext request) {
            throw new UnsupportedOperationException("not supported in test");
        }
    }
}
