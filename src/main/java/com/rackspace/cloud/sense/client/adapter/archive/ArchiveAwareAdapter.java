package com.rackspace.cloud.sense.client.adapter.archive;

import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import java.util.Date;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public interface ArchiveAwareAdapter {

    GenericAdapterResponse<Feed> getArchivedFeed(RequestContext request, Date date);

    void archiveFeed(Date date, Feed copy);
}
