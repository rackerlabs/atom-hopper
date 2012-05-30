package org.atomhopper.adapter.impl;

import static junit.framework.Assert.assertEquals;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;

/**
 * User: sbrayman
 * Date: Sep 26, 2011
 */

@RunWith(Enclosed.class)
public class DisabledFeedInformationTest {

    public static class WhenAccessingDisabledFeedInformation {

        private DisabledFeedInformation disabledFeedInformation;
        private FeedRequest feedRequest;
        private String noId;

        @Before
        public void setUp() throws Exception {
            disabledFeedInformation = DisabledFeedInformation.getInstance();
            feedRequest = mock(FeedRequest.class);
            noId = "atomhopper:no-id";
        }

        @Test
        public void shouldNotReturnId() throws Exception {
            assertEquals("Get Id should return " + noId, disabledFeedInformation.getId(feedRequest), noId);
        }
    }
}
