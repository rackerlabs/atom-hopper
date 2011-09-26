package org.atomhopper.hibernate.adapter;

import org.atomhopper.dbal.FeedRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * User: sbrayman
 * Date: Sep 26, 2011
 */

@RunWith(Enclosed.class)
public class HibernateFeedInformationTest {

    public static class WhenGettingHibernateFeedInformation {

        private FeedRepository feedRepository;
        private HibernateFeedInformation hibernateFeedInformation;

        @Before
        public void setUp() throws Exception {
            feedRepository = mock(FeedRepository.class);
            hibernateFeedInformation = new HibernateFeedInformation(feedRepository);
        }

        @Test
        public void shouldCreateHibernateFeedInformation() {
            assertNotNull(hibernateFeedInformation);
        }
    }
}
