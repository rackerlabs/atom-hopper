package org.atomhopper.hibernate;

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

    public static class WhenCreatingMisconfiguredFeedRepository{
        Map<String, String> parameters;

        @Before
        public void setup() throws Exception {
            parameters = new HashMap<String, String>();
            //parameters.put("hibernate.connection.url", "jdbc:h2:/opt/atomhopper/atom-hopper-db"); //removed to trigger exception
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldThrowAtomDatabaseException() throws Exception {
            new HibernateFeedRepository(parameters);
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

    public static class WhenOperatingInParallel {
        static HibernateFeedRepository feedRepository;
        final CyclicBarrier barrier = new CyclicBarrier(2);
        static Runner runner1 = new Runner();
        static Runner runner2 = new Runner();

        @BeforeClass
        public static void setup() throws Exception {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("hibernate.connection.driver_class", "org.h2.Driver");
            parameters.put("hibernate.connection.url", "jdbc:h2:mem:WhenOperatingInParallel");
            parameters.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            parameters.put("hibernate.connection.username", "sa");
            parameters.put("hibernate.connection.password", "");
            parameters.put("hibernate.hbm2ddl.auto", "update");

            feedRepository = new HibernateFeedRepository(parameters);
        }

        static class Runner {
            Thread t;

            interface Operation<T> {
                T run();
            }

            interface Future<T> {
                T get(long timeout) throws InterruptedException;
            }

            <T> Future<T> run(final Operation<T> op) {
                final ArrayList<T> result = new ArrayList<T>(1);
                final ArrayList<RuntimeException> exception = new ArrayList<RuntimeException>(1);
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.add(op.run());
                        } catch (RuntimeException e) {
                            exception.add(e);
                        }
                    }
                });
                t.start();
                return new Future<T>() {
                    @Override
                    public T get(long timeout) throws InterruptedException {
                        t.join(timeout);
                        if (t.isAlive()) {
                            t.interrupt();
                            t.join();
                        }
                        if (!exception.isEmpty()) {
                            throw new RuntimeException("Operation threw exception", exception.get(0));
                        }
                        return result.get(0);
                    }
                };
            }
        }

        @Test
        public void shouldCreateCategoriesWithoutDuplicates() throws Exception {

            final Runner.Future<Integer> r1 = runner1.run(new Runner.Operation<Integer>() {
                @Override
                public Integer run() {
                    final PersistedEntry entry = new PersistedEntry("entry1");
                    entry.setCategories(Collections.singleton(new PersistedCategory("cat")));
                    PersistedFeed feed = new PersistedFeed("feed1", "feed1");
                    feed.setEntries(Collections.singleton(entry));
                    entry.setFeed(feed);
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    feedRepository.saveEntry(entry);
                    return null;
                }
            });
            final Runner.Future<Integer> r2 = runner2.run(new Runner.Operation<Integer>() {
                @Override
                public Integer run() {
                    final PersistedEntry entry = new PersistedEntry("entry2");
                    entry.setCategories(Collections.singleton(new PersistedCategory("cat")));
                    PersistedFeed feed = new PersistedFeed("feed2", "feed2");
                    feed.setEntries(Collections.singleton(entry));
                    entry.setFeed(feed);
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    feedRepository.saveEntry(entry);
                    return null;
                }
            });
            r1.get(3000);
            r2.get(3000);
        }

        @Test
        public void shouldUpdateCategoriesWithoutDuplicates() throws Exception {

            final Runner.Future<Integer> r1 = runner1.run(new Runner.Operation<Integer>() {
                @Override
                public Integer run() {
                    final Set<PersistedCategory> categories = Collections.singleton(new PersistedCategory("update"));
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    feedRepository.updateCategories(categories);
                    return null;
                }
            });
            final Runner.Future<Integer> r2 = runner2.run(new Runner.Operation<Integer>() {
                @Override
                public Integer run() {
                    final Set<PersistedCategory> categories = Collections.singleton(new PersistedCategory("update"));
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    feedRepository.updateCategories(categories);
                    return null;

                }
            });
            r1.get(3000);
            r2.get(3000);
        }
    }
}
