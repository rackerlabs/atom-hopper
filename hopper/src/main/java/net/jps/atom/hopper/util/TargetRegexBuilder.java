/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util;

import com.rackspace.cloud.commons.util.StringUtilities;

/**
 *
 * @author zinic
 */
public class TargetRegexBuilder {

    private static final String REPLACEMENT_ELEMENT = "@_",
            WORKSPACE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            CATAGORY_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/categories/?(\\?[^#]+)?",
            FEED_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            ENTRY_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/entries/([^/#?]+)/?(\\?[^#]+)?",
            ARCHIVE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/archives(/\\d\\d\\d\\d)(/\\d\\d)?(/\\d\\d)?(/\\d\\d)?(/\\d\\d:\\d\\d)?/?(\\?[^#]+)?";
    
    private String workspace, feed;

    public TargetRegexBuilder() {
        workspace = feed = null;
    }
    
    public TargetRegexBuilder(TargetRegexBuilder copyMe) {
        workspace = copyMe.workspace;
        feed = copyMe.feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    private void checkWorkspaceString() {
        if (StringUtilities.isBlank(workspace)) {
            throw new IllegalStateException("Can not produce a regex pattern without the workspace field of the builder being set!");
        }
    }

    private void checkFeedString() {
        checkWorkspaceString();
        
        if (StringUtilities.isBlank(feed)) {
            throw new IllegalStateException("Can not produce a regex pattern without the workspace or feed fields of the builder being set!");
        }
    }

    public String toWorkspacePattern() {
        checkWorkspaceString();

        return WORKSPACE_TEMPLATE.replaceAll(REPLACEMENT_ELEMENT, workspace);
    }

    public String toCategoryPattern() {
        checkWorkspaceString();

        return CATAGORY_TEMPLATE.replaceAll(REPLACEMENT_ELEMENT, workspace);
    }

    public String toFeedPattern() {
        checkFeedString();
        
        return FEED_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed);
    }

    public String toEntryPattern() {
        checkFeedString();
        
        return ENTRY_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed);
    }

    public String toArchivePattern() {
        checkFeedString();
        
        return ARCHIVE_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed);
    }
}
