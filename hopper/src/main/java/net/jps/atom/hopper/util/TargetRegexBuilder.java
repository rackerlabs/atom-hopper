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
            WORKSPACE_TEMPLATE = REPLACEMENT_ELEMENT + "(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            CATAGORY_TEMPLATE = REPLACEMENT_ELEMENT + "(" + REPLACEMENT_ELEMENT + ")/categories/?(\\?[^#]+)?",
            FEED_TEMPLATE = REPLACEMENT_ELEMENT + "(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            ENTRY_TEMPLATE = REPLACEMENT_ELEMENT + "(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/entries/([^/#?]+)/?(\\?[^#]+)?",
            ARCHIVE_TEMPLATE = REPLACEMENT_ELEMENT + "(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/archives(/\\d\\d\\d\\d)(/\\d\\d)?(/\\d\\d)?(/\\d\\d:\\d\\d)?/?(\\?[^#]+)?";
    
    private String contextPath, workspace, feed;

    public TargetRegexBuilder() {
        workspace = null;
        feed = null;
        contextPath = "/";
    }
    
    public TargetRegexBuilder(TargetRegexBuilder copyMe) {
        workspace = copyMe.workspace;
        feed = copyMe.feed;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath.endsWith("/") ? contextPath : contextPath + "/";
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
    
    private String insertContextPath(String base) {
        return base.replaceFirst(REPLACEMENT_ELEMENT, contextPath);
    }
    
    private String asWorkspacePattern(String base) {
        return insertContextPath(base).replaceFirst(REPLACEMENT_ELEMENT, workspace);
    }
    
    private String asFeedPattern(String base) {
        return asWorkspacePattern(base).replaceFirst(REPLACEMENT_ELEMENT, feed);
    }
    
    public String toWorkspacePattern() {
        checkWorkspaceString();

        return asWorkspacePattern(WORKSPACE_TEMPLATE);
    }

    public String toCategoryPattern() {
        checkWorkspaceString();

        return asWorkspacePattern(CATAGORY_TEMPLATE);
    }

    public String toFeedPattern() {
        checkFeedString();
        
        return asFeedPattern(FEED_TEMPLATE);
    }

    public String toEntryPattern() {
        checkFeedString();
        
        return asFeedPattern(ENTRY_TEMPLATE);
    }

    public String toArchivePattern() {
        checkFeedString();
        
        return asFeedPattern(ARCHIVE_TEMPLATE);
    }
}
