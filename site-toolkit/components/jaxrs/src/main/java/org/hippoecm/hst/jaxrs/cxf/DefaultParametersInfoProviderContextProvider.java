/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.jaxrs.cxf;

import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.component.HstParameterInfoProxyFactory;
import org.hippoecm.hst.core.component.HstParameterValueConverter;
import org.hippoecm.hst.core.parameters.DefaultHstParameterValueConverter;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.parameters.ParametersInfoProvider;
import org.hippoecm.hst.core.request.ComponentConfiguration;
import org.hippoecm.hst.core.request.HstRequestContext;

@Provider
public class DefaultParametersInfoProviderContextProvider implements ContextProvider<ParametersInfoProvider> {

    private static final HstParameterValueConverter DEFAULT_HST_PARAMETER_VALUE_CONVERTER = new DefaultHstParameterValueConverter();

    @Override
    public ParametersInfoProvider createContext(Message message) {
        final OperationResourceInfo operationResourceInfo = message.getExchange().get(OperationResourceInfo.class);
        final Class<?> resourceCls = operationResourceInfo.getClassResourceInfo().getResourceClass();
        return new ParametersInfoProviderImpl(resourceCls.getAnnotation(ParametersInfo.class));
    }

    private static class ParametersInfoProviderImpl implements ParametersInfoProvider {

        private final ParametersInfo paramsInfoAnno;

        public ParametersInfoProviderImpl(final ParametersInfo paramsInfoAnno) {
            this.paramsInfoAnno = paramsInfoAnno;
        }

        @Override
        public <T> T getParametersInfo() {
            if (paramsInfoAnno == null) {
                return null;
            }

            final HstRequestContext requestContext = RequestContextProvider.get();
            final HstParameterInfoProxyFactory parameterInfoProxyFacotory = requestContext
                    .getParameterInfoProxyFactory();
            final ComponentConfiguration componentConfig = null;
            return parameterInfoProxyFacotory.createParameterInfoProxy(paramsInfoAnno, componentConfig,
                    requestContext.getServletRequest(), DEFAULT_HST_PARAMETER_VALUE_CONVERTER);
        }

    }
}
