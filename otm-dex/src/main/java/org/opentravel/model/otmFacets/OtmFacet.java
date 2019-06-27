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

package org.opentravel.model.otmFacets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;

/**
 * Abstract OTM Node for Facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmFacet<T extends TLFacet> extends OtmAbstractFacet<TLFacet>
    implements OtmPropertyOwner, OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmFacet.class );

    public OtmFacet(T tl, OtmLibraryMember parent) {
        super( tl, parent );
    }

    /**
     * Create a facet for OtmOperations which are not library members
     * 
     * @param tl
     * @param actionMgr
     */
    // The only time this is used is for operations - operationFacet/operations which are not library members
    public OtmFacet(T tl) {
        super( tl );
    }

    @Override
    public TLFacet getTL() {
        return (TLFacet) tlObject;
    }


    @Override
    public OtmProperty<?> add(TLModelElement tl) {
        if (tl instanceof TLIndicator)
            getTL().addIndicator( (TLIndicator) tl );
        else if (tl instanceof TLProperty)
            getTL().addElement( (TLProperty) tl );
        else if (tl instanceof TLAttribute)
            getTL().addAttribute( (TLAttribute) tl );
        else
            log.debug( "unknown/not-implemented property type." );

        return OtmPropertyFactory.create( tl, this );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates properties to represent facet children.
     */
    @Override
    public void modelChildren() {
        getTL().getIndicators().forEach( i -> OtmPropertyFactory.create( i, this ) );
        getTL().getAttributes().forEach( i -> OtmPropertyFactory.create( i, this ) );
        getTL().getElements().forEach( i -> OtmPropertyFactory.create( i, this ) );
    }

    @Override
    public void modelInheritedChildren() {
        // Only model once
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // RE-model
        // return;

        // All properties, local and inherited
        // List<TLProperty> inheritedElements = PropertyCodegenUtils.getInheritedProperties(getTL());

        // Get only the directly inherited properties
        if (getOwningMember().getBaseType() != null) {
            PropertyCodegenUtils.getInheritedFacetProperties( getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
            PropertyCodegenUtils.getInheritedFacetAttributes( getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
            PropertyCodegenUtils.getInheritedFacetIndicators( getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );

        }
    }

}
