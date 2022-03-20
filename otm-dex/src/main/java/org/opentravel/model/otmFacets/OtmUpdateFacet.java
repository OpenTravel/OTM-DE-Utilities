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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * OTM Node for Update contextual Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmUpdateFacet extends OtmContextualFacet {
    private static Logger log = LogManager.getLogger( OtmUpdateFacet.class );

    /**
     */
    public OtmUpdateFacet(TLContextualFacet tl, OtmModelManager manager) {
        super( tl, manager );

        if (tl.getFacetType() != TLFacetType.UPDATE)
            throw new IllegalArgumentException(
                "Tried to create update facet from wrong facet type: " + tl.getFacetType() );
    }

    @Override
    public String setName(String text) {
        return null;
    }

    @Override
    protected void setOwningEntity(OtmLibraryMember owner) {
        super.setOwningEntity( owner );
        if (owner.getTL() instanceof TLBusinessObject)
            ((TLBusinessObject) owner.getTL()).addUpdateFacet( getTL() );
    }


}
