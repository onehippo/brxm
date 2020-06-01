/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.content.beans.query.builder;


import javax.jcr.Session;

import org.hippoecm.hst.content.beans.query.exceptions.FilterException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.repository.util.DateTools;

public abstract class Constraint {

    private boolean negated;

    /**
     * Negates the current Constraint
     */
    public Constraint negate() {
        this.negated = !negated;
        return this;
    }

    boolean isNegated() {
        return negated;
    }

    final Filter build(final Session session, final DateTools.Resolution defaultResolution) throws FilterException {
        Filter filter = doBuild(session, defaultResolution);

        if (filter != null && negated) {
            filter.negate();
        }

        return filter;
    }

    protected abstract Filter doBuild(final Session session, final DateTools.Resolution defaultResolution) throws FilterException;
}
