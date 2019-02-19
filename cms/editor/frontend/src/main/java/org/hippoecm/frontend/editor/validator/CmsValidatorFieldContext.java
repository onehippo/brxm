/*
 *  Copyright 2019 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.editor.validator;

import java.util.Locale;

import javax.jcr.Session;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.l10n.ResourceBundleModel;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.validation.IFieldValidator;
import org.onehippo.cms7.services.validation.field.FieldContext;

public class CmsValidatorFieldContext implements FieldContext {

    private final String name;
    private final String type;

    CmsValidatorFieldContext(final IFieldValidator fieldValidator) {
        name = fieldValidator.getFieldType().getName();
        type = fieldValidator.getFieldType().getType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Session getJcrSession() {
        return UserSession.get().getJcrSession();
    }

    @Override
    public Locale getLocale() {
        return UserSession.get().getLocale();
    }

    @Override
    public String getTranslatedMessage(final String key) {
        return getTranslation(key).getObject();
    }

    protected IModel<String> getTranslation(final String key) {
        return new ResourceBundleModel("hippo:cms.validators", key, getLocale());
    }

}
