/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util;

import com.rackspace.cloud.commons.util.StringUtilities;
import java.util.regex.Pattern;

/**
 *
 * Regex builder standards
 * 
 * Workspace: /(workspace)/?
 * Categories: /(workspace)/categories/?
 * Feeds: /(workspace)/(feed)/?(\?[^#]+)?
 * Entries: /(workspace)/(feed)/entries/([^/#?]+)
 * Archives: /(workspace)/(feed)/archives/([^?#]+)(\?[^#]+)?
 * 
 * @author zinic
 */
public class TargetRegexBuilder {

    private static final String REPLACEMENT_ELEMENT = "@_",
            WORKSPACE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/?",
            FEED_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            ENTRY_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/entries/([^/#?]+)/?(\\?[^#]+)?",
            ARCHIVE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/archives(/\\d\\d\\d\\d)(/\\d\\d)?(/\\d\\d)?(/\\d\\d)?(/\\d\\d:\\d\\d)?/?(\\?[^#]+)?";
    
    private String workspace, feed;

    public TargetRegexBuilder() {
        workspace = feed = null;
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

    public Pattern toWorkspacePattern() {
        checkWorkspaceString();

        return Pattern.compile(WORKSPACE_TEMPLATE.replaceAll(REPLACEMENT_ELEMENT, workspace));
    }

    public Pattern toFeedPattern() {
        checkFeedString();
        
        return Pattern.compile(FEED_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed));
    }

    public Pattern toEntryPattern() {
        checkFeedString();
        
        return Pattern.compile(ENTRY_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed));
    }

    public Pattern toArchivePattern() {
        checkFeedString();
        
        return Pattern.compile(ARCHIVE_TEMPLATE.replaceFirst(REPLACEMENT_ELEMENT, workspace).replaceFirst(REPLACEMENT_ELEMENT, feed));
    }
}
