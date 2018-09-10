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

/**
 * Represents a single tree node for an OTM entity structure from which a user may choose
 * a substitution group facet if choices are available.
 */
public class EntityMemberNode {
	
	private String name;
	private EntityFacetSelection facetSelection;
	private EntityMemberNode parentNode;
	private Map<String,List<EntityMemberNode>> childrenByFacet = new HashMap<>();
	
	/**
	 * Constructs a new tree node instance.
	 * 
	 * @param name  the name of the tree node
	 * @param facetSelection  the facet selection instance for the node
	 * @param entityType  the type of the OTM entity represented by this node
	 */
	public EntityMemberNode(String name, EntityFacetSelection facetSelection) {
		this.name = name;
		this.facetSelection = facetSelection;
	}
	
	/**
	 * Returns the name of this node.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the facet selection instance for the node.
	 *
	 * @return EntityFacetSelection
	 */
	public EntityFacetSelection getFacetSelection() {
		return facetSelection;
	}

	/**
	 * Returns the parent of this node.
	 *
	 * @return EntityMemberNode
	 */
	public EntityMemberNode getParentNode() {
		return parentNode;
	}
	
	/**
	 * Returns the children of this node within the context of the specified facet.  If no
	 * such facet exists, this method will return an empty list.
	 *
	 * @return List<EntityMemberNode>
	 */
	public List<EntityMemberNode> getChildren(String facetName) {
		List<EntityMemberNode> children = childrenByFacet.get( facetName );
		return (children == null) ? new ArrayList<EntityMemberNode>() : children;
	}
	
	/**
	 * Adds a child to this node under the specified facet name.
	 * 
	 * @param facetName  the facet name for the new node
	 * @param childNode  the child node to be added
	 */
	public void addChild(String facetName, EntityMemberNode childNode) {
		if (!facetSelection.getFacetNames().contains( facetName )) {
			throw new IllegalArgumentException("Invalid facet name for child node: " + facetName);
		}
		List<EntityMemberNode> children = childrenByFacet.get( facetName );
		
		if (children == null) {
			children = new ArrayList<>();
			childrenByFacet.put( facetName, children );
		}
		childNode.parentNode = this;
		children.add( childNode );
	}
	
}
