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
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

import javafx.scene.image.Image;

/**
 * Abstract tree node that represents a <code>TLAbstractFacet</code> instance.
 */
public class AbstractFacetTreeNode<E extends TLAbstractFacet> extends TreeNode<E> {
	
	/**
	 * Tree node that represents a <code>TLFacet</code> instance.
	 */
	public static class FacetTreeNode extends AbstractFacetTreeNode<TLFacet> {
		public FacetTreeNode(TLFacet entity, TreeNodeFactory factory) {
			super(entity, factory);
		}
	}
	/**
	 * Tree node that represents a <code>TLContextualFacet</code> instance.
	 */
	public static class ContextualFacetTreeNode extends AbstractFacetTreeNode<TLContextualFacet> {
		public ContextualFacetTreeNode(TLContextualFacet entity, TreeNodeFactory factory) {
			super(entity, factory);
		}
	}
	
	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 * @param factory  the factory that created this node
	 */
	public AbstractFacetTreeNode(E entity, TreeNodeFactory factory) {
		super(entity, factory);
	}
	
	/**
	 * @see org.opentravel.release.navigate.TreeNode#getLabel()
	 */
	@Override
	public String getLabel() {
		return (getEntity() instanceof TLContextualFacet) ?
				((TLContextualFacet) getEntity()).getName() :
					MessageBuilder.formatMessage( getEntity().getFacetType().toString() );
	}
	
	/**
	 * @see org.opentravel.release.navigate.TreeNode#getIcon()
	 */
	@Override
	public Image getIcon() {
		return (getEntity() instanceof TLContextualFacet) ? Images.contextualFacetIcon : Images.facetIcon;
	}
	
	/**
	 * @see org.opentravel.release.navigate.TreeNode#getProperties()
	 */
	@Override
	public List<NodeProperty> getProperties() {
		List<NodeProperty> props = new ArrayList<>();
		TLAbstractFacet facet = getEntity();
		
		props.add( new NodeProperty( "name", () -> MessageBuilder.formatMessage( facet.getFacetType().toString() ) ) );
		props.add( new NodeProperty( "DESCRIPTION", () -> getDescription( facet ) ) );
		
		if (facet instanceof TLContextualFacet) {
			TLContextualFacet ctxFacet = (TLContextualFacet) facet;
			
			props.add( new NodeProperty( "localFacet", () -> ctxFacet.isLocalFacet() + "" ) );
			props.add( new NodeProperty( "facetOwner", () -> getEntityDisplayName( ctxFacet.getOwningEntity() ) ) );
			props.add( new NodeProperty( "owningLibrary", () -> getLibraryDisplayName( ctxFacet.getOwningLibrary() ) ) );
		}
		return props;
	}
	
	/**
	 * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
	 */
	@Override
	protected List<TreeNode<Object>> initializeChildren() {
		List<TreeNode<Object>> children = new ArrayList<>();
		TLAbstractFacet abstractFacet = getEntity();
		
		if (abstractFacet instanceof TLFacet) {
			TLFacet facet = (TLFacet) abstractFacet;
			
			if (facet instanceof TLContextualFacet) {
				for (TLContextualFacet childFacet : ((TLContextualFacet) facet).getChildFacets()) {
					children.add( treeNodeFactory.newTreeNode( childFacet ) );
				}
			}
			for (TLAttribute attribute : facet.getAttributes()) {
				children.add( treeNodeFactory.newTreeNode( attribute ) );
			}
			for (TLProperty element : facet.getElements()) {
				children.add( treeNodeFactory.newTreeNode( element ) );
			}
			for (TLIndicator indicator : facet.getIndicators()) {
				children.add( treeNodeFactory.newTreeNode( indicator ) );
			}
		}
		return children;
	}
	
}
