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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLExtensionPointFacet</code> instance.
 */
public class ExtensionPointFacetTreeNode extends TreeNode<TLExtensionPointFacet> {

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param factory the factory that created this node
     */
    public ExtensionPointFacetTreeNode(TLExtensionPointFacet entity, TreeNodeFactory factory) {
        super( entity, factory );
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getLabel()
     */
    @Override
    public String getLabel() {
        return getExtensionName( getEntity() );
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getIcon()
     */
    @Override
    public Image getIcon() {
        return Images.extensionPointFacetIcon;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getProperties()
     */
    @Override
    public List<NodeProperty> getProperties() {
        List<NodeProperty> props = new ArrayList<>();
        TLExtensionPointFacet epf = getEntity();

        props.add( new NodeProperty( "extends", () -> getExtensionName( epf ) ) );
        props.add( new NodeProperty( "description", () -> getDescription( epf ) ) );
        return props;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
     */
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
        List<TreeNode<Object>> children = new ArrayList<>();
        TLExtensionPointFacet epf = getEntity();

        for (TLAttribute attribute : epf.getAttributes()) {
            children.add( treeNodeFactory.newTreeNode( attribute ) );
        }
        for (TLProperty element : epf.getElements()) {
            children.add( treeNodeFactory.newTreeNode( element ) );
        }
        for (TLIndicator indicator : epf.getIndicators()) {
            children.add( treeNodeFactory.newTreeNode( indicator ) );
        }
        return children;
    }

}
