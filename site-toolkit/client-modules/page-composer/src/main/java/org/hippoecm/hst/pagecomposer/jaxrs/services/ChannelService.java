/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
 *
 */

package org.hippoecm.hst.pagecomposer.jaxrs.services;

import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.configuration.channel.Channel;
import org.hippoecm.hst.configuration.channel.ChannelException;
import org.hippoecm.hst.pagecomposer.jaxrs.model.ChannelInfoDescription;

public interface ChannelService {

    ChannelInfoDescription getChannelInfoDescription(final String channelId, final String locale) throws ChannelException;

    Channel getChannel(String channelId);

    void saveChannel(Session session, String channelId, Channel channel) throws RepositoryException, ChannelException;

    List<Channel> getChannels(boolean previewConfigRequired, boolean workspaceRequired);
}
