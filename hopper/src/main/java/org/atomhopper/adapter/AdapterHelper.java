package org.atomhopper.adapter;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.atomhopper.adapter.FeedSource;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * A helper class for objects which implement the FeedSource interface.  This helper class tracks the following:
 * <ul>
 *     <li>If the feed is an archive feed, and if so, the URL for its "current" link.</li>
 *     <li>If the feed has an archived feed, and if so its URL.</li>
 *     <li>Uses the standard "next/previous" markers for links or "next-archive/prev-archive" if an archived link.</li>
 *     <li>Add the suggested &lt;archive&gt; to the feed if its an archive feed.</li>
 * </ul>
 */
public class AdapterHelper {

    static public final String ARCHIVE_NS = "http://purl.org/syndication/history/1.0";
    static public final String ARCHIVE = "archive";
    static public final String ARCHIVE_PREFIX = "fh";

    private String archiveUrl;
    private String nextLink = Link.REL_NEXT;
    private String prevLink = Link.REL_PREVIOUS;
    private String currentUrl = null;

    public void setArchiveUrl( URL url ) {

        archiveUrl = url.toExternalForm();
    }

    public String getArchiveUrl() {
        return archiveUrl;
    }

    public boolean isArchived() {

        return currentUrl != null;
    }

    public void setCurrentUrl( URL currentUrl ) {

        this.currentUrl = currentUrl.toExternalForm();

        nextLink = FeedSource.REL_ARCHIVE_NEXT;
        prevLink = FeedSource.REL_ARCHIVE_PREV;
     }

    public String getCurrentUrl() {

        return currentUrl;
    }

    public String getNextLink() {
        return nextLink;
    }

    public String getPrevLink() {
        return prevLink;
    }

    public Feed addArchiveNode( Feed feed ) {

        Factory factory = feed.getFactory();

        Element root = feed.getDocument().getRoot();

        Element elementArchive = factory.newExtensionElement( new QName( ARCHIVE_NS, ARCHIVE, ARCHIVE_PREFIX ), root );

        elementArchive.setParentElement( root );

        return feed;
    }
}


