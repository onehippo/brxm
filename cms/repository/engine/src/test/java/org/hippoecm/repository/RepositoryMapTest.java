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
package org.hippoecm.repository;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hippoecm.repository.api.ISO9075Helper;
import org.hippoecm.repository.api.RepositoryMap;


public class RepositoryMapTest extends TestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private Node root;

    String[] content = new String[] {
        "/content", "nt:unstructured",
        "/content/articles", "hippo:testdocument",
        "jcr:mixinTypes", "hippo:harddocument",
        "/content/articles/myarticle1", "hippo:handle",
        "jcr:mixinTypes", "hippo:hardhandle",
        "/content/articles/myarticle1/myarticle1", "hippo:testdocument",
        "jcr:mixinTypes", "hippo:harddocument"
    };

    public void setUp() throws Exception {
        super.setUp();
        root = session.getRootNode();
        if(root.hasNode("test"))
            root = root.getNode("test");
        else
            root = root.addNode("test");

        root = session.getRootNode();
        if(root.hasNode("content")) {
            root.getNode("content").remove();
            root.save();
        }
        build(session, content);
        root = root.getNode("content");
        session.save();
    }

    @Test public void testMap() throws Exception {
        Utilities.dump(System.err, session.getRootNode().getNode("hippo:configuration/hippo:documents"));
        Map map = server.getRepositoryMap(session.getRootNode().getNode("hippo:configuration/hippo:documents"));
        System.err.println("\n\n\n\n\n");
        map = (Map) map.get("embedded");
        assertNotNull(map);
        System.err.println(((Map)map.get(root.getNode("articles/myarticle1/myarticle1").getUUID())).get("_path"));
        map = (Map) map.get(root.getNode("articles/myarticle1/myarticle1").getUUID());
        System.err.println(map.get("_path"));
    }
}
