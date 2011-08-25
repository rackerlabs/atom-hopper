/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atomhopper.hibernate;

import java.util.Collections;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author zinic
 */
public class HibernateFeedRepositoryTest {

    public static void main(String[] args) {
        final HibernateFeedRepository feedRepository = new HibernateFeedRepository(Collections.EMPTY_MAP);

        feedRepository.saveFeed("testing");
        Feed f = feedRepository.getFeed("testing");

        System.out.println(f != null ? f.getName() : "null");

        FeedEntry entry = new FeedEntry("some-random-uuid");
        entry.setFeed(new Feed("testing"));

        feedRepository.saveEntry(entry);

        feedRepository.performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                Feed f = (Feed) liveSession.createCriteria(Feed.class).add(Restrictions.idEq("testing")).list().get(0);

                System.out.println("Entries: " + (f != null ? f.getEntries().size() : 0));
            }
        });
    }
}
