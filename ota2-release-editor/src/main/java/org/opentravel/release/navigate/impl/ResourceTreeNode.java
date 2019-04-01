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
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLResource</code> instance.
 */
public class ResourceTreeNode extends TreeNode<TLResource> {

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param factory the factory that created this node
     */
    public ResourceTreeNode(TLResource entity, TreeNodeFactory factory) {
        super( entity, factory );
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getLabel()
     */
    @Override
    public String getLabel() {
        return getEntity().getName();
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getIcon()
     */
    @Override
    public Image getIcon() {
        return Images.resourceIcon;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getProperties()
     */
    @Override
    public List<NodeProperty> getProperties() {
        List<NodeProperty> props = new ArrayList<>();
        TLResource resource = getEntity();

        props.add( new NodeProperty( "name", resource::getName ) );
        props.add( new NodeProperty( "DESCRIPTION", () -> getDescription( resource ) ) );
        props.add( new NodeProperty( "extends", () -> getExtensionName( resource ) ) );
        props.add( new NodeProperty( "abstract", () -> resource.isAbstract() + "" ) );
        props.add( new NodeProperty( "basePath", resource::getBasePath ) );
        props.add( new NodeProperty( "firstClass", () -> resource.isFirstClass() + "" ) );
        props
            .add( new NodeProperty( "businessObject", () -> getEntityDisplayName( resource.getBusinessObjectRef() ) ) );
        return props;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
     */
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
        List<TreeNode<Object>> children = new ArrayList<>();
        TLResource resource = getEntity();

        for (TLResourceParentRef entity : resource.getParentRefs()) {
            children.add( treeNodeFactory.newTreeNode( entity ) );
        }
        for (TLParamGroup entity : resource.getParamGroups()) {
            children.add( treeNodeFactory.newTreeNode( entity ) );
        }
        for (TLActionFacet entity : resource.getActionFacets()) {
            children.add( treeNodeFactory.newTreeNode( entity ) );
        }
        for (TLAction entity : resource.getActions()) {
            children.add( treeNodeFactory.newTreeNode( entity ) );
        }
        return children;
    }

}
