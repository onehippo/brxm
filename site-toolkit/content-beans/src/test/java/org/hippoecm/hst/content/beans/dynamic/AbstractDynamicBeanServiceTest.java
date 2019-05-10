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
package org.hippoecm.hst.content.beans.dynamic;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hippoecm.hst.AbstractBeanTestCase;
import org.hippoecm.hst.container.ModifiableRequestContextProvider;
import org.hippoecm.hst.content.beans.BaseDocument;
import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.junit.Before;

public abstract class AbstractDynamicBeanServiceTest extends AbstractBeanTestCase {

    protected List<Class<? extends HippoBean>> annotatedClasses;
    protected ObjectConverter objectConverter;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockHstRequestContext mockHstRequestContext = new MockHstRequestContext();
        mockHstRequestContext.setSession(session);
        ModifiableRequestContextProvider.set(mockHstRequestContext);

        if (annotatedClasses == null) {
            annotatedClasses = new ArrayList<>();
            annotatedClasses.add(BaseDocument.class);
        }

        objectConverter = getObjectConverter(annotatedClasses);
    }

    protected Object getContentBean() throws Exception {
        Object generatedBean = objectConverter.getObject(session, getDocumentPath());

        assertNotNull("The content bean is not created for " + getDocumentPath(), generatedBean);
        assertThat(generatedBean, instanceOf(HippoBean.class));

        return generatedBean;
    }

    @SuppressWarnings("unchecked")
    protected <T> T callContentBeanMethod(Object generatedBean, String methodName, Class<T> returnType)
            throws Exception {
        Method method = generatedBean.getClass().getMethod(methodName);

        assertNotNull("The method '" + methodName + "' is not found", method);
        assertEquals(returnType, method.getReturnType());

        return (T) method.invoke(generatedBean);
    }

    abstract String getDocumentPath();

}
