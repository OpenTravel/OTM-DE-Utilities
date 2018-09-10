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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.application.common.Images;
import org.opentravel.release.MessageBuilder;
import org.opentravel.release.NodeProperty;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import javafx.scene.image.Image;

/**
 *
 * @author S. Livezey
 */
public class LibraryFolderTreeNode extends TreeNode<TLLibrary> {
	
	public static enum FolderType { COMPLEX_TYPES, SIMPLE_TYPES, RESOURCES, SERVICES }
	
	private FolderType folderType;
	
	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 * @param factory  the factory that created this node
	 */
	public LibraryFolderTreeNode(TLLibrary entity, FolderType folderType, TreeNodeFactory factory) {
		super(entity, factory);
		this.folderType = folderType;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getLabel()
	 */
	@Override
	public String getLabel() {
		try {
			return MessageBuilder.formatMessage( folderType.toString() );
			
		} catch (Throwable t) {
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
	protected List<TreeNode<?>> initializeChildren() {
		List<TreeNode<?>> children = new ArrayList<>();
		TLLibrary library = getEntity();
		
		switch (folderType) {
			case COMPLEX_TYPES:
				for (TLValueWithAttributes entity : library.getValueWithAttributesTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLCoreObject entity : library.getCoreObjectTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLChoiceObject entity : library.getChoiceObjectTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLBusinessObject entity : library.getBusinessObjectTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLContextualFacet entity : library.getContextualFacetTypes()) {
					if (!entity.isLocalFacet()) {
						children.add( treeNodeFactory.newTreeNode( entity ) );
					}
				}
				for (TLExtensionPointFacet entity : library.getExtensionPointFacetTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				break;
				
			case SIMPLE_TYPES:
				for (TLSimple entity : library.getSimpleTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLClosedEnumeration entity : library.getClosedEnumerationTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				for (TLOpenEnumeration entity : library.getOpenEnumerationTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				break;
				
			case RESOURCES:
				for (TLResource entity : library.getResourceTypes()) {
					children.add( treeNodeFactory.newTreeNode( entity ) );
				}
				break;
				
			case SERVICES:
				if (library.getService() != null) {
					children.add( treeNodeFactory.newTreeNode( library.getService() ) );
				}
				break;
		}
		return children;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#sortChildren()
	 */
	@Override
	public boolean sortChildren() {
		return false;
	};
	
}
