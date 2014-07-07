package org.atomhopper.abdera.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class extends the {@link FeedEntityTagProcessor} class and provides
 * additional behavior when building up the EntityTag to return to caller.
 *
 * The EntityTag of a feed page contains the first entry id and the last
 * entry id of the page, just like it's done {@link FeedEntityTagProcessor}.
 * This class appends additional tag to the EntityTag. The additional tag
 * needs to be configured based on the value of an HTTP Request Header
 * specified in the 'headerName' attribute of this bean. If an HTTP
 * Request Header with that name is present, then this class looks up
 * what tag to append to EntityTag based on the map in 'headerTagPostfixMap'.
 *
 * For example, if 'headerName' is 'x-access' and 'headerTagPostfixMap' contains
 * a mapping of 'level1' -> 'L1', 'level2' -> 'L2', then if a request comes in
 * with x-access: level1, the EntityTag will contain a postfix of 'L1'.
 *
 * User: shin4590
 * Date: 6/18/14
 */
@Component
public class HeaderValueFeedEntityTagProcessor extends FeedEntityTagProcessor {

    @Autowired
    private String headerName;

    @Autowired
    private boolean hashETag;

    @Autowired
    private Map<String, String> headerTagPostfixMap;

    protected EntityTag createEntityTag(RequestContext rc, String firstId, String lastId) {
        Set<String> postfixSet = new HashSet<String>();
        String headerName = getHeaderName();
        if ( headerName != null ) {
            Object[] roles = rc.getHeaders(headerName);
            if ( roles != null ) {
                for (Object aRole: roles) {
                    String postfix = headerTagPostfixMap.get(aRole);
                    if ( postfix != null )
                        postfixSet.add(postfix);
                }
            }
        }
        String allPostfix = StringUtils.join(postfixSet, ",");
        return new EntityTag(hashIt(firstId + ":" + lastId + ":" + allPostfix), true);
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Map<String, String> getHeaderTagPostfixMap() {
        return headerTagPostfixMap;
    }

    public void setHeaderTagPostfixMap(Map<String, String> headerTagPostfixMap) {
        this.headerTagPostfixMap = headerTagPostfixMap;
    }

    public boolean isHashETag() {
        return hashETag;
    }

    public void setHashETag(boolean hashETag) {
        this.hashETag = hashETag;
    }

    private String hashIt(String input) {
        if (hashETag) {
            return DigestUtils.md5Hex(input);
        } else {
            return input;
        }
    }
}
