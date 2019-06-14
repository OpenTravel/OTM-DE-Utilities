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

import org.opentravel.release.navigate.impl.AbstractFacetTreeNode.ContextualFacetTreeNode;
import org.opentravel.release.navigate.impl.AbstractFacetTreeNode.FacetTreeNode;
import org.opentravel.release.navigate.impl.ActionFacetTreeNode;
import org.opentravel.release.navigate.impl.ActionRequestTreeNode;
import org.opentravel.release.navigate.impl.ActionResponseTreeNode;
import org.opentravel.release.navigate.impl.ActionTreeNode;
import org.opentravel.release.navigate.impl.AliasTreeNode;
import org.opentravel.release.navigate.impl.AttributeTreeNode;
import org.opentravel.release.navigate.impl.BusinessObjectTreeNode;
import org.opentravel.release.navigate.impl.ChoiceObjectTreeNode;
import org.opentravel.release.navigate.impl.CoreObjectTreeNode;
import org.opentravel.release.navigate.impl.ElementTreeNode;
import org.opentravel.release.navigate.impl.EnumerationTreeNode.ClosedEnumerationTreeNode;
import org.opentravel.release.navigate.impl.EnumerationTreeNode.OpenEnumerationTreeNode;
import org.opentravel.release.navigate.impl.ExtensionPointFacetTreeNode;
import org.opentravel.release.navigate.impl.IndicatorTreeNode;
import org.opentravel.release.navigate.impl.LibraryTreeNode;
import org.opentravel.release.navigate.impl.OperationTreeNode;
import org.opentravel.release.navigate.impl.ParamGroupTreeNode;
import org.opentravel.release.navigate.impl.ParameterTreeNode;
import org.opentravel.release.navigate.impl.ParentRefTreeNode;
import org.opentravel.release.navigate.impl.ResourceTreeNode;
import org.opentravel.release.navigate.impl.ServiceTreeNode;
import org.opentravel.release.navigate.impl.SimpleTreeNode;
import org.opentravel.release.navigate.impl.ValueWithAttributesTreeNode;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Enumeration that indicates the type of a tree node.
 */
public enum TreeNodeType {

    LIBRARY_TREE_NODE(TLLibrary.class, LibraryTreeNode.class),
    SIMPLE_TREE_NODE(TLSimple.class, SimpleTreeNode.class),
    CLOSED_ENUM_TREE_NODE(TLClosedEnumeration.class, ClosedEnumerationTreeNode.class),
    OPEN_ENUM_TREE_NODE(TLOpenEnumeration.class, OpenEnumerationTreeNode.class),
    VWA_TREE_NODE(TLValueWithAttributes.class, ValueWithAttributesTreeNode.class),
    CORE_OBJ_TREE_NODE(TLCoreObject.class, CoreObjectTreeNode.class),
    CHOICE_OBJ_TREE_NODE(TLChoiceObject.class, ChoiceObjectTreeNode.class),
    BUSINESS_OBJ_TREE_NODE(TLBusinessObject.class, BusinessObjectTreeNode.class),
    ALIAS_TREE_NODE(TLAlias.class, AliasTreeNode.class),
    FACET_TREE_NODE(TLFacet.class, FacetTreeNode.class),
    CTX_FACET_TREE_NODE(TLContextualFacet.class, ContextualFacetTreeNode.class),
    EP_FACET_TREE_NODE(TLExtensionPointFacet.class, ExtensionPointFacetTreeNode.class),
    ATTRIBUTE_TREE_NODE(TLAttribute.class, AttributeTreeNode.class),
    ELEMENT_TREE_NODE(TLProperty.class, ElementTreeNode.class),
    INDICATOR_TREE_NODE(TLIndicator.class, IndicatorTreeNode.class),
    SERVICE_TREE_NODE(TLService.class, ServiceTreeNode.class),
    OPERATION_TREE_NODE(TLOperation.class, OperationTreeNode.class),
    RESOURCE_TREE_NODE(TLResource.class, ResourceTreeNode.class),
    PARENT_REF_TREE_NODE(TLResourceParentRef.class, ParentRefTreeNode.class),
    PARAM_GROUP_TREE_NODE(TLParamGroup.class, ParamGroupTreeNode.class),
    PARAMETER_TREE_NODE(TLParameter.class, ParameterTreeNode.class),
    ACTION_FACET_TREE_NODE(TLActionFacet.class, ActionFacetTreeNode.class),
    ACTION_TREE_NODE(TLAction.class, ActionTreeNode.class),
    ACTION_REQUEST_TREE_NODE(TLActionRequest.class, ActionRequestTreeNode.class),
    ACTION_RESPONSE_TREE_NODE(TLActionResponse.class, ActionResponseTreeNode.class);

    private Class<?> entityClass;
    private Class<? extends TreeNode<?>> nodeClass;

    /**
     * Constructor that specifies the associated entity type and the tree node implementation class.
     * 
     * @param entityClass the entity class associated with this tree node type
     * @param nodeClass the tree node implementation class for this type
     */
    private TreeNodeType(Class<?> entityClass, Class<? extends TreeNode<?>> nodeClass) {
        this.entityClass = entityClass;
        this.nodeClass = nodeClass;
    }

    /**
     * Returns the entity class associated with this tree node type.
     *
     * @return Class&lt;?&gt;
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Returns the tree node implementation class for this type.
     *
     * @return Class&lt;? extends TreeNode&lt;?&gt;&gt;
     */
    public Class<? extends TreeNode<?>> getNodeClass() {
        return nodeClass;
    }

    /**
     * Returns the tree node type associated with the given entity class.
     * 
     * @param entityClass the entity class for which to return a tree node type
     * @return TreeNodeType
     */
    public static TreeNodeType fromEntityType(Class<?> entityClass) {
        TreeNodeType result = null;

        for (TreeNodeType nodeType : values()) {
            if (entityClass.equals( nodeType.getEntityClass() )) {
                result = nodeType;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException( "Unknown tree node entity type: " + entityClass.getSimpleName() );
        }
        return result;
    }

}
