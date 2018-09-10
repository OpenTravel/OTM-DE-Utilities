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

package org.opentravel.exampleupgrade;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javafx.scene.image.Image;

/**
 * Tree node that encapsulates a single node from the a DOM tree structure.
 */
public abstract class AbstractDOMTreeNode {
	
	protected static final Image attributeIcon = new Image( AbstractDOMTreeNode.class.getResourceAsStream( "/images/nattrib.gif" ) );
	protected static final Image elementIcon = new Image( AbstractDOMTreeNode.class.getResourceAsStream( "/images/nelem.gif" ) );
	
	private Node domNode;
	private String label;
	
	/**
	 * Constructor that supplies the DOM attribute or element to be displayed.
	 * 
	 * @param domNode  the DOM node instance
	 * @param label  the label for the node (if not specified, a default label will be assigned)
	 */
	public AbstractDOMTreeNode(Node domNode, String label) {
		String nodeValue;
		
		if (domNode instanceof Attr) {
			nodeValue = ((Attr) domNode).getValue();
			
		} else { // must be an Element
			nodeValue = HelperUtils.getElementTextValue( (Element) domNode );
		}
		this.domNode = domNode;
		this.label = (label != null) ? label :
			(domNode.getNodeName() + ((nodeValue == null) ? "" : (" = " + nodeValue)));
	}
	
	/**
	 * Returns the DOM node instance.
	 *
	 * @return Node
	 */
	public Node getDomNode() {
		return domNode;
	}
	
	/**
	 * Returns the display label for this node.
	 *
	 * @return String
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return label;
	}
	
}
