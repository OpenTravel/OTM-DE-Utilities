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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;

/**
 * @author dmh
 *
 */
public class OtmFacetFactory {
    private static Logger log = LogManager.getLogger( OtmFacetFactory.class );

    private OtmFacetFactory() {
        // NO-OP - only static methods
    }

    /**
     * Create a new library member from the contextual facet.
     * 
     * To create contributed facets {@link #create(TLFacet, OtmLibraryMember)}
     * 
     * @param tlFacet
     * @param manager
     * @return
     */
    public static OtmLibraryMember create(TLContextualFacet tlFacet, OtmModelManager manager) {
        OtmContextualFacet facet = null;
        switch (tlFacet.getFacetType()) {
            case CHOICE:
                facet = new OtmChoiceFacet( tlFacet, manager );
                break;
            case CUSTOM:
                facet = new OtmCustomFacet( tlFacet, manager );
                break;
            case QUERY:
                facet = new OtmQueryFacet( tlFacet, manager );
                break;
            case UPDATE:
                facet = new OtmUpdateFacet( tlFacet, manager );
                break;

            case SUMMARY:
            case DETAIL:
            case SHARED:
            case ID:
            case SIMPLE:
            default:
                log.debug( "Un-handled facet factory case: " + tlFacet.getFacetType() );
                break;
        }

        if (facet != null)
            facet.modelChildren();

        return facet;
    }

    /**
     * Create a new Facet that is a child of a library member; either ID, Summary, Detail, Shared or contributed.
     * 
     * To create a contextual facet as a library member use
     * {@link OtmFacetFactory#create(TLContextualFacet, OtmModelManager)}
     * 
     * @param tlFacet
     * @param parent
     * @return
     */
    public static OtmFacet<?> create(TLFacet tlFacet, OtmLibraryMember parent) {

        // If it was previously modeled, use that otm facade.
        if (OtmModelElement.get( tlFacet ) instanceof OtmFacet)
            return (OtmFacet<?>) OtmModelElement.get( tlFacet );

        OtmFacet<?> facet = null;
        switch (tlFacet.getFacetType()) {
            case SUMMARY:
                if (parent instanceof OtmComplexObjects)
                    facet = new OtmSummaryFacet( tlFacet, (OtmComplexObjects<?>) parent );
                break;
            case DETAIL:
                if (parent instanceof OtmComplexObjects)
                    facet = new OtmDetailFacet( tlFacet, (OtmComplexObjects<?>) parent );
                break;
            case SHARED:
                if (parent instanceof OtmComplexObjects)
                    facet = new OtmSharedFacet( tlFacet, (OtmComplexObjects<?>) parent );
                break;
            case ID:
                if (parent instanceof OtmComplexObjects)
                    facet = new OtmIdFacet( tlFacet, (OtmComplexObjects<?>) parent );
                break;
            case CHOICE:
            case CUSTOM:
            case UPDATE:
            case QUERY:
                // 1/26/2021 - changing the contributed facet was causing debugging problems.
                // Contextual facets are fundamentally different than the facets above.
                // Contextual facets have to OTM Facades: one as a library member and one as a child where injected.
                // This factory produces the child facade.
                // The LibraryMemberFactory uses OtmFacetFactory#create(TLContextualFacet, OtmModelManager) to produce
                // the library member.

                // Try to recover contributed facet from listener.
                OtmObject obj = OtmModelElement.get( tlFacet );
                if (obj instanceof OtmContextualFacet)
                    facet = ((OtmContextualFacet) obj).getExistingContributed();
                else if (obj instanceof OtmContributedFacet)
                    facet = (OtmContributedFacet) obj;

                // If not recovered, create a new contributed facet.
                if (facet == null && parent instanceof OtmLibraryMember && tlFacet instanceof TLContextualFacet)
                    facet = new OtmContributedFacet( (TLContextualFacet) tlFacet, parent );
                break;
            case SIMPLE:
            default:
                log.debug( "Un-handled facet factory case: " + tlFacet.getFacetType() );
                break;
        }

        if (facet != null)
            facet.modelChildren();

        return facet;

    }

    public static String getObjectName(OtmFacet<?> member) {
        if (member instanceof OtmDetailFacet)
            return "Detail Facet";
        if (member instanceof OtmIdFacet)
            return "ID Facet";
        if (member instanceof OtmSharedFacet)
            return "Shared Facet";
        if (member instanceof OtmSummaryFacet)
            return "Summary Facet";
        return member.getClass().getSimpleName();
    }
}
