/*
 * Copyright 2018-2019 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms.channelmanager.content.documenttype.field.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onehippo.cms.channelmanager.content.document.model.FieldValue;
import org.onehippo.cms.channelmanager.content.documenttype.field.FieldTypeContext;
import org.onehippo.cms.channelmanager.content.documenttype.field.FieldTypeUtils;
import org.onehippo.cms.channelmanager.content.documenttype.util.LocalizationUtils;
import org.onehippo.cms.channelmanager.content.error.BadRequestException;
import org.onehippo.cms.channelmanager.content.error.ErrorInfo;
import org.onehippo.repository.mock.MockNode;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({LocalizationUtils.class, FieldTypeUtils.class})
public class StaticDropdownFieldTypeTest {

    private static final String PROPERTY = "test:id";

    @Test
    public void populateDropdownWithEqualKeyValues() {
        final StaticDropdownFieldType fieldType = new StaticDropdownFieldType();
        final FieldTypeContext fieldTypeContext = new MockFieldTypeContext.Builder(fieldType)
                .jcrName("myproject:staticdropdown")
                .build();
        expect(fieldTypeContext.getStringConfig("selectable.options")).andReturn(Optional.of("ca,us,mx"));

        replayAll();

        fieldType.init(fieldTypeContext);

        assertThat(fieldType.getOptionDisplayValues().size(), equalTo(3));
        assertThat(fieldType.getOptionValues().size(), equalTo(3));

        final List<String> expected = Arrays.asList("ca", "us", "mx");
        assertThat(fieldType.getOptionValues(), equalTo(expected));
        assertThat(fieldType.getOptionDisplayValues(), equalTo(expected));

        verifyAll();
    }

    @Test
    public void populateDropdownWithDifferentKeyValues() {
        final StaticDropdownFieldType fieldType = new StaticDropdownFieldType();
        final FieldTypeContext fieldTypeContext = new MockFieldTypeContext.Builder(fieldType)
                .jcrName("myproject:staticdropdown")
                .build();
        expect(fieldTypeContext.getStringConfig("selectable.options"))
                .andReturn(Optional.of("ca=Canada,us=United States,mx=Mexico"));

        replayAll();


        fieldType.init(fieldTypeContext);

        assertThat(fieldType.getOptionDisplayValues().size(), equalTo(3));
        assertThat(fieldType.getOptionValues().size(), equalTo(3));

        final List<String> expectedKeys = Arrays.asList("ca", "us", "mx");
        final List<String> expectedValues = Arrays.asList("Canada", "United States", "Mexico");
        assertThat(fieldType.getOptionValues(), equalTo(expectedKeys));
        assertThat(fieldType.getOptionDisplayValues(), equalTo(expectedValues));

        verifyAll();
    }

    @Test
    public void writeToSingleDouble() throws Exception {
        final Node node = MockNode.root();
        final PropertyFieldType fieldType = new StaticDropdownFieldType();
        final String oldValue = "one";
        final String newValue = "two";

        fieldType.setId(PROPERTY);
        fieldType.setJcrType(PropertyType.TYPENAME_STRING);
        node.setProperty(PROPERTY, oldValue);

        try {
            fieldType.writeTo(node, Optional.empty());
            fail("Must not be missing");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(ErrorInfo.Reason.INVALID_DATA));
        }
        assertThat(node.getProperty(PROPERTY).getString(), equalTo(oldValue));

        try {
            fieldType.writeTo(node, Optional.of(Collections.emptyList()));
            fail("Must have 1 entry");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(ErrorInfo.Reason.INVALID_DATA));
        }

        try {
            fieldType.writeTo(node, Optional.of(Arrays.asList(valueOf("11"), valueOf("12"))));
            fail("Must have 1 entry");
        } catch (final BadRequestException e) {
            assertThat(((ErrorInfo) e.getPayload()).getReason(), equalTo(ErrorInfo.Reason.INVALID_DATA));
        }

        fieldType.writeTo(node, Optional.of(listOf(valueOf(newValue))));
        assertThat(node.getProperty(PROPERTY).getString(), equalTo(newValue));

        verifyAll();
    }

    private static List<FieldValue> listOf(final FieldValue value) {
        return Collections.singletonList(value);
    }

    private static FieldValue valueOf(final String value) {
        return new FieldValue(value);
    }
}
