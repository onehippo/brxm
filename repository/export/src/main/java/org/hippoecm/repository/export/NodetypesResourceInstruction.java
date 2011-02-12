/*
 *  Copyright 2011 Hippo (www.hippo.nl).
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
package org.hippoecm.repository.export;

import static org.hippoecm.repository.export.Constants.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.util.JcrCompactNodeTypeDefWriter;


class NodetypesResourceInstruction extends ResourceInstruction implements NamespaceInstruction {
	
	private String m_internalPrefix;
	private final String m_prefix;
	private String m_namespace;
	private final String m_namespaceroot;
	private Element m_namespacePropertyValue;
	
	NodetypesResourceInstruction(String name, Double sequence, File file, String namespace, Element namespacePropertyValue, String internalPrefix) {
		super(name, sequence, file);
		if (!m_file.exists()) {
			m_changed = true;
		}
		m_internalPrefix = internalPrefix;
		int indexOfUnderscore = internalPrefix.indexOf('_');
		m_prefix = (indexOfUnderscore == -1) ? internalPrefix : internalPrefix.substring(0, indexOfUnderscore);
		if (namespace != null) {
			m_namespace = namespace;
			int lastIndexOfPathSeparator = namespace.lastIndexOf('/');
			m_namespaceroot = (lastIndexOfPathSeparator == -1) ? namespace : namespace.substring(0, lastIndexOfPathSeparator);
		}
		else {
			m_namespace = null;
			m_namespaceroot = null;
		}
		m_namespacePropertyValue = namespacePropertyValue;
	}

	@Override
	void export(Session session) {
		log.info("Exporting to " + m_file.getName());
		try {
			if (!m_file.exists()) m_file.createNewFile();
			Writer out = new FileWriter(m_file);
			try {
				String cnd = null;
				try {
					log.debug("Trying to export cnd for internal prefix " + m_internalPrefix);
					cnd = JcrCompactNodeTypeDefWriter.compactNodeTypeDef(session.getWorkspace(), m_internalPrefix);
					// HACK: we only get events for /jcr:system/jcr:nodeTypes/example_1_1 instead
					// of for /jcr:system/jcr:nodeTypes/example
					// here we fix that prefix
					cnd = cnd.replaceAll(m_internalPrefix, m_prefix);
				} 
				catch (NamespaceException e) {
					log.debug("Failed. Now trying regular prefix " + m_prefix);
					// update all content was already finished, we can use regular prefix
					// but we need to first get a fresh session because the old session
					// does not seem to pick up the last step in update all content
					session = ((HippoSession) session).impersonate(new SimpleCredentials("system", new char[]{}));
					try {
						cnd = JcrCompactNodeTypeDefWriter.compactNodeTypeDef(session.getWorkspace(), m_prefix);
					}
					finally {
						session.logout();
					}
				}
				out.write(cnd);
				out.flush();
			}
			finally {
				try {
					out.close();
				}
				catch (IOException e) {}
			}
		}
		catch (IOException e) {
    		log.error("Exporting " + m_file.getName() + " failed.", e);
		}
		catch (RepositoryException e) {
    		log.error("Exporting " + m_file.getName() + " failed.", e);
		}
		m_changed = false;
	}

	@Override
	public Element createInstructionElement() {
        Element element = createBaseInstructionElement();
        // create element:
        // <sv:property sv:name="hippo:nodetypesresource" sv:type="String">
        //   <sv:value>{this.m_file.getName()}</sv:value>
        // </sv:property>
        Element cndProperty = DocumentFactory.getInstance().createElement(PROPERTY_QNAME);
        cndProperty.add(DocumentFactory.getInstance().createAttribute(cndProperty, NAME_QNAME, "hippo:nodetypesresource"));
        cndProperty.add(DocumentFactory.getInstance().createAttribute(cndProperty, TYPE_QNAME, "String"));
        Element cndPropertyValue = DocumentFactory.getInstance().createElement(VALUE_QNAME);
        cndPropertyValue.setText(m_file.getName());
        cndProperty.add(cndPropertyValue);
        element.add(cndProperty);
        
        if (m_namespace != null) {
            // create element:
            // <sv:property sv:name="hippo:namespace" sv:type="String">
            //   <sv:value>{this.m_namespace}</sv:value>
            // </sv:property>
            Element namespaceProperty = DocumentFactory.getInstance().createElement(PROPERTY_QNAME);
            namespaceProperty.add(DocumentFactory.getInstance().createAttribute(namespaceProperty, NAME_QNAME, "hippo:namespace"));
            namespaceProperty.add(DocumentFactory.getInstance().createAttribute(namespaceProperty, TYPE_QNAME, "String"));
            Element namespacePropertyValue = DocumentFactory.getInstance().createElement(VALUE_QNAME);
            namespacePropertyValue.setText(m_namespace);
            namespaceProperty.add(namespacePropertyValue);
            m_namespacePropertyValue = namespacePropertyValue;
            element.add(namespaceProperty);
        }
        
        return element;
	}
	
	@Override
    void nodeAdded(String path) {
    	setInternalPrefixFromPath(path);
    	m_changed = true;
    }
    
	@Override
    boolean nodeRemoved(String path) {
    	setInternalPrefixFromPath(path);
    	m_changed = true;
    	// TODO: should determine whether or not context was removed
    	return false;
    }
    
    /* Don't think this can happen on a node type node */
	@Override
    void propertyAdded(String path) {
    	setInternalPrefixFromPath(path);
    	m_changed = true;
    }
    
    /* Don't think this can happen on a node type node */
	@Override
    void propertyChanged(String path) {
    	setInternalPrefixFromPath(path);
    	m_changed = true;
    }
    
    /* Don't think this can happen on a node type node */
	@Override
    void propertyRemoved(String path) {
    	setInternalPrefixFromPath(path);
    	m_changed = true;
    }
    
    private void setInternalPrefixFromPath(String path) {
    	// path = /jcr:system/jcr:nodeTypes/example_1_2:doctype/jcr:propertyDefinition
    	// relPath = example_1_2:doctype/jcr:propertyDefinition
    	String relPath = path.substring("/jcr:system/jcr:nodeTypes/".length());
    	// nodeTypeRoot = example_1_2:doctype
    	int indexOfPathSeparator = relPath.indexOf('/');
    	String nodeTypeRoot = (indexOfPathSeparator == -1) ? relPath : relPath.substring(0, indexOfPathSeparator);
    	// internalPrefix = example_1_2
    	int indexOfColon = nodeTypeRoot.indexOf(':');
    	m_internalPrefix = (indexOfColon == -1) ? nodeTypeRoot : nodeTypeRoot.substring(0, indexOfColon);
    }
    
	boolean matchesPrefix(String internalPrefix) {
		// internalPrefix = example_1_2
		// prefix = example
		int indexOfUnderscore = internalPrefix.indexOf('_');
		String prefix = (indexOfUnderscore == -1) ? internalPrefix : internalPrefix.substring(0, indexOfUnderscore);
		return prefix.equals(m_prefix);
	}
	
	@Override
	public String toString() {
		return "NodetypesResourceInstruction[prefix=" + m_prefix + "]"; 
	}

	@Override
	public boolean matchesNamespace(String namespace) {
		if (m_namespace == null) return false;
		int lastIndexOfPathSeparator = namespace.lastIndexOf('/');
		String namespaceroot = (lastIndexOfPathSeparator == -1) ? namespace : namespace.substring(0, lastIndexOfPathSeparator);
		return namespaceroot.equals(m_namespaceroot);
	}
	
	public void updateNamespace(String namespace) {
		m_namespace = namespace;
		m_namespacePropertyValue.setText(namespace);
	}
}