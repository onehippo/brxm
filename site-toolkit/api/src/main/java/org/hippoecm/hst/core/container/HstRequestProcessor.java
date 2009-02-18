package org.hippoecm.hst.core.container;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Request processor. This processor should be called by HstComponent dispatcher servlet.
 */
public interface HstRequestProcessor {
    
    void processRequest(ServletContext servletContext, ServletRequest servletRequest, ServletResponse servletResponse) throws ContainerException;

}
