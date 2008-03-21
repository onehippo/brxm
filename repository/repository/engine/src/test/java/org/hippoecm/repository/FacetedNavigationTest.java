/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository;

import java.io.IOException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;

public class FacetedNavigationTest extends FacetedNavigationAbstractTest {

    private static final String SYSTEMUSER_ID = "admin";
    private static final char[] SYSTEMUSER_PASSWORD = "admin".toCharArray();

    public void testTraversal() throws RepositoryException, IOException {
        Node node = commonStart();
        traverse(node); // for a full verbose dump use: Utilities.dump(root);
        commonEnd();
    }

    public void testCounts() throws RepositoryException, IOException {
        numDocs = 500;
        commonStart();
        check("/navigation/xyz/x1", 1, 0, 0);
        check("/navigation/xyz/x2", 2, 0, 0);
        check("/navigation/xyz/x1/y1", 1, 1, 0);
        check("/navigation/xyz/x1/y2", 1, 2, 0);
        check("/navigation/xyz/x2/y1", 2, 1, 0);
        check("/navigation/xyz/x2/y2", 2, 2, 0);
        check("/navigation/xyz/x1/y1/z1", 1, 1, 1);
        check("/navigation/xyz/x1/y1/z2", 1, 1, 2);
        check("/navigation/xyz/x1/y2/z1", 1, 2, 1);
        check("/navigation/xyz/x1/y2/z2", 1, 2, 2);
        check("/navigation/xyz/x2/y1/z1", 2, 1, 1);
        check("/navigation/xyz/x2/y1/z2", 2, 1, 2);
        check("/navigation/xyz/x2/y2/z1", 2, 2, 1);
        check("/navigation/xyz/x2/y2/z2", 2, 2, 2);
        commonEnd();
    }

    public void testGetItemFromSession() throws RepositoryException {
        commonStart();

        String basePath = "/navigation/xyz/x1/y1/z2";
        Item item = session.getItem(basePath);
        assertNotNull(item);
        assertTrue(item instanceof Node);
        Node baseNode = (Node)item;

        Node resultSetNode_1 = baseNode.getNode(HippoNodeType.HIPPO_RESULTSET);
        assertNotNull(resultSetNode_1);

        Node resultSetNode_2 = (Node)session.getItem(basePath + "/" + HippoNodeType.HIPPO_RESULTSET);
        assertNotNull(resultSetNode_2);

        commonEnd();
    }

    public void testGetItemFromNode() throws RepositoryException {
        commonStart();

        String basePath = "/navigation/xyz/x1/y1/z2";
        Item item = session.getItem(basePath);
        assertNotNull(item);
        assertTrue(item instanceof Node);
        Node baseNode = (Node)item;

        Node resultSetNode_1 = baseNode.getNode(HippoNodeType.HIPPO_RESULTSET);
        assertNotNull(resultSetNode_1);

        Node resultSetNode_2 = (Node)session.getItem(basePath + "/" + HippoNodeType.HIPPO_RESULTSET);
        assertNotNull(resultSetNode_2);

        commonEnd();
    }

    public void testVirtualNodeHasNoJcrUUID() throws RepositoryException {
        commonStart();

        Node node = session.getRootNode().getNode("navigation").getNode("xyz").getNode("x1").getNode("y1").getNode("z2");
        node = node.getNode(HippoNodeType.HIPPO_RESULTSET);

        // deliberate while loop to force that we have at least one child node to traverse
        NodeIterator iter = node.getNodes();
        do {
            node = iter.nextNode();
            assertFalse(node.hasProperty("jcr:uuid"));
            assertTrue(node.hasProperty("hippo:uuid"));
            /* FIXME: enable these for checks for HREPTWO-283
             *  assertFalse(node.isNodeType("mix:referenceable"));
             *  assertTrue(node.isNodeType("hippo:referenceable"));
             */
        } while(iter.hasNext());

        commonEnd();
    }

    public void testAddingNodesOpenFacetSearch() throws RepositoryException {
        commonStart();

        Node node, child, searchNode = session.getRootNode().getNode("navigation").getNode("xyz");
        traverse(searchNode);

        assertFalse(searchNode.getNode("x1").hasNode("yy"));
        session.refresh(false);
        session.save();

        node = session.getRootNode().getNode("documents");
        child = node.addNode("test", "hippo:realdocument");
        child.setProperty("x", "x1");
        child.setProperty("y", "yy");
        node.save();

        searchNode = session.getRootNode().getNode("navigation").getNode("xyz");
        assertTrue(searchNode.getNode("x1").hasNode("yy"));
        assertTrue(searchNode.getNode("x1").getNode("yy").hasNode(HippoNodeType.HIPPO_RESULTSET));
        assertTrue(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test"));
        assertFalse(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test[2]"));

        node = session.getRootNode().getNode("documents");
        child = node.addNode("test", "hippo:realdocument");
        child.setProperty("x", "x1");
        child.setProperty("y", "yy");
        session.save();

        searchNode = session.getRootNode().getNode("navigation").getNode("xyz");
        assertTrue(searchNode.getNode("x1").hasNode("yy"));
        assertTrue(searchNode.getNode("x1").getNode("yy").hasNode(HippoNodeType.HIPPO_RESULTSET));
        assertTrue(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test"));
        assertTrue(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test[2]"));

        session.getRootNode().getNode("documents").getNode("test").setProperty("y","zz");
        session.save();
        session.refresh(false);

        searchNode = session.getRootNode().getNode("navigation").getNode("xyz");
        assertTrue(searchNode.getNode("x1").hasNode("yy"));
        assertTrue(searchNode.getNode("x1").getNode("yy").hasNode(HippoNodeType.HIPPO_RESULTSET));
        assertTrue(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test"));
        assertFalse(searchNode.getNode("x1").getNode("yy").getNode(HippoNodeType.HIPPO_RESULTSET).hasNode("test[2]"));
        assertTrue(searchNode.getNode("x1").getNode("zz").hasNode(HippoNodeType.HIPPO_RESULTSET));

        session.logout();
        session = server.login(SYSTEMUSER_ID, SYSTEMUSER_PASSWORD);

        commonEnd();
    }

}
