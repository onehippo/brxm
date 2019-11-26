/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.hippoecm.frontend.service;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.tester.WicketTester;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WicketFaviconServiceImplTest {

    public static final String WICKET_FAVICON_PREFIX = "./wicket/resource/org.hippoecm.frontend.service.WicketFaviconServiceImpl/";

    // Needed for RequestCycle.get()
    @SuppressWarnings("FieldCanBeLocal")
    private WicketTester wicketTester;

    @Before
    public void setUp() throws Exception {
        wicketTester  = new WicketTester();
    }

    @Test
    public void getRelativeFaviconUrlCms() {
        FaviconService faviconService = new WicketFaviconServiceImpl("cms");
        assertEquals(WICKET_FAVICON_PREFIX +
                "cms-icon.png",faviconService.getRelativeFaviconUrl());
    }

    @Test
    public void getRelativeFaviconUrlConsole() {
        FaviconService faviconService = new WicketFaviconServiceImpl("console");
        assertEquals(WICKET_FAVICON_PREFIX +
                "console-icon.png",faviconService.getRelativeFaviconUrl());
    }

    @Test
    public void getRelativeFaviconUrlResourceDoesNotExist() {
        FaviconService faviconService = new WicketFaviconServiceImpl("authoring");
        assertEquals("skin/images/cms-icon.png",faviconService.getRelativeFaviconUrl());
    }

    @Test
    public void getFaviconResourceReferenceCms() {
        WicketFaviconService wicketFaviconService = new WicketFaviconServiceImpl("cms");
        final ResourceReference faviconResourceReference = wicketFaviconService.getFaviconResourceReference();
        assertEquals(WICKET_FAVICON_PREFIX +
                "cms-icon.png", getResourceReferenceUrl(faviconResourceReference));
    }

    @Test
    public void getFaviconResourceReferenceConsole() {
        WicketFaviconService wicketFaviconService = new WicketFaviconServiceImpl("console");
        final ResourceReference faviconResourceReference = wicketFaviconService.getFaviconResourceReference();
        assertEquals(WICKET_FAVICON_PREFIX +
                "console-icon.png", getResourceReferenceUrl(faviconResourceReference));
    }

    @Test
    public void getFaviconResourceReferenceResourceDoesNotExist() {
        WicketFaviconService wicketFaviconService = new WicketFaviconServiceImpl("authoring");
        final ResourceReference faviconResourceReference = wicketFaviconService.getFaviconResourceReference();
        assertEquals("skin/images/cms-icon.png", getResourceReferenceUrl(faviconResourceReference));
    }

    @NotNull
    protected String getResourceReferenceUrl(final ResourceReference faviconResourceReference) {
        return RequestCycle.get().urlFor(faviconResourceReference, null).toString();
    }
}
