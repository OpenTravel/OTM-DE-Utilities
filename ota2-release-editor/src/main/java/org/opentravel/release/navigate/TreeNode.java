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

import org.opentravel.release.NodeProperty;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;

import java.util.List;

import javafx.scene.image.Image;

/**
 * Represents a single node in the tree view for the selected library.
 */
public abstract class TreeNode<E> {

    protected TreeNodeFactory treeNodeFactory;
    private List<TreeNode<Object>> children;
    private E entity;

    /**
     * Constructor that specifies the OTM entity for this node.
     * 
     * @param entity the OTM entity represented by this node
     * @param factory the factory that created this node
     */
    public TreeNode(E entity, TreeNodeFactory factory) {
        this.entity = entity;
        this.treeNodeFactory = factory;
    }

    /**
     * Returns the the OTM entity represented by this node.
     *
     * @return E
     */
    public E getEntity() {
        return entity;
    }

    /**
     * Returns the label for this tree node.
     * 
     * @return String
     */
    public abstract String getLabel();

    /**
     * Returns the icon for this tree node.
     * 
     * @return Image
     */
    public abstract Image getIcon();

    /**
     * Returns the list of properties associated with the OTM entity associated with this node.
     * 
     * @return List&lt;NodeProperty&gt;
     */
    public abstract List<NodeProperty> getProperties();

    /**
     * Initializes the list of children for this node.
     * 
     * @return List&lt;TreeNode&lt;?&gt;&gt;
     */
    protected abstract List<TreeNode<Object>> initializeChildren();

    /**
     * Returns true if the children of this node are to be sorted in their natural ascending order. Default is true;
     * subclasses may override.
     * 
     * @return boolean
     */
    public boolean sortChildren() {
        return true;
    }

    /**
     * Returns the children of this tree node.
     * 
     * @return List&lt;TreeNode&lt;?&gt;&gt;
     */
    public List<TreeNode<Object>> getChildren() {
        if (children == null) {
            children = initializeChildren();
        }
        return children;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * Returns the DESCRIPTION of given model element or an empty string if no DESCRIPTION has been defined.
     * 
     * @param docOwner the model element for which to return a DESCRIPTION
     * @return String
     */
    protected String getDescription(TLDocumentationOwner docOwner) {
        TLDocumentation doc = docOwner.getDocumentation();
        String docText = (doc == null) ? null : doc.getDescription();

        return (docText == null) ? "" : docText;
    }

    /**
     * Returns a display name for the given library.
     * 
     * @param library the library for which to return a display name
     * @return String
     */
    protected String getLibraryDisplayName(AbstractLibrary library) {
        String displayName = "N/A";

        if (entity != null) {
            displayName = library.getPrefix() + ":" + library.getName();
        }
        return displayName;
    }

    /**
     * Returns a display name for the given entity.
     * 
     * @param entity the entity for which to return a display name
     * @return String
     */
    protected String getEntityDisplayName(NamedEntity entity) {
        String displayName = "N/A";

        if (entity != null) {
            String prefix = entity.getOwningLibrary().getPrefix();
            displayName = prefix + ":" + entity.getLocalName();
        }
        return displayName;
    }

    /**
     * Returns the name of the entity that the given extension owner extends.
     * 
     * @param extOwner the owner for which to return an extension
     * @return String
     */
    protected String getExtensionName(TLExtensionOwner extOwner) {
        TLExtension extension = (extOwner == null) ? null : extOwner.getExtension();
        return (extension == null) ? "N/A" : getEntityDisplayName( extension.getExtendsEntity() );
    }

}
