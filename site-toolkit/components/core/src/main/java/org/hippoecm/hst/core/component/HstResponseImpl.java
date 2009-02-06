package org.hippoecm.hst.core.component;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.hippoecm.hst.configuration.components.HstComponentConfiguration;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * Factory implementation for creating HTTP Response Wrappers
 * @version $Id$
 */
public class HstResponseImpl extends HttpServletResponseWrapper implements HstResponse
{
    protected HstRequestContext requestContext;
    protected HstComponentConfiguration componentConfiguration;
    protected String redirectLocation;
    
    public HstResponseImpl(HttpServletResponse response, HstRequestContext requestContext, HstComponentConfiguration componentConfiguration) {
        super(response);
        this.requestContext = requestContext;
        this.componentConfiguration = componentConfiguration;
    }

    public void setResponse(HttpServletResponse response) {
        super.setResponse(response);
    }
    
    public void sendRedirect(String redirectLocation) throws IOException {
        this.redirectLocation = redirectLocation;
    }
    
    public String getRedirectLocation() {
        return this.redirectLocation;
    }
}
