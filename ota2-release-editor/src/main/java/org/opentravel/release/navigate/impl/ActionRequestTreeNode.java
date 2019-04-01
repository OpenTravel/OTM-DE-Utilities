/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.release.navigate.impl;

import org.opentravel.application.common.Images;
import org.opentravel.release.MessageBuilder;
import org.opentravel.release.NodeProperty;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLParamGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLActionRequest</code> instance.
 */
public class ActionRequestTreeNode extends TreeNode<TLActionRequest> {

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param factory the factory that created this node
     */
    public ActionRequestTreeNode(TLActionRequest entity, TreeNodeFactory factory) {
        super( entity, factory );
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getLabel()
     */
    @Override
    public String getLabel() {
        return "Request";
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getIcon()
     */
    @Override
    public Image getIcon() {
        return Images.requestIcon;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getProperties()
     */
    @Override
    public List<NodeProperty> getProperties() {
        List<NodeProperty> props = new ArrayList<>();
        TLActionRequest request = getEntity();
        TLParamGroup paramGroup = request.getParamGroup();

        props.add(
            new NodeProperty( "method", () -> MessageBuilder.formatMessage( request.getHttpMethod().toString() ) ) );
        props.add( new NodeProperty( "DESCRIPTION", () -> getDescription( request ) ) );
        props.add( new NodeProperty( "paramGroup", () -> (paramGroup == null) ? "" : paramGroup.getName() ) );
        props.add( new NodeProperty( "payloadType", () -> getEntityDisplayName( request.getPayloadType() ) ) );
        return props;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
     */
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
        return Collections.emptyList();
    }

}
