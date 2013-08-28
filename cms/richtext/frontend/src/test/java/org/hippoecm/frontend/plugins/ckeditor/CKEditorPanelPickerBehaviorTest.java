/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugins.ckeditor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.easymock.classextension.EasyMock;
import org.hippoecm.frontend.plugins.richtext.dialog.images.ImagePickerBehavior;
import org.hippoecm.frontend.plugins.richtext.dialog.links.DocumentPickerBehavior;
import org.hippoecm.frontend.plugins.ckeditor.hippopicker.HippoPicker;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link CKEditorPanelPickerBehavior}.
 */
public class CKEditorPanelPickerBehaviorTest {

    private DocumentPickerBehavior documentPickerBehavior;
    private ImagePickerBehavior imagePickerBehavior;
    private CKEditorPanelPickerBehavior behavior;

    @Before
    public void setUp() {
        documentPickerBehavior = EasyMock.createMock(DocumentPickerBehavior.class);
        imagePickerBehavior = EasyMock.createMock(ImagePickerBehavior.class);
        behavior = new CKEditorPanelPickerBehavior(documentPickerBehavior, imagePickerBehavior);
    }

    @Test
    public void callbackUrlsAreAddedToCKEditorConfiguration() throws Exception {
        final String documentPickerCallbackUrl = "./documentpicker/callback";
        final String imagePickerCallbackUrl = "./imagepicker/callback";

        expect(documentPickerBehavior.getCallbackUrl()).andReturn(documentPickerCallbackUrl);
        expect(imagePickerBehavior.getCallbackUrl()).andReturn(imagePickerCallbackUrl);

        replay(documentPickerBehavior, imagePickerBehavior);

        final JSONObject editorConfig = new JSONObject();
        behavior.addCKEditorConfiguration(editorConfig);

        verify(documentPickerBehavior, imagePickerBehavior);

        assertTrue("CKEditor config has configuration for the hippopicker plugin", editorConfig.has(HippoPicker.CONFIG_KEY));
        JSONObject hippoPickerConfig = editorConfig.getJSONObject(HippoPicker.CONFIG_KEY);

        assertTrue("hippopicker configuration has configuration for the internal link picker", hippoPickerConfig.has(HippoPicker.InternalLink.CONFIG_KEY));
        JSONObject internalLinkPickerConfig = hippoPickerConfig.getJSONObject(HippoPicker.InternalLink.CONFIG_KEY);
        assertEquals(documentPickerCallbackUrl, internalLinkPickerConfig.getString(HippoPicker.InternalLink.CONFIG_CALLBACK_URL));

        assertTrue("hippopicker configuration has configuration for the image picker", hippoPickerConfig.has(HippoPicker.Image.CONFIG_KEY));
        JSONObject imagePickerConfig = hippoPickerConfig.getJSONObject(HippoPicker.Image.CONFIG_KEY);
        assertEquals(imagePickerCallbackUrl, imagePickerConfig.getString(HippoPicker.Image.CONFIG_CALLBACK_URL));
    }

    @Test
    public void testGetAjaxBehaviors() throws Exception {
        List ajaxBehaviors = new ArrayList();
        for (AbstractAjaxBehavior ajaxBehavior : behavior.getAjaxBehaviors()) {
            ajaxBehaviors.add(ajaxBehavior);
        }
        assertEquals(2, ajaxBehaviors.size());
        assertTrue(ajaxBehaviors.contains(documentPickerBehavior));
        assertTrue(ajaxBehaviors.contains(imagePickerBehavior));
    }

}
