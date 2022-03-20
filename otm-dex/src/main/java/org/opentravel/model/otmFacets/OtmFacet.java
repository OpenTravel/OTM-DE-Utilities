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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmIndicator;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyBase;
import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Abstract OTM Node for Facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmFacet<T extends TLFacet> extends OtmAbstractFacetPropertyOwner<TLFacet> {
    private static Logger log = LogManager.getLogger( OtmFacet.class );

    protected OtmLibraryMember parent;

    public OtmFacet(T tl, OtmLibraryMember parent) {
        super( tl );
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for all elements, attributes and indicators
     */
    @Override
    public boolean canAdd(OtmProperty property) {
        return property instanceof OtmPropertyBase<?>;
    }

    /**
     * Check the TL facet to assure it has the TL property.
     * <p>
     * This is the right way to check because the children list may get rebuilt with new facades.
     * 
     * @param facet
     * @param property
     * @return
     */
    public boolean contains(OtmObject property) {
        if (property instanceof OtmElement)
            return getTL().getElements().contains( property.getTL() );
        if (property instanceof OtmAttribute)
            return getTL().getAttributes().contains( property.getTL() );
        if (property instanceof OtmIndicator)
            return getTL().getIndicators().contains( property.getTL() );
        return false;
    }

    public DexActionManager getActionManger() {
        return parent.getActionManager();
    }

    public OtmLibraryMember getParent() {
        return parent;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return getParent();
    }

    @Override
    public String getObjectTypeName() {
        return OtmFacetFactory.getObjectName( this );
    }

    @Override
    public TLFacet getTL() {
        return (TLFacet) tlObject;
    }

}
