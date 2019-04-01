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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.w3c.dom.Node;

/**
 * Tree node that encapsulates a single node in the upgraded DOM tree structure.
 */
public class DOMTreeUpgradeNode extends AbstractDOMTreeNode {

    private NamedEntity otmEntity;
    private NamedEntity declaredEntity;
    private TLMemberField<?> otmField;
    private ExampleMatchType matchType;

    /**
     * Constructor used to create a node representing an OTM entity. The entity can be presumed to be an element that is
     * a complex type with child elements and/or attributes.
     * 
     * @param otmEntity the OTM complex entity type associated with this node
     * @param declaredEntity the entity type originally declared in the owning OTM element
     * @param domNode the DOM node instance
     * @param matchType indicates the match type of the OTM entity with the original EXAMPLE
     */
    public DOMTreeUpgradeNode(NamedEntity otmEntity, NamedEntity declaredEntity, Node domNode,
        ExampleMatchType matchType) {
        super( domNode, (matchType == ExampleMatchType.MISSING) ? (domNode.getNodeName() + " [MISSING]") : null );
        this.otmEntity = otmEntity;
        this.declaredEntity = declaredEntity;
        this.matchType = matchType;
    }

    /**
     * Constructor used to create a node representing an OTM field (attribute, element, or indicator) with a simple
     * EXAMPLE value.
     * 
     * @param otmField the OTM field associated with this node
     * @param domNode the DOM node instance
     * @param matchType indicates the match type of the OTM field with the original EXAMPLE
     */
    public DOMTreeUpgradeNode(TLMemberField<?> otmField, Node domNode, ExampleMatchType matchType) {
        super( domNode, (matchType == ExampleMatchType.MISSING) ? (domNode.getNodeName() + " [MISSING]") : null );
        this.otmField = otmField;
        this.matchType = matchType;
    }

    /**
     * Returns the OTM complex entity type associated with this node.
     *
     * @return NamedEntity
     */
    public NamedEntity getOtmEntity() {
        return otmEntity;
    }

    /**
     * Returns the entity type originally declared in the owning OTM element.
     *
     * @return NamedEntity
     */
    public NamedEntity getDeclaredEntity() {
        return declaredEntity;
    }

    /**
     * Returns the OTM field associated with this node.
     *
     * @return TLMemberField&lt;TLMemberFieldOwner&gt;
     */
    @SuppressWarnings("unchecked")
    public TLMemberField<TLMemberFieldOwner> getOtmField() {
        return (TLMemberField<TLMemberFieldOwner>) otmField;
    }

    /**
     * Returns the flag indicating the match type of the OTM entity or field with the original EXAMPLE.
     *
     * @return ExampleMatchType
     */
    public ExampleMatchType getMatchType() {
        return matchType;
    }

    /**
     * Assigns the flag indicating the match type of the OTM entity or field with the original EXAMPLE.
     *
     * @param matchType the match type value to assign
     */
    public void setMatchType(ExampleMatchType matchType) {
        this.matchType = matchType;
    }

    /**
     * Returns true if this node is associated with an OTM entity, false if it is associated with a field.
     * 
     * @return boolean
     */
    public boolean isEntityNode() {
        return (otmEntity != null);
    }

}
