package org.apache.abdera.examples;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.UrlEncoding;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestContext.Scope;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.context.StreamWriterResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;
import org.apache.abdera.util.Constants;
import org.apache.abdera.writer.StreamWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
class SimpleAdapter extends AbstractCollectionAdapter {

    @Override
    public String getAuthor(RequestContext request) throws ResponseContextException {
        return "Simple McGee";
    }

    @Override
    public String getId(RequestContext request) {
        return "tag:example.org,2008:feed";
    }

    public String getHref(RequestContext request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("collection", "feed");
        return request.urlFor(TargetType.TYPE_COLLECTION, params);
    }

    public String getTitle(RequestContext request) {
        return "A simple feed";
    }

    public ResponseContext extensionRequest(RequestContext request) {
        return ProviderHelper.notallowed(request, "Method Not Allowed", ProviderHelper.getDefaultMethods(request));
    }

    private Document<Feed> getFeedDocument(RequestContext context) throws ResponseContextException {
        Feed feed = (Feed) context.getAttribute(Scope.SESSION, "feed");
        if (feed == null) {
            feed = createFeedBase(context);
            feed.setBaseUri(getHref(context));
            context.setAttribute(Scope.SESSION, "feed", feed);
        }
        return feed.getDocument();
    }

    public ResponseContext getFeed(RequestContext request) {
        Document<Feed> feed;
        try {
            feed = getFeedDocument(request);
        } catch (ResponseContextException e) {
            return e.getResponseContext();
        }

        return ProviderHelper.returnBase(feed, 200, feed.getRoot().getUpdated()).setEntityTag(ProviderHelper.calculateEntityTag(feed.getRoot()));
    }

    public ResponseContext deleteEntry(RequestContext request) {
        Entry entry = getAbderaEntry(request);
        if (entry != null) {
            entry.discard();
        }
        return ProviderHelper.nocontent();
    }

    public ResponseContext getEntry(RequestContext request) {
        Entry entry = getAbderaEntry(request);
        if (entry != null) {
            Feed feed = entry.getParentElement();
            entry = (Entry) entry.clone();
            entry.setSource(feed.getAsSource());
            Document<Entry> entry_doc = entry.getDocument();
            return ProviderHelper.returnBase(entry_doc, 200, entry.getEdited()).setEntityTag(ProviderHelper.calculateEntityTag(entry));
        } else {
            return ProviderHelper.notfound(request);
        }
    }

    public ResponseContext postEntry(RequestContext request) {
        Abdera abdera = request.getAbdera();
        try {
            Document<Entry> entry_doc = (Document<Entry>) request.getDocument(abdera.getParser()).clone();
            if (entry_doc != null) {
                Entry entry = entry_doc.getRoot();
                if (!ProviderHelper.isValidEntry(entry)) {
                    return ProviderHelper.badrequest(request);
                }
                setEntryDetails(request, entry, abdera.getFactory().newUuidUri());
                Feed feed = getFeedDocument(request).getRoot();
                feed.insertEntry(entry);
                feed.setUpdated(new Date());
                BaseResponseContext rc =
                        (BaseResponseContext) ProviderHelper.returnBase(entry_doc, 201, entry.getEdited());
                return rc.setLocation(ProviderHelper.resolveBase(request).resolve(entry.getEditLinkResolvedHref()).toString()).setContentLocation(rc.getLocation().toString()).setEntityTag(ProviderHelper.calculateEntityTag(entry));
            } else {
                return ProviderHelper.badrequest(request);
            }
        } catch (ParseException pe) {
            return ProviderHelper.notsupported(request);
        } catch (ClassCastException cce) {
            return ProviderHelper.notsupported(request);
        } catch (Exception e) {
            return ProviderHelper.badrequest(request);
        }
    }

    private void setEntryDetails(RequestContext request, Entry entry, String id) {
        entry.setUpdated(new Date());
        entry.setEdited(entry.getUpdated());
        entry.getIdElement().setValue(id);
        entry.addLink(getEntryLink(request, entry.getId().toASCIIString()), "edit");
    }

    private String getEntryLink(RequestContext request, String entryid) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("collection", request.getTarget().getParameter("collection"));
        params.put("entry", entryid);
        return request.urlFor(TargetType.TYPE_ENTRY, params);
    }

    public ResponseContext putEntry(RequestContext request) {
        Abdera abdera = request.getAbdera();
        Entry orig_entry = getAbderaEntry(request);
        if (orig_entry != null) {
            try {
                Document<Entry> entry_doc = (Document<Entry>) request.getDocument(abdera.getParser()).clone();
                if (entry_doc != null) {
                    Entry entry = entry_doc.getRoot();
                    if (!entry.getId().equals(orig_entry.getId())) {
                        return ProviderHelper.conflict(request);
                    }
                    if (!ProviderHelper.isValidEntry(entry)) {
                        return ProviderHelper.badrequest(request);
                    }
                    setEntryDetails(request, entry, orig_entry.getId().toString());
                    orig_entry.discard();
                    Feed feed = getFeedDocument(request).getRoot();
                    feed.insertEntry(entry);
                    feed.setUpdated(new Date());
                    return ProviderHelper.nocontent();
                } else {
                    return ProviderHelper.badrequest(request);
                }
            } catch (ParseException pe) {
                return ProviderHelper.notsupported(request);
            } catch (ClassCastException cce) {
                return ProviderHelper.notsupported(request);
            } catch (Exception e) {
                return ProviderHelper.badrequest(request);
            }
        } else {
            return ProviderHelper.notfound(request);
        }
    }

    private Entry getAbderaEntry(RequestContext request) {
        try {
            return getFeedDocument(request).getRoot().getEntry(getResourceName(request));
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getResourceName(RequestContext request) {
        if (request.getTarget().getType() != TargetType.TYPE_ENTRY) {
            return null;
        }
        String[] segments = request.getUri().toString().split("/");
        return UrlEncoding.decode(segments[segments.length - 1]);
    }

    public ResponseContext getCategories(RequestContext request) {
        return new StreamWriterResponseContext(request.getAbdera()) {

            protected void writeTo(StreamWriter sw) throws IOException {
                sw.startDocument().startCategories(false).writeCategory("foo").writeCategory("bar").writeCategory("baz").endCategories().endDocument();
            }
        }.setStatus(200).setContentType(Constants.CAT_MEDIA_TYPE);
    }
}
