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
import org.opentravel.model.OtmModelManager;
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
    private static Log log = LogFactory.getLog( OtmFacetFactory.class );

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
        return create( tlFacet, parent, null );
    }

    // Use the base as the source of the contributed facet
    public static OtmFacet<?> create(TLFacet tlFacet, OtmLibraryMember parent, OtmLibraryMember base) {
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
                if (parent instanceof OtmLibraryMember && tlFacet instanceof TLContextualFacet)
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
}
