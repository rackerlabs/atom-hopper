package org.atomhopper.adapter.request;

import org.apache.abdera.Abdera;
import org.atomhopper.util.uri.template.TemplateParameters;

import java.util.List;

/**
 * A client request contains only the bare minimum needed to express the request
 * contents; in this case by wrapping an Abdera RequestContext.
 */
public interface ClientRequest {

    String getTargetParameter(String parameter);
    
    String getRequestParameter(String parameter);

    List<String> getRequestParameters(String parameter);

    String urlFor(TemplateParameters param);
    
    Abdera getAbdera();
}
