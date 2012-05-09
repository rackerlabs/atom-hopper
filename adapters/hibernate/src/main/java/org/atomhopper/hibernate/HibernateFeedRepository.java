package org.atomhopper.hibernate;

import java.util.ArrayList;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

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
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

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
        return performComplexAction(new ComplexSessionAction<Set<PersistedCategory>>() {

            @Override
            public Set<PersistedCategory> perform(Session liveSession) {
                final Set<PersistedCategory> updatedCategories = new HashSet<PersistedCategory>();

                for (PersistedCategory entryCategory : categories) {
                    PersistedCategory liveCategory = (PersistedCategory) liveSession.createCriteria(PersistedCategory.class)
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
    }

    @Override
    public void saveEntry(final PersistedEntry entry) {
        performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                PersistedFeed feed = (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq(entry.getFeed().getName())).uniqueResult();

                if (feed == null) {
                    feed = entry.getFeed();
                }

                liveSession.saveOrUpdate(feed);
                liveSession.save(entry);
            }
        });
    }

    @Override
    public Collection<PersistedFeed> getAllFeeds() {
        return performComplexAction(new ComplexSessionAction<Collection<PersistedFeed>>() {

            @Override
            public Collection<PersistedFeed> perform(Session liveSession) {
                return liveSession.createCriteria(PersistedFeed.class).list();
            }
        });
    }

    @Override
    public PersistedEntry getEntry(final String entryId, final String feedName) {
        return performComplexAction(new ComplexSessionAction<PersistedEntry>() {

            @Override
            public PersistedEntry perform(Session liveSession) {
                return (PersistedEntry) liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.idEq(entryId)).add(Restrictions.eq("feed.name", feedName)).uniqueResult();
            }
        });
    }

    @Override
    public PersistedFeed getFeed(final String name) {
        return performComplexAction(new ComplexSessionAction<PersistedFeed>() {

            @Override
            public PersistedFeed perform(Session liveSession) {
                return (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq(name)).uniqueResult();
            }
        });
    }

    @Override
    public List<PersistedEntry> getLastPage(final String feedName, final int pageSize) {
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {

                final LinkedList<PersistedEntry> lastPage = new LinkedList<PersistedEntry>();
                
                lastPage.addAll(liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.eq(FEED_NAME, feedName))
                        .addOrder(Order.asc(DATE_LAST_UPDATED))
                        .setMaxResults(pageSize).list());
                
                
                return lastPage;
            }
        });
    }

    @Override
    public List<PersistedEntry> getNextMarker(final PersistedEntry persistedEntry, final String feedName) {
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {

                final LinkedList<PersistedEntry> page = new LinkedList<PersistedEntry>();

                page.addAll(liveSession.createCriteria(PersistedEntry.class)
                        .add(Restrictions.eq(FEED_NAME, feedName))
                        .add(Restrictions.lt(DATE_LAST_UPDATED, persistedEntry.getCreationDate()))
                        .addOrder(Order.desc(DATE_LAST_UPDATED))
                        .setMaxResults(1).list());


                return page;
            }
        });
    }
}
