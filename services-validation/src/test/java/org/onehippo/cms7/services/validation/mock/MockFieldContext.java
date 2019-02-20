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
package org.onehippo.cms7.services.validation.mock;

import java.util.Locale;

import javax.jcr.Session;

import org.onehippo.cms7.services.validation.field.FieldContext;

public class MockFieldContext implements FieldContext {

    private String name;
    private String type;

    public MockFieldContext() {
    }

    public MockFieldContext(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Session getJcrSession() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public String getTranslatedMessage(final String key) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
