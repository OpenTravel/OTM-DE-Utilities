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

package org.opentravel.release.navigate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Factory that handles the creation of <code>TreeItem</code> instances based
 * on OTM model components.
 */
public class TreeNodeFactory {
	
	/**
	 * Constructs a new <code>TreeNode</code> for the given entity.
	 * 
	 * @param entity  the entity from which to construct the tree node
	 * @return TreeItem<TreeNode<Object>>
	 * @throws IllegalArgumentException  thrown if a node cannot be constructed for the given entity
	 */
	public TreeItem<TreeNode<Object>> newTree(Object entity) {
		return newTree( newTreeNode( entity ) );
	}
	
	/**
	 * Recursively constructs a new tree of items for the given node.
	 * 
	 * @param node  the node for which to construct a tree
	 * @return TreeItem<TreeNode<Object>>
	 */
	private TreeItem<TreeNode<Object>> newTree(TreeNode<Object> node) {
		TreeItem<TreeNode<Object>> treeItem = new TreeItem<>( node );
		Image nodeIcon = node.getIcon();
		
		if (nodeIcon != null) {
			treeItem.setGraphic( new ImageView( nodeIcon ) );
		}
		
		for (TreeNode<Object> child : node.getChildren()) {
			treeItem.getChildren().add( newTree( child ) );
		}
		
		return treeItem;
	}
	
	/**
	 * Constructs a new <code>TreeNode</code> for the given entity.
	 * 
	 * @param entity  the entity from which to construct the tree node
	 * @return TreeItem<? extends TreeNode<E>>
	 * @throws IllegalArgumentException  thrown if a node cannot be constructed for the given entity
	 */
	@SuppressWarnings("unchecked")
	public <E> TreeNode<E> newTreeNode(E entity) {
		Class<E> entityType = (Class<E>) entity.getClass();
		TreeNodeType nodeType = TreeNodeType.fromEntityType( entityType );
		Class<TreeNode<E>> nodeClass = (Class<TreeNode<E>>) nodeType.getNodeClass();
		
		try {
			Constructor<TreeNode<E>> constructor =
					nodeClass.getConstructor( entityType, TreeNodeFactory.class );
			
			return constructor.newInstance( entity, this );
			
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(
					"No qualifying constructor found for tree node class: " + nodeClass.getSimpleName(), e);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalArgumentException(
					"Error instantiating tree node class: " + nodeClass.getSimpleName(), e);
		}
	}
	
	/**
	 * Returns a comparator that can be used for sorting sibling tree items.
	 * 
	 * @return Comparator<TreeItem<TreeNode<Object>>>
	 */
	public Comparator<TreeItem<TreeNode<Object>>> getComparator() {
		return new TreeItemComparator();
	}
	
	/**
	 * Comparator used for sorting tree items creating by this factory.
	 */
	private static class TreeItemComparator implements Comparator<TreeItem<TreeNode<Object>>> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(TreeItem<TreeNode<Object>> item1, TreeItem<TreeNode<Object>> item2) {
			String label1 = item1.getValue().getLabel();
			String label2 = item2.getValue().getLabel();
			
			return label1.compareTo( label2 );
		}
		
	}
	
}
