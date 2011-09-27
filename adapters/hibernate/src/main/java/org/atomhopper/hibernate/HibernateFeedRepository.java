package org.atomhopper.hibernate;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateFeedRepository implements FeedRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateFeedRepository.class);
    private final HibernateSessionManager sessionManager;
    private static final String DATE_LAST_UPDATED = "dateLastUpdated";

    public HibernateFeedRepository(Map<String, String> parameters) {
        sessionManager = new HibernateSessionManager(parameters);
    }

    public void performSimpleAction(SimpleSessionAction action) {
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
            session.close();
        }
    }

    public <T> T performComplexAction(ComplexSessionAction<T> action) {
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
            session.close();
        }
    }

    @Override
    public Set<PersistedCategory> getCategoriesForFeeed(final String feedName) {
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
    public List<PersistedEntry> getFeedHead(final String feedName, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize, final String feedOrder) {
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                final List<PersistedEntry> feedHead = new LinkedList<PersistedEntry>();

                final Criteria criteria = liveSession.createCriteria(PersistedEntry.class);
                criteriaGenerator.enhanceCriteria(criteria);
                
                criteria.setMaxResults(pageSize);
                if(feedOrder.equalsIgnoreCase("asc")) {
                    criteria.addOrder(Order.asc(DATE_LAST_UPDATED));
                } else {
                    criteria.addOrder(Order.desc(DATE_LAST_UPDATED));
                }
                
                feedHead.addAll(criteria.list());

                return feedHead;
            }
        });
    }

    @Override
    public List<PersistedEntry> getFeedPage(final String feedName, final PersistedEntry markerEntry, final PageDirection direction, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize, final String feedOrder) {
        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                final LinkedList<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

                final Criteria criteria = liveSession.createCriteria(PersistedEntry.class);
                criteriaGenerator.enhanceCriteria(criteria);

                criteria.setMaxResults(pageSize);
                if (feedOrder.equalsIgnoreCase("asc")) {
                    criteria.addOrder(Order.asc(DATE_LAST_UPDATED));
                } else {
                    criteria.addOrder(Order.desc(DATE_LAST_UPDATED));
                }

                switch (direction) {
                    case FORWARD:
                        criteria.add(Restrictions.gt(DATE_LAST_UPDATED, markerEntry.getCreationDate()));
                        feedPage.add(markerEntry);
                        feedPage.addAll(criteria.list());
                        break;

                    case BACKWARD:
                        criteria.add(Restrictions.lt(DATE_LAST_UPDATED, markerEntry.getCreationDate()));
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
    public void saveEntry(final PersistedEntry entry) {
        performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                PersistedFeed feed = (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq(entry.getFeed().getName())).uniqueResult();

                if (feed == null) {
                    feed = entry.getFeed();
                }

                feed.getEntries().add(entry);
                liveSession.saveOrUpdate(feed);
                
                // Categories that actually don't exist in the DB yet
                Set<PersistedCategory> newCategories = new HashSet<PersistedCategory>();

                // Make sure to update our category objects
                for (PersistedCategory cat : entry.getCategories()) {
                    PersistedCategory category = (PersistedCategory) liveSession.createCriteria(PersistedCategory.class).add(Restrictions.idEq(cat.getTerm().toLowerCase())).uniqueResult();

                    if (category == null) {
                        cat.setTerm(cat.getTerm().toLowerCase());
                        cat.getFeedEntries().add(entry);
                        liveSession.save(cat);
                        newCategories.add(cat);
                    }
                }
                entry.setCategories(newCategories);                
                liveSession.persist(entry);
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
    public PersistedEntry getEntry(final String entryId) {
        return performComplexAction(new ComplexSessionAction<PersistedEntry>() {

            @Override
            public PersistedEntry perform(Session liveSession) {
                return (PersistedEntry) liveSession.createCriteria(PersistedEntry.class).add(Restrictions.idEq(entryId)).uniqueResult();
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
}
