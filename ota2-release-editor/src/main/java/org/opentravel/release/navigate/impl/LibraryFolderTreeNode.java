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
import org.opentravel.schemacompiler.model.TLLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.image.Image;

/**
 *
 * @author S. Livezey
 */
public class LibraryFolderTreeNode extends TreeNode<TLLibrary> {

    public enum FolderType {
        COMPLEX_TYPES, SIMPLE_TYPES, RESOURCES, SERVICES
    }

    private FolderType folderType;

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param folderType the type of folder represented by this node
     * @param factory the factory that created this node
     */
    public LibraryFolderTreeNode(TLLibrary entity, FolderType folderType, TreeNodeFactory factory) {
        super( entity, factory );
        this.folderType = folderType;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getLabel()
     */
    @Override
    public String getLabel() {
        try {
            return MessageBuilder.formatMessage( folderType.toString() );

        } catch (Exception e) {
            return "???";
        }
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getIcon()
     */
    @Override
    public Image getIcon() {
        return Images.folderIcon;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#getProperties()
     */
    @Override
    public List<NodeProperty> getProperties() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
     */
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
        List<TreeNode<Object>> children = new ArrayList<>();
        TLLibrary library = getEntity();

        switch (folderType) {
            case COMPLEX_TYPES:
                library.getValueWithAttributesTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getCoreObjectTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getChoiceObjectTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getBusinessObjectTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getContextualFacetTypes().forEach( e -> {
                    if (e.isLocalFacet()) {
                        treeNodeFactory.newTreeNode( e );
                    }
                } );
                library.getExtensionPointFacetTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                break;

            case SIMPLE_TYPES:
                library.getSimpleTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getClosedEnumerationTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                library.getOpenEnumerationTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                break;

            case RESOURCES:
                library.getResourceTypes().forEach( e -> treeNodeFactory.newTreeNode( e ) );
                break;

            case SERVICES:
                if (library.getService() != null) {
                    children.add( treeNodeFactory.newTreeNode( library.getService() ) );
                }
                break;

            default:
                // No default action required
        }
        return children;
    }

    /**
     * @see org.opentravel.release.navigate.TreeNode#sortChildren()
     */
    @Override
    public boolean sortChildren() {
        return false;
    }

}
