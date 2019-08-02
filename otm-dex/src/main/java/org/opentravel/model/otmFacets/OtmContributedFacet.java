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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;

import java.util.Collection;
import java.util.Collections;

/**
 * Abstract OTM Node for Custom Facets (not library members) with a parent.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmContributedFacet extends OtmFacet<TLContextualFacet> {
    private static Log log = LogFactory.getLog( OtmContributedFacet.class );

    // The library member that defines this facet.
    private OtmContextualFacet contributor = null;

    /**
     */
    public OtmContributedFacet(TLContextualFacet tl, OtmLibraryMember parent) {
        super( tl, parent );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET_CONTRIBUTED;
    }

    public OtmContextualFacet getContributor() {
        if (contributor == null)
            contributor = (OtmContextualFacet) OtmModelElement.get( getTL() );
        return contributor;
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return contributor != null ? contributor.getDescendantsChildrenOwners() : Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return contributor != null ? contributor.getDescendantsTypeProviders() : Collections.emptyList();
    }

    @Override
    public OtmLibrary getLibrary() {
        return contributor != null ? contributor.getLibrary() : null;
    }

    @Override
    public TLContextualFacet getTL() {
        return (TLContextualFacet) tlObject;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Contributor will not have this set as where-contributed if it is an inherited "ghost" facet
     */
    @Override
    public boolean isInherited() {
        // log.debug("Is " + this + " inherited? " );
        if (getContributor() == null)
            return false;
        return getContributor().getWhereContributed() != this;
    }

    /**
     * @param otmContextualFacet
     */
    public void setContributor(OtmContextualFacet contextualFacet) {
        contributor = contextualFacet;
    }

}
