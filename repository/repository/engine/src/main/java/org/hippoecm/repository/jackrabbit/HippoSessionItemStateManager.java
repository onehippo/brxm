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
package org.hippoecm.repository.jackrabbit;

import javax.jcr.ReferentialIntegrityException;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.LocalItemStateManager;
import org.apache.jackrabbit.core.state.SessionItemStateManager;
import org.apache.jackrabbit.core.state.StaleItemStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HippoSessionItemStateManager extends SessionItemStateManager {
    protected final Logger log = LoggerFactory.getLogger(HippoSessionItemStateManager.class);

    HippoHierarchyManager wrappedHierMgr = null;
    HippoLocalItemStateManager localStateMgr;

    HippoSessionItemStateManager(NodeId rootNodeId, LocalItemStateManager manager, SessionImpl session) {
        super(rootNodeId, manager, session);
        this.localStateMgr = (HippoLocalItemStateManager) manager;
        if(wrappedHierMgr == null)
            wrappedHierMgr = new HippoHierarchyManager(this, super.getHierarchyMgr());
    }

    HippoSessionItemStateManager(NodeId rootNodeId, LocalItemStateManager manager, XASessionImpl session) {
        super(rootNodeId, manager, session);
        this.localStateMgr = (HippoLocalItemStateManager) manager;
        if(wrappedHierMgr == null)
            wrappedHierMgr = new HippoHierarchyManager(this, super.getHierarchyMgr());
    }

    public void disposeAllTransientItemStates() {
        /* It is imperative that the stateMgr.refresh() method is ONLY called after a 
         * super.disposeAllTransientItemStates().  This is the only way to guarantee
         * that there are in fact no changes in the changelog of the local ISM.
         */
        super.disposeAllTransientItemStates();
        try {
            edit();
            localStateMgr.refresh();
        } catch(ReferentialIntegrityException ex) {
            // cannot occur
        } catch(StaleItemStateException ex) {
            // cannot occur
        } catch(ItemStateException ex) {
            // cannot occur
        }
    }


    @Override
    public HierarchyManager getHierarchyMgr() {
        if(wrappedHierMgr == null)
            wrappedHierMgr = new HippoHierarchyManager(this, super.getHierarchyMgr());
        return wrappedHierMgr;
    }

    @Override
    public HierarchyManager getAtticAwareHierarchyMgr() {
        return new HippoHierarchyManager(this, super.getAtticAwareHierarchyMgr());
    }

    @Override
    public void stateDiscarded(ItemState discarded) {
        super.stateDiscarded(discarded);
    }
}
