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

package org.opentravel.model.otmLibraryMembers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmFacetFactory;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * @author dmh
 *
 */
public class OtmLibraryMemberFactory {
    private static Log log = LogFactory.getLog( OtmLibraryMemberFactory.class );

    private OtmLibraryMemberFactory() {
        // NO-OP - only static methods
    }

    public static OtmLibraryMember memberFactory(LibraryMember tlMember, OtmModelManager manager) {
        if (manager == null)
            throw new IllegalArgumentException( "Member factory must be passed a non-null manager." );
        // log.debug("Ready to create member for: " + tlMember.getLocalName() + " of type "
        // + tlMember.getClass().getSimpleName());

        OtmLibraryMember otmMember = null;
        if (tlMember instanceof TLBusinessObject)
            otmMember = new OtmBusinessObject( (TLBusinessObject) tlMember, manager );
        else if (tlMember instanceof TLChoiceObject)
            otmMember = new OtmChoiceObject( (TLChoiceObject) tlMember, manager );
        else if (tlMember instanceof TLCoreObject)
            otmMember = new OtmCoreObject( (TLCoreObject) tlMember, manager );
        else if (tlMember instanceof TLSimple)
            otmMember = new OtmSimpleObject( (TLSimple) tlMember, manager );
        else if (tlMember instanceof TLOpenEnumeration)
            otmMember = new OtmEnumerationOpen( (TLOpenEnumeration) tlMember, manager );
        else if (tlMember instanceof TLClosedEnumeration)
            otmMember = new OtmEnumerationClosed( (TLClosedEnumeration) tlMember, manager );
        else if (tlMember instanceof TLContextualFacet)
            otmMember = OtmFacetFactory.create( (TLContextualFacet) tlMember, manager );
        else if (tlMember instanceof TLValueWithAttributes)
            otmMember = new OtmValueWithAttributes( (TLValueWithAttributes) tlMember, manager );
        else if (tlMember instanceof XSDSimpleType)
            otmMember = new OtmXsdSimple( (XSDSimpleType) tlMember, manager );
        else if (tlMember instanceof XSDElement)
            otmMember = new OtmXsdElement( (XSDElement) tlMember, manager );
        else if (tlMember instanceof TLService)
            otmMember = new OtmServiceObject( (TLService) tlMember, manager );
        else if (tlMember instanceof TLResource)
            otmMember = new OtmResourceObject( (TLResource) tlMember, manager );
        else if (tlMember instanceof XSDComplexType)
            // Skip Complex Types: for the built-ins they will have an associated element
            otmMember = null; // NO-OP
        else
            log.debug( "TODO - model " + tlMember.getClass().getSimpleName() );

        manager.add( otmMember );
        return otmMember;
    }

    public static String getObjectName(OtmLibraryMember member) {
        if (member instanceof OtmBusinessObject)
            return "Business Object";
        if (member instanceof OtmChoiceObject)
            return "Choice Object";
        if (member instanceof OtmCoreObject)
            return "Core Object";
        if (member instanceof OtmValueWithAttributes)
            return "Value With Attributes";
        if (member instanceof OtmSimpleObject)
            return "Simple Object";
        if (member instanceof OtmEnumerationClosed)
            return "Closed Enumeration";
        if (member instanceof OtmEnumerationOpen)
            return "Open Enumeration";
        if (member instanceof OtmCustomFacet)
            return "Custom Facet";
        if (member instanceof OtmChoiceFacet)
            return "Choice Facet";
        if (member instanceof OtmQueryFacet)
            return "Query Facet";

        return member.getClass().getSimpleName();
    }
}
