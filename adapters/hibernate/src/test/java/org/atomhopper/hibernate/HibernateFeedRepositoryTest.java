package org.atomhopper.hibernate;

import static org.mockito.Mockito.mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.dbal.AtomDatabaseException;
import org.atomhopper.hibernate.actions.ComplexSessionAction;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

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

    public static class WhenCreatingEntry {
        static HibernateFeedRepository feedRepository;

        @BeforeClass
        public static void setup() throws Exception {
            Map<String, String> parameters;
            parameters = new HashMap<String, String>();
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.connection.url", "jdbc:h2:mem:WhenCreatingFeed");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");

            feedRepository = new HibernateFeedRepository(parameters);

            PersistedEntry entry = new PersistedEntry("entryId");
            entry.setCategories(Collections.singleton(new PersistedCategory("term")));

            PersistedFeed feed = new PersistedFeed("feedName", "feedId");
            entry.setFeed(feed);

            feedRepository.saveEntry(entry);
        }

        @Test
        public void shouldAlsoCreateFeed() throws Exception {
            final Collection<PersistedFeed> feeds = feedRepository.getAllFeeds();
            assertEquals(1, feeds.size());
            final PersistedFeed feed = feeds.iterator().next();
            assertEquals("feedName", feed.getName());
            assertEquals("feedId", feed.getFeedId());
        }

        @Test
        public void shouldFindEntry() throws Exception {
            final PersistedEntry entry = feedRepository.getEntry("entryId", "feedName");
            assertEquals("entryId", entry.getEntryId());
        }

        @Test
        public void shouldCreateCategories() {
            feedRepository.performSimpleAction(new SimpleSessionAction() {
                @Override
                public void perform(Session liveSession) {
                    final PersistedEntry entry = (PersistedEntry) liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.idEq("entryId")).add(Restrictions.eq("feed.name", "feedName")).uniqueResult();
                    assertEquals(1, entry.getCategories().size());
                    final PersistedCategory category = entry.getCategories().iterator().next();
                    assertEquals("term", category.getTerm());
                }
            });
        }

        @Test
        public void shouldFindEntryInFeed() throws Exception {
            feedRepository.performSimpleAction(new SimpleSessionAction() {
                @Override
                public void perform(Session liveSession) {
                    final List feeds = liveSession.createCriteria(PersistedFeed.class).list();
                    final PersistedFeed feed = (PersistedFeed) feeds.iterator().next();
                    assertEquals(1, feed.getEntries().size());
                    final PersistedEntry entry = feed.getEntries().iterator().next();
                    assertEquals("entryId", entry.getEntryId());
                }
            });
        }
    }

    public static class WhenGettingCategories {
        static HibernateFeedRepository feedRepository;
        static Set<PersistedCategory> categories;

        @BeforeClass
        public static void setup() throws Exception {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.connection.url", "jdbc:h2:mem:WhenGettingCategories");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");

            feedRepository = new HibernateFeedRepository(parameters);

            PersistedEntry entry1 = new PersistedEntry("entryId1");
            PersistedCategory cat1 = new PersistedCategory("cat1");
            PersistedCategory cat2 = new PersistedCategory("cat2");
            entry1.setCategories(new HashSet<PersistedCategory>(Arrays.asList(cat1, cat2)));

            PersistedEntry entry2 = new PersistedEntry("entryId2");
            PersistedCategory cat3 = new PersistedCategory("cat3");
            entry2.setCategories(Collections.singleton(cat3));

            PersistedFeed feed = new PersistedFeed("feedName", "feedId");
            for (PersistedEntry entry : new PersistedEntry[] { entry1, entry2 }) {
                entry.setFeed(feed);
                feedRepository.saveEntry(entry);
            }

            categories = feedRepository.getCategoriesForFeed("feedName");
            for (PersistedCategory category : categories) {
                System.out.println(category.getTerm());
            }
        }

        @Test
        public void shouldContainExpectedCategories() throws Exception {
            assertTrue(categories.containsAll(Arrays.asList(
                new PersistedCategory("cat1"), new PersistedCategory("cat2"), new PersistedCategory("cat3")
            )));
        }

        @Test
        public void shouldNotContainOtherCategories() throws Exception {
            assertEquals(3, categories.size());

        }
    }
}
