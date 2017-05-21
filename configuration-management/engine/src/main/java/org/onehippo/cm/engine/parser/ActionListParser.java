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
package org.onehippo.cm.engine.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onehippo.cm.api.model.action.ActionItem;
import org.onehippo.cm.api.model.action.ActionType;
import org.onehippo.cm.impl.model.ModuleImpl;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

public class ActionListParser extends AbstractBaseParser {

    private static final String ACTION_LISTS_NODE = "action-lists";

    public ActionListParser(final boolean explicitSequencing) {
        super(explicitSequencing);
    }

    public void parse(final InputStream inputStream, final String location, final ModuleImpl module) throws ParserException {

        final Node node = composeYamlNode(inputStream, location);
        final Map<String, Node> sourceMap = asMapping(node, new String[]{ACTION_LISTS_NODE}, null);
        for (final Node versionNode : asSequence(sourceMap.get(ACTION_LISTS_NODE))) {

            final MappingNode mappingNode = (MappingNode) versionNode;
            final NodeTuple nodeTuple= mappingNode.getValue().get(0);

            final String strVersion = asScalar(nodeTuple.getKeyNode()).getValue();
            final Double version = Double.parseDouble(strVersion);

            final List<ActionItem> actionItems = collectActionItems(nodeTuple);
            module.getActionsMap().put(version, actionItems);
        }
    }

    private List<ActionItem> collectActionItems(final NodeTuple nodeTuple) throws ParserException {
        final List<ActionItem> actionItems = new ArrayList<>();
        final Map<String, Node> actionItemsMap = asMapping(nodeTuple.getValueNode(), null, null);
        for (final String path : actionItemsMap.keySet()) {
            final ActionItem actionItem = createActionItem(actionItemsMap, path);
            if (!actionItems.add(actionItem)) {
                throw new RuntimeException(String.format("Duplicate items are not allowed: %s", actionItem));
            }
        }
        return actionItems;
    }

    private ActionItem createActionItem(final Map<String, Node> stringNodeMap, final String path) throws ParserException {
        Node actionNode = stringNodeMap.get(path);
        String action = asScalar(actionNode).getValue();
        return new ActionItem(path, ActionType.valueOf(StringUtils.upperCase(action)));
    }
}
