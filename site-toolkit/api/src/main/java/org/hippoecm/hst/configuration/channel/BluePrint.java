/*
 *  Copyright 2011 Hippo.
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
package org.hippoecm.hst.configuration.channel;

public interface BluePrint {

    /**
     * Unique id for this blueprint
     * @return
     */
    String getId();

    /**
     * @return the CMS plugin that is able to edit the configuration for this blueprint
     */
    String getCmsPluginClass();

    /**
     * Class for interface that is exposed at runtime to components
     * @return
     */
    String getParameterInfo();

}
