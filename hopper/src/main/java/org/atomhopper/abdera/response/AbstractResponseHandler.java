package net.jps.atom.hopper.abdera.response;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.abdera.filter.AdapterResponseProcessor;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

    private final List<AdapterResponseProcessor<T>> responseProcessors;

    public AbstractResponseHandler() {
        responseProcessors = new LinkedList<AdapterResponseProcessor<T>>();
    }
    
    public AbstractResponseHandler(AdapterResponseProcessor<T>... processors) {
        responseProcessors = Arrays.asList(processors);
        
    }
    
    protected void processResponse(RequestContext rc, AdapterResponse<T> adapterResponse) {
        for (AdapterResponseProcessor<T> processor : getResponseProcessors()) {
            processor.process(rc, adapterResponse);
        }
    }
    
    protected List<AdapterResponseProcessor<T>> getResponseProcessors() {
        return responseProcessors;
    }
}
