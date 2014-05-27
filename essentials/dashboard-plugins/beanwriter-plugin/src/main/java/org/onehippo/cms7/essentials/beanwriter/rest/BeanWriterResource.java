/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.beanwriter.rest;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.onehippo.cms7.essentials.dashboard.ctx.DefaultPluginContext;
import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.model.PluginRestful;
import org.onehippo.cms7.essentials.dashboard.rest.BaseResource;
import org.onehippo.cms7.essentials.dashboard.rest.MessageRestful;
import org.onehippo.cms7.essentials.dashboard.rest.RestfulList;
import org.onehippo.cms7.essentials.dashboard.setup.ProjectSetupPlugin;
import org.onehippo.cms7.essentials.dashboard.utils.ProjectUtils;
import org.onehippo.cms7.services.contenttype.ContentType;
import org.onehippo.cms7.services.contenttype.ContentTypeService;
import org.onehippo.cms7.services.contenttype.ContentTypes;
import org.onehippo.cms7.services.contenttype.HippoContentTypeService;


/**
 * @version "$Id$"
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("/beanwriter/")
public class BeanWriterResource extends BaseResource {


    @POST
    public RestfulList<MessageRestful> runBeanWriter(@Context ServletContext servletContext) throws Exception {
        final String className = ProjectSetupPlugin.class.getName();
        final PluginContext context = new DefaultPluginContext(new PluginRestful(className));
        //############################################
        // USE SERVICES
        //############################################
        final ContentTypeService service = new HippoContentTypeService(context.createSession());
        final ContentTypes contentTypes = service.getContentTypes();
        final SortedMap<String, Set<ContentType>> typesByPrefix = contentTypes.getTypesByPrefix();
        for (Map.Entry<String, Set<ContentType>> entry : typesByPrefix.entrySet()) {
            final String key = entry.getKey();
        }



        final String basePath = ProjectUtils.getBaseProjectDirectory();




        // inject project settings:
        final RestfulList<MessageRestful> messages = new MyRestList();
        /*final java.nio.file.Path namespacePath = new File(basePath + File.separator + "bootstrap").toPath();

        final List<MemoryBean> memoryBeans = BeanWriterUtils.buildBeansGraph(namespacePath, context, EssentialConst.SOURCE_PATTERN_JAVA);
        BeanWriterUtils.addMissingMethods(context, memoryBeans, EssentialConst.FILE_EXTENSION_JAVA);
        final Multimap<String, Object> pluginContextData = context.getPluginContextData();
        final Collection<Object> objects = pluginContextData.get(BeanWriterUtils.CONTEXT_DATA_KEY);
        for (Object object : objects) {
            final BeanWriterLogEntry entry = (BeanWriterLogEntry) object;
            messages.add(new MessageRestful(entry.getMessage()));
        }
        if (messages.getItems().size() == 0) {
            messages.add(new MessageRestful("All beans were up to date"));
        } else {
            messages.add(new MessageRestful("Please rebuild and restart your application:", DisplayEvent.DisplayType.STRONG));

            messages.add(new MessageRestful(
                    "mvn clean package\n" +
                            "mvn -P cargo.run", DisplayEvent.DisplayType.PRE
            ));
        }*/

        return messages;
    }
}
