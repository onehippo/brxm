/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.repository.testutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.io.FileUtils;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.util.NodeIterable;
import org.hippoecm.repository.util.PropertyIterable;
import org.hippoecm.repository.util.Utilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for writing tests against repository.
 *
 * Your unit test should follow the following pattern:
 *
        <code>
        public class SampleTest extends org.onehippo.repository.testutils.RepositoryTestCase {
            public void setUp() throws Exception {
                super.setUp();
                // your code here
            }
            public void tearDown() throws Exception {
                // your code here
                super.tearDown();
            }
        }
        </code>
 */
public abstract class RepositoryTestCase {

    /**
     * System property indicating whether to use the same repository server across all
     * test invocations. If this property is false or not present a new repository will be created
     * for every test. Sometimes this is unavoidable because you need a clean repository. If you don't
     * then your test performance will benefit greatly by setting this property.
     */
    private static final String KEEPSERVER_SYSPROP = "org.onehippo.repository.test.keepserver";

    protected static final String SYSTEMUSER_ID = "admin";
    protected static final char[] SYSTEMUSER_PASSWORD = "admin".toCharArray();

    protected static HippoRepository external = null;
    protected static HippoRepository background = null;

    private static final String defaultRepoPath;

    static {
        final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        final File storage = new File(tmpdir, "repository-" + UUID.randomUUID().toString());
        if (!storage.exists()) {
            storage.mkdir();
        }
        defaultRepoPath = storage.getAbsolutePath();
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected HippoRepository server = null;
    protected Session session = null;

    /**
     * Check whether repository content & configuration are the same before & after the test.
     */
    private int configurationHashcode;
    private Set<String> topLevelNodes;
    private String debugPath = "/hippo:configuration";
    private Map<String, Integer> hashes;
    private ByteArrayOutputStream debugStream;


    @BeforeClass
    public static void setUpClass() throws Exception {
        if (background == null && external == null) {
            if (System.getProperty("repo.path") == null) {
                System.setProperty("repo.path", defaultRepoPath);
            }
            clearRepository();
            background = HippoRepositoryFactory.getHippoRepository();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!Boolean.getBoolean(KEEPSERVER_SYSPROP)) {
            clearRepository();
        }
    }

    public static void clearRepository() {
        if (background != null) {
            background.close();
            background = null;
        }
        final File storage = new File(System.getProperty("repo.path", defaultRepoPath));
        String[] paths = new String[] { ".lock", "repository", "version", "workspaces" };
        for (final String path : paths) {
            FileUtils.deleteQuietly(new File(storage, path));
        }
    }

    public static void setRepository(HippoRepository repository) {
        external = repository;
    }

    @Before
    public void setUp() throws Exception {
        this.setUp(false);
    }

    protected void setUp(boolean clearRepository) throws Exception {
        if (clearRepository) {
            clearRepository();
        }
        if (background == null && external == null) {
            background = HippoRepositoryFactory.getHippoRepository();
        }
        if (external != null) {
            server = external;
        } else {
            server = background;
        }
        session = server.login(SYSTEMUSER_ID, SYSTEMUSER_PASSWORD);
        while (session.nodeExists("/test")) {
            session.getNode("/test").remove();
            session.save();
        }

        saveState();
    }

    @After
    public void tearDown() throws Exception {
        this.tearDown(false);
    }

    protected void tearDown(boolean clearRepository) throws Exception {
        if (session != null) {
            session.refresh(false);
            session.logout();
            session = null;
        }

        checkState();

        if (clearRepository) {
            clearRepository();
        }
    }

    /**
     * Sets the configuration change debugger path.
     * Retains
     * @param path
     */
    protected final void setConfigurationChangeDebugPath(String path) {
        this.debugPath = path;
    }


    private void saveState() throws RepositoryException {
        // save top-level nodes for tearDown validation
        topLevelNodes = new HashSet<String>();
        for (Node node : new NodeIterable(session.getRootNode().getNodes())) {
            topLevelNodes.add(node.getName() + "[" + node.getIndex() + "]");
        }

        configurationHashcode = hashCode(session.getNode("/hippo:configuration"));

        hashes = new HashMap<String, Integer>();
        final Node configChangeDebugNode = session.getNode(debugPath);
        for (Node configChild : new NodeIterable(configChangeDebugNode.getNodes())) {
            hashes.put(configChild.getName(), hashCode(configChild));
        }
        for (Property configProp : new PropertyIterable(configChangeDebugNode.getProperties())) {
            hashes.put(configProp.getName(), hashCode(configProp));
        }

        if (log.isDebugEnabled()) {
            debugStream = new ByteArrayOutputStream();
            Utilities.dump(new PrintStream(debugStream), configChangeDebugNode);
        }
    }

    private void checkState() throws Exception {
        Session cleanupSession = server.login(SYSTEMUSER_ID, SYSTEMUSER_PASSWORD);
        while (cleanupSession.nodeExists("/test")) {
            cleanupSession.getNode("/test").remove();
            cleanupSession.save();
        }

        int configHashcode = hashCode(cleanupSession.getNode("/hippo:configuration"));
        if (configHashcode != configurationHashcode) {
            Map<String, Integer> afterHash = new HashMap<String, Integer>();
            final Node configChangeDebugNode = cleanupSession.getNode(debugPath);
            for (Node configChild : new NodeIterable(configChangeDebugNode.getNodes())) {
                afterHash.put(configChild.getName(), hashCode(configChild));
            }
            for (Property configProp : new PropertyIterable(configChangeDebugNode.getProperties())) {
                afterHash.put(configProp.getName(), hashCode(configProp));
            }

            Set<String> missing = new HashSet<String>();
            Set<String> changed = new HashSet<String>();
            Set<String> added = new HashSet<String>();
            for (Map.Entry<String, Integer> oldHashEntry : hashes.entrySet()) {
                final String name = oldHashEntry.getKey();
                if (!afterHash.containsKey(name)) {
                    missing.add(name);
                } else if (!afterHash.get(name).equals(oldHashEntry.getValue())) {
                    changed.add(name);
                }
            }
            for (String name : afterHash.keySet()) {
                if (!hashes.containsKey(name)) {
                    added.add(name);
                }
            }

            if (log.isDebugEnabled()) {
                System.out.println("Before:");
                System.out.write(debugStream.toByteArray());

                System.out.println("After:");
                Utilities.dump(cleanupSession.getNode(debugPath));
            }

            throw new Exception("Configuration has been changed, but not reverted; make sure changes in tearDown overrides are saved.  " +
                    "Detected changes: added = " + added + ", changed = " + changed + ", removed = " + missing + ".  " +
                    "Use RepositoryTestCase#setConfigurationChangeDebugPath to narrow down.");
        }

        for (Node node : new NodeIterable(cleanupSession.getRootNode().getNodes())) {
            final boolean removed = topLevelNodes.remove(node.getName() + "[" + node.getIndex() + "]");
            if (!removed) {
                throw new Exception("tearDown found node with name '" + node.getName() + "' in session; this node should be removed in subclass");
            }
        }
        if (topLevelNodes.size() > 0) {
            throw new Exception("tearDown found nodes " + topLevelNodes + " missing");
        }

        cleanupSession.logout();
    }

    protected int hashCode(Node node) throws RepositoryException {
        if (excludeFromHashCode(node)) {
            return 0;
        }
        String name = node.getName();
        String type = node.getPrimaryNodeType().getName();
        int hashCode = name.hashCode() + type.hashCode() * 31;

        int propHash = 0;
        for (Property property : new PropertyIterable(node.getProperties())) {
            propHash = propHash + hashCode(property);
        }
        hashCode = 31 * hashCode + propHash;

        boolean orderable = node.getPrimaryNodeType().hasOrderableChildNodes();
        for (NodeType mixin : node.getMixinNodeTypes()) {
            if (mixin.hasOrderableChildNodes()) {
                orderable = true;
            }
        }
        int childHash = 0;
        for (Node child : new NodeIterable(node.getNodes())) {
            if (child instanceof HippoNode) {
                if (((HippoNode) child).isVirtual()) {
                    continue;
                }
            }
            if (orderable) {
                childHash = 31 * childHash + hashCode(child);
            } else {
                childHash = childHash + hashCode(child);
            }
        }
        hashCode = 31 * hashCode + childHash;
        return hashCode;
    }

    protected int hashCode(Property property) throws RepositoryException {
        int hashCode = property.getName().hashCode();
        if (property.isMultiple()) {
            for (Value value : property.getValues()) {
                hashCode = hashCode(value) + (hashCode * 31);
            }
        } else {
            hashCode = hashCode(property.getValue()) + (hashCode * 31);
        }
        return hashCode;
    }

    protected int hashCode(Value value) throws RepositoryException {
        return value.getString().hashCode();
    }

    private boolean excludeFromHashCode(final Node node) throws RepositoryException {
        return getExcludedFromHashCodePaths().contains(node.getPath());
    }

    protected Collection<String> getExcludedFromHashCodePaths() {
        return Collections.emptySet();
    }

    protected void build(Session session, String[] contents) throws RepositoryException {
        Node node = null;
        for (int i = 0; i < contents.length; i += 2) {
            if (contents[i].startsWith("/")) {
                String path = contents[i].substring(1);
                node = session.getRootNode();
                if (path.contains("/")) {
                    node = node.getNode(path.substring(0, path.lastIndexOf("/")));
                    path = path.substring(path.lastIndexOf("/") + 1);
                }
                node = node.addNode(path, contents[i + 1]);
            } else {
                PropertyDefinition propDef = null;
                PropertyDefinition[] propDefs = node.getPrimaryNodeType().getPropertyDefinitions();
                for (final PropertyDefinition pd : propDefs) {
                    if (pd.getName().equals(contents[i])) {
                        propDef = pd;
                        break;
                    }
                }
                if ("jcr:mixinTypes".equals(contents[i])) {
                    final String mixins = contents[i + 1];
                    for (String mixin : mixins.split(",")) {
                        node.addMixin(mixin);
                    }
                } else {
                    if (propDef != null && propDef.isMultiple()) {
                        Value[] values;
                        if (node.hasProperty(contents[i])) {
                            values = node.getProperty(contents[i]).getValues();
                            Value[] newValues = new Value[values.length + 1];
                            System.arraycopy(values, 0, newValues, 0, values.length);
                            values = newValues;
                        } else {
                            if (contents[i + 1] != null)
                                values = new Value[1];
                            else
                                values = new Value[0];
                        }
                        if (values.length > 0) {
                            if (propDef.getRequiredType() == PropertyType.REFERENCE) {
                                String uuid = session.getRootNode().getNode(contents[i + 1]).getIdentifier();
                                values[values.length - 1] = session.getValueFactory().createValue(uuid,
                                        PropertyType.REFERENCE);
                            } else {
                                values[values.length - 1] = session.getValueFactory().createValue(contents[i + 1]);
                            }
                        }
                        node.setProperty(contents[i], values);
                    } else {
                        if (propDef != null && propDef.getRequiredType() == PropertyType.REFERENCE) {
                            node.setProperty(
                                    contents[i],
                                    session.getValueFactory()
                                            .createValue(
                                                    session.getRootNode().getNode(contents[i + 1].substring(1))
                                                            .getIdentifier(), PropertyType.REFERENCE));
                        } else if ("hippo:docbase".equals(contents[i])) {
                            String docbase;
                            if (contents[i + 1].startsWith("/")) {
                                if (contents[i + 1].substring(1).equals("")) {
                                    docbase = session.getRootNode().getIdentifier();
                                } else {
                                    docbase = session.getRootNode().getNode(contents[i + 1].substring(1))
                                            .getIdentifier();
                                }
                            } else {
                                docbase = contents[i + 1];
                            }
                            node.setProperty(contents[i], session.getValueFactory().createValue(docbase),
                                    PropertyType.STRING);
                        } else {
                            node.setProperty(contents[i], contents[i + 1]);
                        }
                    }
                }
            }
        }
    }

    protected Node traverse(Session session, String path) throws RepositoryException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return ((HippoWorkspace) session.getWorkspace()).getHierarchyResolver().getNode(session.getRootNode(), path);
    }

}
