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
import java.util.List;

import org.opentravel.application.common.Images;
import org.opentravel.release.MessageBuilder;
import org.opentravel.release.NodeProperty;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.release.navigate.impl.LibraryFolderTreeNode.FolderType;
import org.opentravel.schemacompiler.model.TLLibrary;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLLibrary</code> instance.
 */
public class LibraryTreeNode extends TreeNode<TLLibrary> {
	
	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 * @param factory  the factory that created this node
	 */
	public LibraryTreeNode(TLLibrary entity, TreeNodeFactory factory) {
		super(entity, factory);
	}
	
	/**
	 * @see org.opentravel.release.navigate.TreeNode#getLabel()
	 */
	@Override
	public String getLabel() {
		TLLibrary library = getEntity();
		
		return new StringBuilder().append( library.getPrefix() )
				.append(":").append( library.getName() ).toString();
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getIcon()
	 */
	@Override
	public Image getIcon() {
		return Images.libraryIcon;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getProperties()
	 */
	@Override
	public List<NodeProperty> getProperties() {
		List<NodeProperty> props = new ArrayList<>();
		TLLibrary library = getEntity();
		
		props.add( new NodeProperty( "name", library::getName ) );
		props.add( new NodeProperty( "namespace", library::getNamespace ) );
		props.add( new NodeProperty( "prefix", library::getPrefix ) );
		props.add( new NodeProperty( "version", library::getVersion ) );
		props.add( new NodeProperty( "status", () -> MessageBuilder.formatMessage( library.getStatus().toString() ) ) );
		props.add( new NodeProperty( "comments", library::getComments ) );
		return props;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
	 */
	@SuppressWarnings("unchecked")
    @Override
    protected List<TreeNode<Object>> initializeChildren() {
		List<TreeNode<Object>> children = new ArrayList<>();
		
		for (FolderType folderType : FolderType.values()) {
			TreeNode<?> folderNode = new LibraryFolderTreeNode( getEntity(), folderType, treeNodeFactory );
			
			if (!folderNode.getChildren().isEmpty()) {
				children.add( (TreeNode<Object>) folderNode );
			}
		}
		return children;
	}
	
}
