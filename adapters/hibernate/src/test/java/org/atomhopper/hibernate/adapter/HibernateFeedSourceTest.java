package org.atomhopper.hibernate.adapter;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


@RunWith(Enclosed.class)
public class HibernateFeedSourceTest {

    public static class WhenCallingNonImplementedFunctionality {

        private HibernateFeedSource hibernateFeedSource;

        @Before
        public void setUp() throws Exception {
            hibernateFeedSource = new HibernateFeedSource();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            hibernateFeedSource.setParameters(map);
        }

        // TODO: Finish the tests
    }
}