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
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

import javafx.scene.image.Image;

/**
 * Abstract tree node that represents a <code>TLAbstractEnumeration</code> instance.
 */
public abstract class EnumerationTreeNode<E extends TLAbstractEnumeration> extends TreeNode<E> {
	
	/**
	 * Tree node that represents a <code>TLClosedEnumeration</code> instance.
	 */
	public static class ClosedEnumerationTreeNode extends EnumerationTreeNode<TLClosedEnumeration> {
		public ClosedEnumerationTreeNode(TLClosedEnumeration entity, TreeNodeFactory factory) {
			super(entity, factory);
		}
	}
	
	/**
	 * Tree node that represents a <code>TLOpenEnumeration</code> instance.
	 */
	public static class OpenEnumerationTreeNode extends EnumerationTreeNode<TLOpenEnumeration> {
		public OpenEnumerationTreeNode(TLOpenEnumeration entity, TreeNodeFactory factory) {
			super(entity, factory);
		}
	}
	
	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 * @param factory  the factory that created this node
	 */
	public EnumerationTreeNode(E entity, TreeNodeFactory factory) {
		super( entity, factory );
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
		return Images.enumerationIcon;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#getProperties()
	 */
	@Override
	public List<NodeProperty> getProperties() {
		List<NodeProperty> props = new ArrayList<>();
		TLAbstractEnumeration enumeration = getEntity();
		
		props.add( new NodeProperty( "name", () -> { return enumeration.getName(); } ) );
		props.add( new NodeProperty( "enumType",() -> { return (enumeration instanceof TLOpenEnumeration) ? "Open" : "Closed"; } ) );
		props.add( new NodeProperty( "description", () -> { return getDescription( enumeration ); } ) );
		props.add( new NodeProperty( "extends", () -> { return getExtensionName( enumeration ); } ) );
		return props;
	}

	/**
	 * @see org.opentravel.release.navigate.TreeNode#initializeChildren()
	 */
	@Override
	protected List<TreeNode<?>> initializeChildren() {
		// TODO: Add enumeration value children
		return Collections.emptyList();
	}

}
