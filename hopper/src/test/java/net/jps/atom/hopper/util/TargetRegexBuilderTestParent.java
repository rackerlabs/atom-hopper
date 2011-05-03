package net.jps.atom.hopper.util;

public class TargetRegexBuilderTestParent {

    public TargetRegexBuilder workspaceRegexBuilder() {
        final TargetRegexBuilder target = new TargetRegexBuilder();
        target.setWorkspace("workspace");

        return target;
    }

    public TargetRegexBuilder feedRegexBuilder() {
        final TargetRegexBuilder target = workspaceRegexBuilder();
        target.setFeed("feed");

        return target;
    }


    public static final String[] DEFAULT_CATEGORIES_SHORT = new String[]{"category_1", "category_2"},
            DEFAULT_CATEGORIES_LONG = new String[]{"category_a", "category_b", "category_c", "category_d", "category_e"};
    public static final String CONTEXT_PATH = "/approot",
            WORKSPACE = "/workspace",
            CATEGORIES = "/workspace/feed/categories",
            FEED = "/workspace/feed",
            ENTRY = "/workspace/feed/entries/tag:domain.com,2011-01-01:entry-id",
            ARCHIVE = "/workspace/feed/archives",
            ARCHIVE_MARKER = "/workspace/feed/archives/abc123";


    public static String withCategories(String base, String[] categories) {
        final StringBuilder uri = new StringBuilder(base);
        uri.append("?categories=");

        if (categories.length >= 1) {
            uri.append(categories[0]);

            for (int i = 1; i < categories.length; i++) {
                uri.append(";").append(categories[i]);
            }
        }

        return uri.toString();
    }

    public static String addContextRoot(String base) {
        return CONTEXT_PATH + base;
    }

    public static String addTrailingSlash(String base) {
        return base + "/";
    }
}
