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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Tree node that encapsulates a single node in the original DOM tree structure.
 */
public class DOMTreeOriginalNode extends AbstractDOMTreeNode {
	
	protected static final String IS_REFERENCED_KEY = "isReferenced";
	
	/**
	 * Constructor that specifies the DOM node and all required configuration information.
	 * 
	 * @param domNode  the DOM node instance
	 */
	public DOMTreeOriginalNode(Node domNode) {
		super(domNode, null);
	}
	
	/**
	 * Constructs a new tree of <code>DOMTreeOriginalNode</code> objects based on the
	 * DOM node provided.
	 * 
	 * @param domNode  the DOM node instance for which to create a tree
	 * @return TreeItem<DOMTreeOriginalNode>
	 */
	public static TreeItem<DOMTreeOriginalNode> createTree(Node domNode) {
		DOMTreeOriginalNode odn = new DOMTreeOriginalNode( domNode );
		TreeItem<DOMTreeOriginalNode> treeItem = new TreeItem<>( odn );
		Node domChild = domNode.getFirstChild();
		Image nodeImage;
		
		if (domNode instanceof Element) {
			NamedNodeMap attrs = ((Element) domNode).getAttributes();
			int attrCount = attrs.getLength();
			
			for (int i = 0; i < attrCount; i++) {
				Attr domAttr = (Attr) attrs.item( i );
				
				if (!domAttr.getNodeName().equals("xmlns")
						&& !domAttr.getNodeName().contains(":")) {
					treeItem.getChildren().add( createTree( domAttr ) );
				}
			}
			nodeImage = elementIcon;
			
		} else { // must be an attribute
			nodeImage = attributeIcon;
		}
		treeItem.setGraphic( new ImageView( nodeImage ) );
		
		while (domChild != null) {
			if (domChild.getNodeType() == Node.ELEMENT_NODE) {
				treeItem.getChildren().add( createTree( domChild ) );
			}
			domChild = domChild.getNextSibling();
		}
		return treeItem;
	}
	
	/**
	 * Returns the reference status for this node.
	 * 
	 * @return ReferenceStatus
	 */
	public ReferenceStatus getReferenceStatus() {
		Node domNode = getDomNode();
		Boolean refFlag = (domNode == null) ?
				null : (Boolean) domNode.getUserData( IS_REFERENCED_KEY );
		ReferenceStatus result;
		
		if ((refFlag != null) && refFlag) {
			result = ReferenceStatus.REFERENCED;
		} else {
			result = ReferenceStatus.NOT_REFERENCED;
		}
		return result;
	}
	
}
