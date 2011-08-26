package org.atomhopper.dbal;

/**
 * The PageDirection to request the page forward of the marker or backward
 * of the marker. The direction is based on the temporal direction of the feed 
 * where forward is any time period after the creation date of the marker and 
 * backward is any time period before the creation date of the marker.
 * 
 * Forward pages should provides the next page, marker inclusive - AKA Previous.
 * Backward pages should provides the previous page, marker exclusive - AKA Next.
 * 
 * 
 * HEAD (First Entry in Live Feed)                       PAST (Older Entries)
 * =========================================================================
 *                                  M
 *                     PREVIOUS     a     NEXT
 *                     BACKWARD     r     FORWARD
 *                                  k
 *                                  e
 *                                  r
 * @author zinic
 */
public enum PageDirection {

    FORWARD, BACKWARD
}
