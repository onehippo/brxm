/*
 *  Copyright 2008 Hippo.
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
 */
package org.hippoecm.hst.core.component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

import org.w3c.dom.Element;

/**
 * Temporarily holds the current state of a HST response
 */
public interface HstResponseState
{
    
    boolean isActionResponse();

    boolean isRenderResponse();

    boolean isResourceResponse();

    boolean isMimeResponse();
    
    boolean isStateAwareResponse();

    void addCookie(Cookie cookie);

    void addDateHeader(String name, long date);

    void addHeader(String name, String value);

    void addIntHeader(String name, int value);

    boolean containsHeader(String name);

    void sendError(int errorCode, String errorMessage) throws IOException;

    void sendError(int errorCode) throws IOException;

    void sendRedirect(String redirectLocation) throws IOException;

    String getRedirectLocation();

    void setDateHeader(String name, long date);

    void setHeader(String name, String value);

    void setIntHeader(String name, int value);

    void setStatus(int statusCode, String message);

    void setStatus(int statusCode);

    void flushBuffer() throws IOException;

    int getBufferSize();

    String getCharacterEncoding();

    String getContentType();

    Locale getLocale();

    ServletOutputStream getOutputStream() throws IOException;

    PrintWriter getWriter() throws IOException;

    boolean isCommitted();

    void reset();

    void resetBuffer();

    void setBufferSize(int size);

    void setCharacterEncoding(String charset);

    void setContentLength(int len);

    void setContentType(String type);

    void setLocale(Locale locale);

    void addHeadElement(Element element, String keyHint);
    
    List<Element> getHeadElements();
    
    boolean containsHeadElement(String keyHint);

    void clear();

    void flush() throws IOException;

}
