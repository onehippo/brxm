/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.dashboard.event.listeners;


import javax.inject.Singleton;

import org.onehippo.cms7.essentials.dashboard.event.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

/**
 * @version "$Id$"
 */
@Component
@Singleton
public class LoggingPluginEventListener {

    private static Logger log = LoggerFactory.getLogger(LoggingPluginEventListener.class);

    @Subscribe
    public void onPluginEvent(final LogEvent event) {
        log.info("@PLUGIN EVENT:  {}", event.getMessage());
    }
}
