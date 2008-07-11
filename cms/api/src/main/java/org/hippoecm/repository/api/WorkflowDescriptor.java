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
package org.hippoecm.repository.api;

import javax.jcr.RepositoryException;

public interface WorkflowDescriptor {
    final static String SVN_ID = "$Id$";

    /**
     * Obtain the human-interpretable display name of this workflow.
     *
     * @returns A description of the workflow
     */
    public String getDisplayName() throws RepositoryException;

    /**
     * Method to access extra information that might be associated with this workflow.
     * An example is the plugin class name to be used by a frontend application to access the workflow.
     *
     * @returns A String value, can be null.
     */
    public String getValue(String name) throws RepositoryException;
}