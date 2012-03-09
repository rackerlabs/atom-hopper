package org.atomhopper.hibernate;

import org.atomhopper.dbal.AtomDatabaseException;
import org.atomhopper.hibernate.actions.ComplexSessionAction;
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
            feedRepository.performSimpleAction(simpleSessionAction);
        }
    }

    public static class WhenPerformingComplexAction {

        HibernateFeedRepository feedRepository;
        Map<String, String> parameters;
        ComplexSessionAction complexSessionAction;

        @Before
        public void setup() throws Exception {
            parameters = new HashMap<String, String>();
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");

            feedRepository = new HibernateFeedRepository(parameters);
            complexSessionAction = mock(ComplexSessionAction.class);
        }

        /*This should throw the error because */
        @Test(expected=AtomDatabaseException.class)
        public void shouldThrowAtomDatabaseException() throws Exception {
            feedRepository.performComplexAction(complexSessionAction);
        }
    }

    public static class WhenGettingCategories {

        @Before
        public void setup() throws Exception {

        }

        @Test
        public void shouldReturnFeedCategories() throws Exception {

        }
    }
}
