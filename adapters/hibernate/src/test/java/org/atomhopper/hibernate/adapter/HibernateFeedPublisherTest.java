package org.atomhopper.hibernate.adapter;

import java.util.HashMap;
import java.util.Map;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;


@RunWith(Enclosed.class)
public class HibernateFeedPublisherTest {

    public static class WhenCallingNonImplementedFunctionality {

        private HibernateFeedPublisher hibernateFeedPublisher;
        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;

        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);

            hibernateFeedPublisher = new HibernateFeedPublisher();
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            hibernateFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            hibernateFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            hibernateFeedPublisher.setParameters(map);
        }
    }
}
