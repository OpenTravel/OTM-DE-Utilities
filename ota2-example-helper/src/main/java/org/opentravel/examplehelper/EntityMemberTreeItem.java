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

package org.opentravel.examplehelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Tree item that can be used to display a structure of <code>EntityMemberNode</code>s
 * in a JavaFX tree view.
 */
public class EntityMemberTreeItem extends TreeItem<EntityMemberNode> {
	
	private Map<String,ObservableList<TreeItem<EntityMemberNode>>> childrenByFacetName = new HashMap<>();
	
	/**
	 * Constructor that assigns the underlying node for this tree item.
	 * 
	 * @param node  the entity member node
	 */
	public EntityMemberTreeItem(EntityMemberNode node) {
		List<String> facetNames = node.getFacetSelection().getFacetNames();
		List<TreeItem<EntityMemberNode>> allChildren = new ArrayList<>();
		
		setValue( node );
		setExpanded( true );
		
		if (facetNames != null) {
			for (String facetName : facetNames) {
				List<TreeItem<EntityMemberNode>> itemChildren = new ArrayList<>();
				
				for (EntityMemberNode childNode : node.getChildren( facetName )) {
					EntityMemberTreeItem childItem = new EntityMemberTreeItem( childNode );
					
					itemChildren.add( childItem );
					allChildren.add( childItem );
				}
				childrenByFacetName.put( facetName, FXCollections.observableArrayList( itemChildren ) );
			}
			super.getChildren().addAll( allChildren );
		}
	}
	
	/**
	 * @see javafx.scene.control.TreeItem#getChildren()
	 */
	@Override
	public ObservableList<TreeItem<EntityMemberNode>> getChildren() {
		String selectedFacet = getValue().getFacetSelection().getSelectedFacetName();
		ObservableList<TreeItem<EntityMemberNode>> children = null;
		
		if (selectedFacet == null) {
			if (childrenByFacetName.size() == 1) {
				children = childrenByFacetName.values().iterator().next();
			}
		} else {
			children = childrenByFacetName.get( selectedFacet );
		}
		
		if (children == null) {
			children = FXCollections.observableArrayList();
		}
		return children;
	}

	/**
	 * @see javafx.scene.control.TreeItem#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return getChildren().isEmpty();
	}
	
}
