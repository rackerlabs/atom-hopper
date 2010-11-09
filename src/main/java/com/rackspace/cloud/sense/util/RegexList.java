
package com.rackspace.cloud.sense.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexList {
    private final List<Pattern> regexMatchers;

    public RegexList() {
        this.regexMatchers = new LinkedList<Pattern>();
    }

    public void add(String newRegexTarget) {
        regexMatchers.add(Pattern.compile(newRegexTarget));
    }

    public boolean targets(String target) {
        for (Pattern targetPattern : regexMatchers) {
            if (targetPattern.matcher(target).matches()) {
                return true;
            }
        }

        return false;
    }
}
