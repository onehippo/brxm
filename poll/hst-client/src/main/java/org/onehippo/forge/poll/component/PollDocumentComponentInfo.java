/*
 * Copyright 2015-2019 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.poll.component;

import org.hippoecm.hst.core.parameters.JcrPath;
import org.hippoecm.hst.core.parameters.Parameter;

public interface PollDocumentComponentInfo extends PollComponentInfo {

    @Override
    @Parameter(name = "poll-dataPath", hideInChannelManager = true)
    String getPollDataPath();

    @Override
    @Parameter(name = "poll-docsClass", hideInChannelManager = true)
    String getPollDocsClass();

    @Override
    @Parameter(name = "poll-pollCompoundName", hideInChannelManager = true)
    String getPollCompoundName();

    @Override
    @Parameter(name = "poll-docsPath", required = true)
    @JcrPath(pickerSelectableNodeTypes = "poll:polldocument")
    String getPollDocsPath();
}
