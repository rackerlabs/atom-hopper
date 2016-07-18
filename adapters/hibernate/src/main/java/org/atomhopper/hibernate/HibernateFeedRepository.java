package org.atomhopper.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.dbal.AtomDatabaseException;
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.hibernate.actions.ComplexSessionAction;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.atomhopper.hibernate.query.CategoryCriteriaGenerator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateFeedRepository implements FeedRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateFeedRepository.class);
    private final HibernateSessionManager sessionManager;
    private static final String DATE_LAST_UPDATED = "dateLastUpdated";
    private static final String FEED_NAME = "feed.name";

    public HibernateFeedRepository(Map<String, String> parameters) {
        sessionManager = new HibernateSessionManager(parameters);
    }

    public void performSimpleAction(SimpleSessionAction action) {
        final long begin = System.currentTimeMillis();
        LOG.debug("~!$: Simple Action Session begin: " + begin);

        final Session session = sessionManager.getSession();

        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            action.perform(session);

            tx.commit();
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }

            throw new AtomDatabaseException("Failure performing hibernate action: " + action.toString(), ex);
        } finally {
            LOG.debug("~!$: Closing session. Elapsed time: " + (System.currentTimeMillis() - begin));
            session.close();
        }
    }

    public <T> T performComplexAction(ComplexSessionAction<T> action) {
        final long begin = System.currentTimeMillis();
        LOG.debug("~!$: Complex Action Session begin: " + begin);

        final Session session = sessionManager.getSession();

        T returnable = null;
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            returnable = action.perform(session);

            tx.commit();

            return returnable;
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }

            throw new AtomDatabaseException("Failure performing hibernate action: " + action.toString(), ex);
        } finally {
            LOG.debug("~!$: Closing session. Elapsed time: " + (System.currentTimeMillis() - begin));
            session.close();
        }
    }

    public <T> T performComplexActionNonTransactionable(ComplexSessionAction<T> action) {
        final Session session = sessionManager.getSession();

        T returnable = null;

        try {
            returnable = action.perform(session);

            return returnable;
        } catch (Exception ex) {
            throw new AtomDatabaseException("Failure performing hibernate action: " + action.toString(), ex);
        } finally {
            session.close();
        }
    }

    @Override
    public Set<PersistedCategory> getCategoriesForFeed(final String feedName) {
        return performComplexAction(new ComplexSessionAction<Set<PersistedCategory>>() {

            @Override
            public Set<PersistedCategory> perform(Session liveSession) {
                final PersistedFeed persistedFeed = (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq(feedName)).uniqueResult();
                final Set<PersistedCategory> categories = new HashSet<PersistedCategory>();

                if (persistedFeed != null) {
                    for (PersistedEntry entry : persistedFeed.getEntries()) {
                        categories.addAll(entry.getCategories());
                    }
                }

                return categories;
            }
        });
    }

    @Override
    public List<PersistedEntry> getFeedHead(final String feedName, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize) {
        return performComplexActionNonTransactionable(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                final List<PersistedEntry> feedHead = new LinkedList<PersistedEntry>();

                final Criteria criteria = liveSession.createCriteria(PersistedEntry.class).add(Restrictions.eq(FEED_NAME, feedName));
                criteriaGenerator.enhanceCriteria(criteria);

                criteria.setMaxResults(pageSize).addOrder(Order.desc(DATE_LAST_UPDATED));

                feedHead.addAll(criteria.list());

                return feedHead;
            }
        });
    }

    @Override
    public List<PersistedEntry> getFeedPage(final String feedName, final PersistedEntry markerEntry, final PageDirection direction,
                                            final CategoryCriteriaGenerator criteriaGenerator, final int pageSize) {
        return performComplexActionNonTransactionable(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                final LinkedList<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

                final Criteria criteria = liveSession.createCriteria(PersistedEntry.class).add(Restrictions.eq(FEED_NAME, feedName));
                criteriaGenerator.enhanceCriteria(criteria);
                criteria.setMaxResults(pageSize);

                switch (direction) {
                    case FORWARD:
                        criteria.add(Restrictions.gt(DATE_LAST_UPDATED, markerEntry.getCreationDate())).addOrder(Order.asc(DATE_LAST_UPDATED));
                        feedPage.addAll(criteria.list());
                        Collections.reverse(feedPage);
                        break;

                    case BACKWARD:
                        criteria.add(Restrictions.le(DATE_LAST_UPDATED, markerEntry.getCreationDate())).addOrder(Order.desc(DATE_LAST_UPDATED));
                        feedPage.addAll(criteria.list());
                        break;
                }

                return feedPage;
            }
        });
    }

    @Override
    public void saveFeed(final PersistedFeed feed) {
        performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                liveSession.persist(feed);
            }
        });
    }

    @Override
    public Set<PersistedCategory> updateCategories(final Set<PersistedCategory> categories) {
        /* Creating caretories can fail if there's another request in parallel
         * attempting to create the same categories, in another transaction.
         * If that other transaction completes first, then the check for
         * whether categories already exist will indicate that they don't, due
         * to transaction isolation. After the ComplexSessionAction returns,
         * performComplexAction will attempt to commit, which fails due to the
         * categories already aving been inserted and committed in the other
         * transaction.
         * To handle this, we need to retry the whole transaction. This time
         * around, the criterion that failed the earlier transaction will be
         * found, and therefore skipped. We thus need to retry at most
         * categories.size() times. */
        int attemptsLeft = categories.size() + 1;
        AtomDatabaseException firstException = null;
        while (attemptsLeft-- > 0) {
            try {
                return performComplexAction(new ComplexSessionAction<Set<PersistedCategory>>() {

                    @Override
                    public Set<PersistedCategory> perform(Session liveSession) {
                        final Set<PersistedCategory> updatedCategories = new HashSet<PersistedCategory>();

                        for (PersistedCategory entryCategory : categories) {
                            PersistedCategory liveCategory =
                                (PersistedCategory) liveSession.createCriteria(PersistedCategory.class)
                                    .add(Restrictions.idEq(entryCategory.getTerm())).uniqueResult();

                            if (liveCategory == null) {
                                liveCategory = new PersistedCategory(entryCategory.getTerm());
                                liveSession.save(liveCategory);
                            }

                            updatedCategories.add(liveCategory);
                        }

                        return updatedCategories;
                    }
                });
            } catch (AtomDatabaseException e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        throw firstException;
    }

    @Override
    public void saveEntry(final PersistedEntry entry) {
        /* Retry in case of category creation failure (see updateCategories) */
        int attemptsLeft = entry.getCategories().size() + 1;
        AtomDatabaseException firstException = null;
        while (attemptsLeft-- > 0) {
            try {
                performSimpleAction(new SimpleSessionAction() {

                    @Override
                    public void perform(Session liveSession) {
                        PersistedFeed feed = (PersistedFeed) liveSession.createCriteria(PersistedFeed.class)
                            .add(Restrictions.idEq(entry.getFeed().getName())).uniqueResult();

                        if (feed == null) {
                            feed = entry.getFeed();
                        }

                        liveSession.saveOrUpdate(feed);
                        liveSession.save(entry);
                    }
                });
                return;
            } catch (AtomDatabaseException e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        throw firstException;
    }

    @Override
    public Collection<PersistedFeed> getAllFeeds() {
        return performComplexActionNonTransactionable(new ComplexSessionAction<Collection<PersistedFeed>>() {

            @Override
            public Collection<PersistedFeed> perform(Session liveSession) {
                return liveSession.createCriteria(PersistedFeed.class).list();
            }
        });
    }

    @Override
    public PersistedEntry getEntry(final String entryId, final String feedName) {
        return performComplexActionNonTransactionable(new ComplexSessionAction<PersistedEntry>() {

            @Override
            public PersistedEntry perform(Session liveSession) {
                return (PersistedEntry) liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.idEq(entryId)).add(Restrictions.eq("feed.name", feedName)).uniqueResult();
            }
        });
    }

    @Override
    public PersistedFeed getFeed(final String name) {
        return performComplexActionNonTransactionable(new ComplexSessionAction<PersistedFeed>() {

            @Override
            public PersistedFeed perform(Session liveSession) {
                return (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq(name)).uniqueResult();
            }
        });
    }

    @Override
    public List<PersistedEntry> getLastPage(final String feedName, final int pageSize, final CategoryCriteriaGenerator criteriaGenerator) {

        return performComplexActionNonTransactionable(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                Criteria criteria = liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.eq(FEED_NAME, feedName))
                        .addOrder(Order.asc(DATE_LAST_UPDATED))
                        .setMaxResults(pageSize);

                criteriaGenerator.enhanceCriteria(criteria);

                List<PersistedEntry> entries = criteria.list();

                return entries.size() > 0 ? entries : null;
            }
        });
    }

    @Override
    public PersistedEntry getNextMarker(final PersistedEntry persistedEntry, final String feedName, final CategoryCriteriaGenerator criteriaGenerator) {

        return performComplexActionNonTransactionable(new ComplexSessionAction<PersistedEntry>() {

            @Override
            public PersistedEntry perform(Session liveSession) {
                Criteria criteria = liveSession.createCriteria(PersistedEntry.class);

                criteria.add(Restrictions.eq(FEED_NAME, feedName))
                        .add(Restrictions.lt(DATE_LAST_UPDATED, persistedEntry.getCreationDate()))
                        .addOrder(Order.desc(DATE_LAST_UPDATED))
                        .setMaxResults(1);

                criteriaGenerator.enhanceCriteria(criteria);

                List<PersistedEntry> entries = criteria.list();

                return entries.size() > 0 ? (PersistedEntry) entries.get(0) : null;
            }
        });
    }

    private int safeLongToInt(long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (value + " cannot be cast to int without changing its value.");
        }
        return (int) value;
    }
}
