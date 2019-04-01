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

package org.opentravel.upversion;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

import java.util.List;

/**
 * Visitor that remaps all references from the previous version of a library to the new version.
 */
public class UpversionReferenceVisitor extends ModelElementVisitorAdapter {

    private UpversionRegistry registry;

    /**
     * Constructor that provides the mappings from old to new library and entity versions.
     * 
     * @param registry the upversion registry
     */
    public UpversionReferenceVisitor(UpversionRegistry registry) {
        this.registry = registry;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public boolean visitAttribute(TLAttribute attribute) {
        NamedEntity newVersionType = registry.getNewVersion( attribute.getType() );

        if (newVersionType != null) {
            attribute.setType( (TLPropertyType) newVersionType );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public boolean visitElement(TLProperty element) {
        NamedEntity newVersionType = registry.getNewVersion( element.getType() );

        if (newVersionType != null) {
            element.setType( (TLPropertyType) newVersionType );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
     */
    @Override
    public boolean visitExtension(TLExtension extension) {
        NamedEntity newVersionExtends = registry.getNewVersion( extension.getExtendsEntity() );

        if (newVersionExtends != null) {
            extension.setExtendsEntity( newVersionExtends );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
     */
    @Override
    public boolean visitSimple(TLSimple simple) {
        NamedEntity newVersionBase = registry.getNewVersion( simple.getParentType() );

        if (newVersionBase != null) {
            simple.setParentType( (TLAttributeType) newVersionBase );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
     */
    @Override
    public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
        NamedEntity newVersionBase = registry.getNewVersion( simpleFacet.getSimpleType() );

        if (newVersionBase != null) {
            simpleFacet.setSimpleType( (TLAttributeType) newVersionBase );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
        NamedEntity newVersionBase = registry.getNewVersion( vwa.getParentType() );

        if (newVersionBase != null) {
            vwa.setParentType( (TLAttributeType) newVersionBase );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
     */
    @Override
    public boolean visitContextualFacet(TLContextualFacet facet) {
        NamedEntity newVersionOwner = registry.getNewVersion( facet.getOwningEntity() );

        if (newVersionOwner != null) {
            facet.setOwningEntity( (TLFacetOwner) newVersionOwner );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
     */
    @Override
    public boolean visitResource(TLResource resource) {
        NamedEntity newVersionBO = registry.getNewVersion( resource.getBusinessObjectRef() );

        if (newVersionBO != null) {
            resource.setBusinessObjectRef( (TLBusinessObject) newVersionBO );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
     */
    @Override
    public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
        NamedEntity newParentResource = registry.getNewVersion( parentRef.getParentResource() );

        if (newParentResource != null) {
            parentRef.setParentResource( (TLResource) newParentResource );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
     */
    @Override
    public boolean visitParamGroup(TLParamGroup paramGroup) {
        TLFacet newFacetRef = (TLFacet) registry.getNewVersion( paramGroup.getFacetRef() );
        TLFacet facetRef = (newFacetRef != null) ? newFacetRef : paramGroup.getFacetRef();

        if (newFacetRef != null) {
            paramGroup.setFacetRef( (TLFacet) newFacetRef );
        }

        // Also
        if (facetRef != null) {
            assignParameterFieldRefs( paramGroup, facetRef );
        }
        return true;
    }

    /**
     * Ensure that all of the parameter references are pointing to the new-version fields. This does not seem to cause
     * problems in the resulting OTM files except for some unnecessary import statements.
     * 
     * @param paramGroup the group for which to assign paraameter field references
     * @param facetRef the facet reference of the parameter group
     */
    private void assignParameterFieldRefs(TLParamGroup paramGroup, TLFacet facetRef) {
        List<TLMemberField<TLMemberFieldOwner>> newFacetFields =
            ResourceCodegenUtils.getEligibleParameterFields( facetRef );

        for (TLParameter parameter : paramGroup.getParameters()) {
            String fieldName = parameter.getFieldRefName();

            if (fieldName != null) {
                TLMemberField<?> newField = null;

                for (TLMemberField<?> aField : newFacetFields) {
                    if (aField.getName().equals( fieldName )) {
                        newField = aField;
                    }
                }
                parameter.setFieldRef( newField );
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
     */
    @Override
    public boolean visitActionFacet(TLActionFacet facet) {
        NamedEntity newVersionBasePayload = registry.getNewVersion( facet.getBasePayload() );

        if (newVersionBasePayload != null) {
            facet.setBasePayload( newVersionBasePayload );
        }
        return true;
    }

}
