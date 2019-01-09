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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLReferenceType;

/**
 * Tree builder component that constructs trees of <code>EntityMemberNode</code>s from
 * which the user can select substitution groups to render in the EXAMPLE pane.
 */
public class EntityMemberTreeBuilder {
	
	private NamedEntity otmEntity;
	private FacetSelections facetSelections = new FacetSelections();
	
	/**
	 * Constructor that specifies the OTM entity for which to construct a tree.
	 * 
	 * @param otmEntity  the OTM entity
	 */
	public EntityMemberTreeBuilder(NamedEntity otmEntity) {
		this.otmEntity = otmEntity;
	}
	
	/**
	 * Constructs a node tree that can be configured by the user with their preferred
	 * substitution group selections.
	 * 
	 * @return EntityMemberNode
	 */
	public EntityMemberNode buildTree() {
		return buildTree( otmEntity, new HashSet<QName>() );
	}
	
	/**
	 * Recursive routine that builds the tree of <code>EntityMemberNode</code> objects.
	 * 
	 * @param entity  the entity for which to build the root node of the tree
	 * @param priorEntityNames  the set of qualified entity names that have occurred
	 *							in higher-level parent nodes of the tree
	 * @return EntityMemberNode
	 */
	private EntityMemberNode buildTree(NamedEntity entity, Set<QName> priorEntityNames) {
		QName entityName = new QName( entity.getNamespace(), entity.getLocalName() );
		EntityMemberNode rootNode = null;
		
		// Only create a node if we have not yet encountered this entity in the hierarchy
		if (!priorEntityNames.contains( entityName )) {
			
			// Start by building a new node for this entity
			EntityFacetSelection facetSelection = facetSelections.getFacetSelection( entity );
			String nodeName = HelperUtils.getDisplayName( entity, false );
			
			if (facetSelection == null) {
				facetSelection = new EntityFacetSelection( entity );
				facetSelections.addFacetSelection( facetSelection );
			}
			rootNode = new EntityMemberNode( nodeName, facetSelection );
			priorEntityNames.add( entityName );
			
			// Next build the children for this node
			List<TLFacet> facetList = new ArrayList<>();
			
			if (facetSelection.getFacetNames() != null) {
				for (String facetName : facetSelection.getFacetNames()) {
					facetList.add( facetSelection.getFacet( facetName ) );
				}
				
			} else if (facetSelection.getEntityType() instanceof TLFacet) {
				facetList.add( (TLFacet) facetSelection.getEntityType() );
			}
			
			// If required, build a business object element for an action facet
			if (entity instanceof TLActionFacet) {
				TLActionFacet actionFacet = (TLActionFacet) entity;
				
				if ((actionFacet.getReferenceType() != TLReferenceType.NONE)
						&& !facetSelection.getFacetNames().isEmpty()) {
					TLBusinessObject boRef = actionFacet.getOwningResource().getBusinessObjectRef();
					EntityMemberNode boNode = buildTree( boRef, priorEntityNames );
					
					if (boNode != null) {
						rootNode.addChild( facetSelection.getFacetNames().get( 0 ), boNode );
					}
				}
			}
			
			// Build a child node for each element of each facet
			for (TLFacet facet : facetList) {
				List<TLProperty> facetElements = PropertyCodegenUtils.getInheritedProperties( facet );
				
				for (TLProperty element : facetElements) {
					TLPropertyType elementType = element.getType();
					EntityMemberNode childNode;
					
					if (elementType instanceof TLAlias) {
						elementType = (TLPropertyType) ((TLAlias) elementType).getOwningEntity();
					}
					if ((elementType instanceof TLPropertyOwner) || (elementType instanceof TLFacetOwner)) {
						childNode = buildTree( element.getType(), priorEntityNames );
						
						if (childNode != null) {
							rootNode.addChild( facetSelection.getFacetName( facet ), childNode );
						}
					}
				}
			}
			
			priorEntityNames.remove( entityName );
		}
		return rootNode;
	}
	
	/**
	 * Returns the <code>FacetSelections</code> that contains the selection options
	 * for each entity in the resulting node tree.
	 * 
	 * @return FacetSelections
	 */
	public FacetSelections getFacetSelections() {
		return facetSelections;
	}
	
}
