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
import org.opentravel.release.NodeProperty;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.schemacompiler.model.TLSimple;

import javafx.scene.image.Image;

/**
 * Tree node that represents a <code>TLSimple</code> instance.
 */
public class SimpleTreeNode extends TreeNode<TLSimple> {

	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 * @param factory  the factory that created this node
	 */
	public SimpleTreeNode(TLSimple entity, TreeNodeFactory factory) {
		super(entity, factory);
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getLabel()
	 */
	@Override
	public String getLabel() {
		return getEntity().getLocalName();
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getIcon()
	 */
	@Override
	public Image getIcon() {
		return Images.simpleTypeIcon;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getProperties()
	 */
	@Override
	public List<NodeProperty> getProperties() {
		List<NodeProperty> props = new ArrayList<>();
		TLSimple simple = getEntity();
		
		props.add( new NodeProperty( "name", () -> { return simple.getName(); } ) );
		props.add( new NodeProperty( "DESCRIPTION", () -> { return getDescription( simple ); } ) );
		props.add( new NodeProperty( "parentType", () -> { return getEntityDisplayName( simple.getParentType() ); } ) );
		props.add( new NodeProperty( "listTypeInd", () -> { return simple.isListTypeInd() + ""; } ) );
		
		if (simple.getPattern() != null) {
			props.add( new NodeProperty( "pattern", () -> { return simple.getPattern(); } ) );
		}
		if (simple.getMinLength() >= 0) {
			props.add( new NodeProperty( "minLength", () -> { return simple.getMinLength() + ""; } ) );
		}
		if (simple.getMaxLength() >= 0) {
			props.add( new NodeProperty( "maxLength", () -> { return simple.getMaxLength() + ""; } ) );
		}
		if (simple.getFractionDigits() >= 0) {
			props.add( new NodeProperty( "fractionDigits", () -> { return simple.getFractionDigits() + ""; } ) );
		}
		if (simple.getTotalDigits() >= 0) {
			props.add( new NodeProperty( "totalDigits", () -> { return simple.getTotalDigits() + ""; } ) );
		}
		if (simple.getMinInclusive() != null) {
			props.add( new NodeProperty( "minInclusive", () -> { return simple.getMinInclusive() + ""; } ) );
		}
		if (simple.getMaxInclusive() != null) {
			props.add( new NodeProperty( "maxInclusive", () -> { return simple.getMaxInclusive() + ""; } ) );
		}
		if (simple.getMinExclusive() != null) {
			props.add( new NodeProperty( "minExclusive", () -> { return simple.getMinExclusive() + ""; } ) );
		}
		if (simple.getMaxExclusive() != null) {
			props.add( new NodeProperty( "maxExclusive", () -> { return simple.getMaxExclusive() + ""; } ) );
		}
		return props;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
	 */
	@Override
	protected List<TreeNode<?>> initializeChildren() {
		return Collections.emptyList();
	}
	
}
