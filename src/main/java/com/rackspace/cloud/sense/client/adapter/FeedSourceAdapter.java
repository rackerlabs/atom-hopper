/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EntryResponse;
import com.rackspace.cloud.sense.domain.response.FeedResponse;
import com.rackspace.cloud.sense.domain.entry.GetEntryRequest;
import com.rackspace.cloud.sense.domain.entry.PostEntryRequest;
import com.rackspace.cloud.sense.domain.entry.PutEntryRequest;
import com.rackspace.cloud.sense.domain.feed.GetFeedRequest;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public interface FeedSourceAdapter {

    FeedResponse getFeed(GetFeedRequest feedInfo, Feed copy);

    EntryResponse getEntry(GetEntryRequest entryInfo, Entry copy);

    EntryResponse postEntry(PostEntryRequest postRequest);

    EntryResponse putEntry(PutEntryRequest putRequest);
}
