/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 */
package com.onehippo.cms7.crisp.demo.components;

import static com.onehippo.cms7.crisp.demo.Constants.RESOURCE_SPACE_DEMO_PRODUCT_CATALOG;
import static com.onehippo.cms7.crisp.demo.Constants.RESOURCE_SPACE_DEMO_PRODUCT_CATALOG_XML;

import java.util.HashMap;
import java.util.Map;

import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.essentials.components.EssentialsContentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import com.onehippo.cms7.crisp.api.resource.Resource;
import com.onehippo.cms7.crisp.demo.beans.NewsDocument;
import com.onehippo.cms7.crisp.hst.module.CrispHstServices;

public class NewsContentComponent extends EssentialsContentComponent {

    private static Logger log = LoggerFactory.getLogger(NewsContentComponent.class);

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) {
        super.doBeforeRender(request, response);

        NewsDocument document = (NewsDocument) request.getRequestContext().getContentBean();

        log.warn("\n\n===============================================================================\n"
                + "  [INFO] JSON based Resource Retrieval DEMO\n"
                + "===============================================================================\n\n");
        Resource productCatalogs = findProductCatalogs(document);

        if (productCatalogs != null) {
            request.setAttribute("productCatalogs", productCatalogs);
        }

        log.warn("\n\n===============================================================================\n"
                + "  [INFO] XML based Resource Retrieval DEMO\n"
                + "===============================================================================\n\n");
        Resource productCatalogsXml = findProductCatalogsXml(document);

        if (productCatalogsXml != null) {
            request.setAttribute("productCatalogsXml", productCatalogsXml);
        }

    }

    private Resource findProductCatalogs(final NewsDocument document) {
        Resource productCatalogs = null;

        try {
            ResourceServiceBroker resourceServiceBroker = CrispHstServices.getDefaultResourceServiceBroker();
            final Map<String, Object> pathVars = new HashMap<>();
            // Note: Just as an example, let's try to find all the data by passing empty query string.
            pathVars.put("fullTextSearchTerm", "");
            productCatalogs = resourceServiceBroker.findResources(RESOURCE_SPACE_DEMO_PRODUCT_CATALOG,
                    "/products/?q={fullTextSearchTerm}", pathVars);
        } catch (Exception e) {
            log.warn("Failed to find resources from '{}{}' resource space for full text search term, '{}'.",
                    RESOURCE_SPACE_DEMO_PRODUCT_CATALOG, "/products/", document.getTitle(), e);
        }

        return productCatalogs;
    }

    private Resource findProductCatalogsXml(final NewsDocument document) {
        Resource productCatalogs = null;

        try {
            ResourceServiceBroker resourceServiceBroker = CrispHstServices.getDefaultResourceServiceBroker();
            final Map<String, Object> pathVars = new HashMap<>();
            // Note: Just as an example, let's try to find all the data by passing empty query string.
            pathVars.put("fullTextSearchTerm", "");
            productCatalogs = resourceServiceBroker.findResources(RESOURCE_SPACE_DEMO_PRODUCT_CATALOG_XML,
                    "/products.xml?q={fullTextSearchTerm}", pathVars);
        } catch (Exception e) {
            log.warn("Failed to find resources from '{}{}' resource space for full text search term, '{}'.",
                    RESOURCE_SPACE_DEMO_PRODUCT_CATALOG, "/products.xml", document.getTitle(), e);
        }

        return productCatalogs;
    }
}
