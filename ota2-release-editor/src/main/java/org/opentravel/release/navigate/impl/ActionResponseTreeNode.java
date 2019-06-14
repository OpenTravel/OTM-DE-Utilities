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
import org.opentravel.release.NodeProperty;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.schemacompiler.model.TLActionResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLActionResponse</code> instance.
 */
public class ActionResponseTreeNode extends TreeNode<TLActionResponse> {

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param factory the factory that created this node
     */
    public ActionResponseTreeNode(TLActionResponse entity, TreeNodeFactory factory) {
        super( entity, factory );
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getLabel()
     */
    @Override
    public String getLabel() {
        return "Response [" + getDisplayStatusCodes() + "]";
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getIcon()
     */
    @Override
    public Image getIcon() {
        return Images.responseIcon;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getProperties()
     */
    @Override
    public List<NodeProperty> getProperties() {
        List<NodeProperty> props = new ArrayList<>();
        TLActionResponse response = getEntity();

        props.add( new NodeProperty( "statusCodes", this::getDisplayStatusCodes ) );
        props.add( new NodeProperty( "description", () -> getDescription( response ) ) );
        props.add( new NodeProperty( "payloadType", () -> getEntityDisplayName( response.getPayloadType() ) ) );
        return props;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
     */
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
        return Collections.emptyList();
    }

    /**
     * Returns a displayable version of the status codes for the response.
     * 
     * @return String
     */
    private String getDisplayStatusCodes() {
        StringBuilder codesStr = new StringBuilder();

        for (Integer statusCode : getEntity().getStatusCodes()) {
            if (codesStr.length() > 0) {
                codesStr.append( "," );
            }
            codesStr.append( statusCode );
        }
        return codesStr.toString();
    }

}
