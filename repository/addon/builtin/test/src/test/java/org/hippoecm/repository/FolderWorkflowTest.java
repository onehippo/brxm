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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.standardworkflow.FolderWorkflow;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FolderWorkflowTest extends TestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    Node root;
    
    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        root = session.getRootNode();
        if(root.hasNode("test"))
            root.getNode("test").remove();
        root = root.addNode("test");
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        root = session.getRootNode();
        if(root.hasNode("test"))
            root.getNode("test").remove();
        super.tearDown();
    }

    @Test
    public void testFolder() throws RepositoryException, WorkflowException, RemoteException {
        Node node = root.addNode("f","hippostd:folder");
        node.addMixin("hippo:harddocument");
        session.save();
        WorkflowManager manager = ((HippoWorkspace)session.getWorkspace()).getWorkflowManager();
        FolderWorkflow workflow = (FolderWorkflow) manager.getWorkflow("internal", node);
        assertNotNull(workflow);
        Map<String,Set<String>> types = workflow.list();
        assertNotNull(types);
        assertTrue(types.containsKey("New Folder"));
        assertTrue(types.get("New Folder").contains("orderable folder"));
        String path = workflow.add("New Folder", "orderable folder", "d");
        assertNotNull(path);
        node = session.getRootNode().getNode(path.substring(1));
        assertEquals("/test/f/d",node.getPath());
        assertTrue(node.isNodeType("hippostd:folder"));
    }
    
    @Test
    public void testDirectory() throws RepositoryException, WorkflowException, RemoteException {
        Node node = root.addNode("f","hippostd:directory");
        node.addMixin("hippo:harddocument");
        session.save();
        WorkflowManager manager = ((HippoWorkspace)session.getWorkspace()).getWorkflowManager();
        FolderWorkflow workflow = (FolderWorkflow) manager.getWorkflow("internal", node);
        assertNotNull(workflow);
        Map<String,Set<String>> types = workflow.list();
        assertNotNull(types);
        assertTrue(types.containsKey("New Folder"));
        assertTrue(types.get("New Folder").contains("simple folder"));
        String path = workflow.add("New Folder", "simple folder", "d");
        assertNotNull(path);
        node = session.getRootNode().getNode(path.substring(1));
        assertEquals("/test/f/d",node.getPath());
        assertTrue(node.isNodeType("hippostd:directory"));
    }

    @Test
    @Ignore
    public void testTemplateDocument() throws RepositoryException, WorkflowException, RemoteException {
        Node node = root.addNode("f","hippostd:folder");
        node.addMixin("hippo:harddocument");
        session.save();
        WorkflowManager manager = ((HippoWorkspace)session.getWorkspace()).getWorkflowManager();
        FolderWorkflow workflow = (FolderWorkflow) manager.getWorkflow("internal", node);
        assertNotNull(workflow);
        Map<String,String[]> renames = new TreeMap<String,String[]>();
        Map<String,Set<String>> types = workflow.list();
        assertNotNull(types);
        assertTrue(types.containsKey("simple"));
        assertTrue(types.get("simple").contains("document"));
        String path = workflow.add("simple", "document", "d");
        assertNotNull(path);
        node = session.getRootNode().getNode(path.substring(1));
        assertEquals("/test/f/d",node.getPath());
        assertTrue(node.isNodeType("hippo:handle"));
        assertTrue(node.hasNode(node.getName()));
        assertTrue(node.getNode(node.getName()).isNodeType("hippostd:document"));
    }
    
    @Test
    public void testReorderFolder() throws RepositoryException, WorkflowException, RemoteException {
        Node node = root.addNode("f","hippostd:folder");
        node.addMixin("hippo:harddocument");
        node.addNode("aap");
        node.addNode("noot");
        node.addNode("mies");
        node.addNode("zorro");
        node.addNode("foo");
        node.addNode("bar");
        session.save();

        NodeIterator it = node.getNodes();
        assertEquals("aap", it.nextNode().getName());
        assertEquals("noot", it.nextNode().getName());
        assertEquals("mies", it.nextNode().getName());
        assertEquals("zorro", it.nextNode().getName());
        assertEquals("foo", it.nextNode().getName());
        assertEquals("bar", it.nextNode().getName());
        
        WorkflowManager manager = ((HippoWorkspace)session.getWorkspace()).getWorkflowManager();
        FolderWorkflow workflow = (FolderWorkflow) manager.getWorkflow("internal", node);        

        /*
         * aap      aap
         * noot     bar
         * mies     foo
         * zorro => mies
         * foo      noot
         * bar      zorro
         */
        LinkedHashMap<String, String> mapping = new LinkedHashMap<String, String>();
        mapping.put("aap", "bar");
        mapping.put("bar", "foo");
        mapping.put("foo", "mies");
        mapping.put("mies", "noot");
        mapping.put("noot", "zorro");
        mapping.put("zorro", null);
          
        workflow.reorder(mapping);
        it = node.getNodes();
        assertEquals("aap", it.nextNode().getName());
        assertEquals("bar", it.nextNode().getName());
        assertEquals("foo", it.nextNode().getName());
        assertEquals("mies", it.nextNode().getName());
        assertEquals("noot", it.nextNode().getName());
        assertEquals("zorro", it.nextNode().getName());

    }

    /* The following two tests can only be executed if repository is run
     * locally, and the method copy in FolderWorkflowImpl is made public,
     * which is shouldn't be.  They where used for development purposes,
     * mainly.

    @Test
    public void testCopyFolder() throws RepositoryException, RemoteException {
        FolderWorkflowImpl workflow;
        workflow = new FolderWorkflowImpl(session, session, session.getRootNode().getNode(
                                "hippo:configuration/hippo:queries/hippo:templates/folder/hippostd:templates/document folder"));
        assertFalse(session.getRootNode().getNode("test").hasNode("folder"));
        TreeMap<String,String[]> renames = new TreeMap<String,String[]>();
        renames.put("./_name", new String[] { "f" });
        workflow.copy(session.getRootNode().getNode(
                                "hippo:configuration/hippo:queries/hippo:templates/folder/hippostd:templates/document folder"),
                                session.getRootNode().getNode("test"), renames, ".");
        assertTrue(session.getRootNode().getNode("test").hasNode("f"));
    }

    @Test
    public void testCopyDocument() throws RepositoryException, RemoteException {
        FolderWorkflowImpl workflow;
        workflow = new FolderWorkflowImpl(session, session, session.getRootNode().getNode(
                                                                 "hippo:configuration/hippo:queries/hippo:templates/document"));
        assertFalse(session.getRootNode().getNode("test").hasNode("document"));
        assertFalse(session.getRootNode().getNode("test").hasNode("d"));
        TreeMap<String,String[]> renames = new TreeMap<String,String[]>();
        renames.put("./_name", new String[] { "d" });
        renames.put("./_node/_name", new String[] { "d" });
        System.err.println("\n\n\n\n\n\n\n\n");
        workflow.copy(session.getRootNode().getNode(
                                       "hippo:configuration/hippo:queries/hippo:templates/simple/hippostd:templates/document"),
                                       session.getRootNode().getNode("test"), renames, ".");
        assertTrue(session.getRootNode().getNode("test").hasNode("d"));
        assertTrue(session.getRootNode().getNode("test").getNode("d").hasNode("d"));
    }

    */
}
