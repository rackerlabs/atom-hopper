package org.atomhopper.hibernate;

import java.util.List;
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.dbal.AtomDatabaseException;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.hibernate.actions.ComplexSessionAction;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class HibernateFeedRepository implements FeedRepository {

    private final HibernateSessionManager sessionManager;

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
    public List<PersistedEntry> getFeedPage(final String feedName, final String marker, final int pageSize, final PageDirection direction) {
        final PersistedEntry markerEntry = getEntry(marker);

        return performComplexAction(new ComplexSessionAction<List<PersistedEntry>>() {

            @Override
            public List<PersistedEntry> perform(Session liveSession) {
                final LinkedList<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

                final Criteria criteria = liveSession.createCriteria(PersistedEntry.class);
                criteria.setMaxResults(pageSize);
                criteria.addOrder(Order.asc("created"));

                switch (direction) {
                    case FORWARD:
                        criteria.add(Restrictions.gt("created", markerEntry.getCreationDate()));
                        feedPage.add(markerEntry);
                        feedPage.addAll(criteria.list());
                        break;

                    case BACKWARD:
                        criteria.add(Restrictions.lt("created", markerEntry.getCreationDate()));
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
                liveSession.persist(entry);

                // Make sure to update our category objects
                for (PersistedCategory cat : entry.getCategories()) {
                    PersistedCategory category = (PersistedCategory) liveSession.createCriteria(PersistedCategory.class).add(Restrictions.idEq(cat.getTerm())).uniqueResult();

                    if (category == null) {
                        category = cat;
                    }

                    category.getFeedEntries().add(entry);
                    liveSession.saveOrUpdate(category);
                }
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
