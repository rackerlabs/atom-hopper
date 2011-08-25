package org.atomhopper.util;

import org.atomhopper.adapter.TargetResolverField;
import org.apache.commons.lang.StringUtils;

public class TargetRegexBuilder {

    private static final String REPLACEMENT_ELEMENT = "@_",
            WORKSPACE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")?(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            CATAGORY_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")?(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/categories/?(\\?[^#]+)?",
            FEED_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")?(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/?(\\?[^#]+)?",
            ENTRY_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")?(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + ")/entries/([^/#?]+)/?(\\?[^#]+)?",
            ARCHIVE_TEMPLATE = "/(" + REPLACEMENT_ELEMENT + ")?(" + REPLACEMENT_ELEMENT + ")/(" + REPLACEMENT_ELEMENT + "/archives)(/\\d\\d\\d\\d)(/\\d\\d)?(/\\d\\d)?(/\\d\\d:\\d\\d)?/?(\\?[^#]+)?",

            ARCHIVES_URI_FRAGMENT = "archives";

    private String contextPath, workspace, feed;

    public TargetRegexBuilder() {
        workspace = null;
        feed = null;
        contextPath = "";
    }

    public TargetRegexBuilder(TargetRegexBuilder copyMe) {
        contextPath = copyMe.contextPath;
        workspace = copyMe.workspace;
        feed = copyMe.feed;
    }

    public void setContextPath(String contextPath) {
        final StringBuilder builder = new StringBuilder();

        if (contextPath.startsWith("/")) {
            builder.append(contextPath.substring(1));
        } else {
            builder.append(contextPath);
        }

        if (!contextPath.endsWith("/")) {
            builder.append("/");
        }

        this.contextPath = builder.toString();
    }

    public void setFeed(String feed) {
        this.feed = StringUtils.strip(feed, "/");
    }

    public void setWorkspace(String workspace) {
        this.workspace = StringUtils.strip(workspace, "/");
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getFeedResource() {
        return feed;
    }

    public String getWorkspaceResource() {
        return workspace;
    }

    public String getArchivesResource() {
        return feed + ARCHIVES_URI_FRAGMENT;
    }

    private void checkWorkspaceString() {
        if (StringUtils.isBlank(workspace)) {
            throw new IllegalStateException("Can not produce a regex pattern without the workspace field of the builder being set!");
        }
    }

    private void checkFeedString() {
        checkWorkspaceString();

        if (StringUtils.isBlank(feed)) {
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

    public String toCategoriesPattern() {
        checkWorkspaceString();

        return asFeedPattern(CATAGORY_TEMPLATE);
    }

    public String toFeedPattern() {
        checkFeedString();

        return asFeedPattern(FEED_TEMPLATE);
    }

    public String toEntryPattern() {
        checkFeedString();

        return asFeedPattern(ENTRY_TEMPLATE);
    }

    public String toArchivesPattern() {
        checkFeedString();

        return asFeedPattern(ARCHIVE_TEMPLATE);
    }

    public static String[] getWorkspaceResolverFieldList() {
        return new String[]{
                    TargetResolverField.CONTEXT_PATH.toString(),
                    TargetResolverField.WORKSPACE.toString()
                };
    }

    public static String[] getFeedResolverFieldList() {
        return new String[]{
                    TargetResolverField.CONTEXT_PATH.toString(),
                    TargetResolverField.WORKSPACE.toString(),
                    TargetResolverField.FEED.toString()
                };
    }

    public static String[] getCategoriesResolverFieldList() {
        return new String[]{
                    TargetResolverField.CONTEXT_PATH.toString(),
                    TargetResolverField.WORKSPACE.toString(),
                    TargetResolverField.FEED.toString()
                };
    }

    public static String[] getEntryResolverFieldList() {
        return new String[]{
                    TargetResolverField.CONTEXT_PATH.toString(),
                    TargetResolverField.WORKSPACE.toString(),
                    TargetResolverField.FEED.toString(),
                    TargetResolverField.ENTRY.toString()
                };
    }

    public static String[] getArchiveResolverFieldList() {
        return new String[]{
                    TargetResolverField.CONTEXT_PATH.toString(),
                    TargetResolverField.WORKSPACE.toString(),
                    TargetResolverField.FEED.toString(),
                    TargetResolverField.ARCHIVE_YEAR.toString(),
                    TargetResolverField.ARCHIVE_MONTH.toString(),
                    TargetResolverField.ARCHIVE_DAY.toString(),
                    TargetResolverField.ARCHIVE_TIME.toString()
                };
    }
}
