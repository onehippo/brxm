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
package org.hippoecm.hst.core.linking;

import javax.jcr.Node;

import org.hippoecm.hst.configuration.HstSite;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.service.Service;

public interface HstLinkCreator {
   
   
    HstLink create(Service service, ResolvedSiteMapItem resolvedSiteMapItem);
    
    /**
     * Rewrite a jcr Node to a HstLink wrt its current HstSiteMapItem
     * @param node
     * @param siteMapItem
     * @return HstLink 
     */
    HstLink create(Node node, ResolvedSiteMapItem resolvedSiteMapItem);
    
    /**
     * For creating a link from a HstSiteMapItem to a HstSiteMapItem with toSiteMapItemId within the same Site
     * @param toSiteMapItemId
     * @param currentSiteMapItem
     * @return HstLink
     */
    HstLink create(String toSiteMapItemId, ResolvedSiteMapItem resolvedSiteMapItem);
    
    /**
     * Regardless the current context, create a HstLink to the HstSiteMapItem that you use as argument. This is only possible if the sitemap item does not
     * contain any ancestor with a wildcard
     * @param toHstSiteMapItem
     * @return HstLink
     */
    HstLink create(HstSiteMapItem toHstSiteMapItem);
    
    
    /**
     * create a link to siteMapItem of hstSite. This is only possible if the sitemap item does not
     * contain any ancestor with a wildcard
     * @param hstSite
     * @param toSiteMapItemId
     * @return HstLink
     */
    HstLink create(HstSite hstSite, String toSiteMapItemId);
}
