//FIXME

/*
 * Copyright 2007 Hippo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hippoecm.repository.api.ISO9075Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectAccessServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final Logger logger = LoggerFactory.getLogger(DirectAccessServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getRequestURI();
        if (path.startsWith(req.getContextPath())) {
            path = path.substring(req.getContextPath().length());
        }
        if (path.startsWith(req.getServletPath())) {
            path = path.substring(req.getServletPath().length());
        }

        path = URLDecoder.decode(path, "UTF-8");

        String currentPath = "";
        StringTokenizer pathElts = new StringTokenizer(path, "/");
        while (pathElts.hasMoreTokens()) {
            String pathElt = pathElts.nextToken();
            currentPath += "/" + ISO9075Helper.decodeLocalName(pathElt);
        }
        path = currentPath;

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        try {
            Session session = JCRConnector.getJCRSession(req.getSession());
            Item item = JCRConnector.getItem(session, path);
            if (item == null) {
                logger.warn(path + "not found, response status = 404)");
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!item.isNode()) {
                res.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                return;
            }
            Node node = (Node) item;

            String mimeType = node.getProperty("mime-type").getString();
            InputStream istream = node.getProperty("data").getStream();

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType(mimeType);

            OutputStream ostream = res.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = istream.read(buffer)) >= 0) {
                ostream.write(buffer, 0, len);
            }
        } catch (RepositoryException ex) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
