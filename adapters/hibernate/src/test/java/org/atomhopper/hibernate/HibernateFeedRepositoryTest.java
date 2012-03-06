package org.atomhopper.hibernate;

import org.atomhopper.dbal.AtomDatabaseException;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * User: sbrayman
 * Date: 2/27/12
 */

@RunWith(Enclosed.class)
public class HibernateFeedRepositoryTest {

    public static class WhenPerformingSimpleAction {

        HibernateFeedRepository feedRepository;
        Map<String, String> parameters;
        SimpleSessionAction simpleSessionAction;

        @Before
        public void setup() throws Exception {
            parameters = new HashMap<String, String>();
            //parameters.put("hibernate.connection.url", "jdbc:h2:/opt/atomhopper/atom-hopper-db"); //removed to trigger exception
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");

            feedRepository = new HibernateFeedRepository(parameters);
            simpleSessionAction = mock(SimpleSessionAction.class);
        }

        @Test(expected=AtomDatabaseException.class)
        public void shouldThrowAtomDatabaseException() throws Exception {
            //parameters.get("")
            feedRepository.performSimpleAction(simpleSessionAction);
        }
    }

/*
    @Test
    public void testPerformComplexAction() throws Exception {

    }

    @Test
    public void testGetCategoriesForFeed() throws Exception {

    }

    @Test
    public void testGetFeedHead() throws Exception {

    }

    @Test
    public void testGetFeedPage() throws Exception {

    }

    @Test
    public void testSaveFeed() throws Exception {

    }

    @Test
    public void testUpdateCategories() throws Exception {

    }

    @Test
    public void testSaveEntry() throws Exception {

    }

    @Test
    public void testGetAllFeeds() throws Exception {

    }

    @Test
    public void testGetEntry() throws Exception {

    }

    @Test
    public void testGetFeed() throws Exception {

    }

    @Test
    public void testGetLastEntry() throws Exception {

    }
*/
}
