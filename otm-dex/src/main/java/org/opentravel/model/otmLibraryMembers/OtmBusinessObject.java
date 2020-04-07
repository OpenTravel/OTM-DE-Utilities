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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmIdFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmFacets.OtmUpdateFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * OTM Object Node for business objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmBusinessObject extends OtmComplexObjects<TLBusinessObject> {
    private static Log log = LogFactory.getLog( OtmBusinessObject.class );

    /**
     * Construct business object library member. Set its model manager, TL object and add a listener.
     * 
     * @param tlo
     * @param mgr
     */
    public OtmBusinessObject(TLBusinessObject tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmBusinessObject(String name, OtmModelManager mgr) {
        super( new TLBusinessObject(), mgr );
        setName( name );
    }

    // FUTURE - how to add documentation to ???
    // public void junk() {
    // String docPath = DocumentationPathBuilder.buildPath( getTL());
    //
    // }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Does</b> add contextual facets to the TL object
     * 
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmContextualFacet)
            return add( (OtmContextualFacet) child );
        else {
            OtmObject result = null;
            result = super.add( child );
            if (child instanceof OtmContributedFacet) {
                ((OtmContributedFacet) child).getTL().setOwningEntity( getTL() );
            }
            return result;
        }
    }

    /**
     * Add the contextual facet library member to this business object. A contributed facet is created and added to the
     * children.
     * 
     * @param cf contextual facet that is a library member
     * @return new contributed facet
     */
    public OtmContributedFacet add(OtmContextualFacet cf) {
        if (cf instanceof OtmCustomFacet)
            getTL().addCustomFacet( cf.getTL() );
        else if (cf instanceof OtmQueryFacet)
            getTL().addQueryFacet( cf.getTL() );
        else if (cf instanceof OtmUpdateFacet)
            getTL().addUpdateFacet( cf.getTL() );
        else
            return null;

        // Creating the contributed facet will link the contributed and contributor via the TL facet.
        OtmContributedFacet contrib = new OtmContributedFacet( cf.getTL(), this );
        super.add( contrib );
        return contrib;
    }

    public OtmIdFacet getIdFacet() {
        getChildren(); // Make sure it has been modeled.
        if (OtmModelElement.get( getTL().getIdFacet() ) instanceof OtmIdFacet)
            return (OtmIdFacet) (OtmModelElement.get( getTL().getIdFacet() ));
        return null;
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        return super.setName( name );
    }

    @Override
    public TLBusinessObject getTL() {
        return (TLBusinessObject) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.BUSINESS;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        children.forEach( c -> {
            if (c instanceof OtmIdFacet)
                ch.add( c );
            if (c instanceof OtmAlias)
                ch.add( c );
            if (c instanceof OtmContributedFacet && ((OtmContributedFacet) c).getContributor() instanceof OtmQueryFacet)
                ch.add( c );
        } );
        getInheritedChildren().forEach( c -> {
            if (c instanceof OtmContributedFacet && ((OtmContributedFacet) c).getContributor() instanceof OtmQueryFacet)
                ch.add( c );
        } );
        return ch;
    }

    /**
     * {@inheritDoc} Also remove any contributed facets.
     */
    @Override
    public OtmBusinessObject copy() {
        OtmLibraryMember copy = super.copy();

        // Remove any contributed facets
        Collection<OtmContributedFacet> contribs = copy.getChildrenContributedFacets();
        for (OtmContributedFacet contrib : contribs) {
            copy.delete( contrib );
        }

        // if (!copy.getChildrenContributedFacets().isEmpty())
        // log.warn( "Error - still has contributed facets." );
        // if (!((TLBusinessObject) copy.getTL()).getCustomFacets().isEmpty())
        // log.warn( "Error - still has custom facets." );

        log.debug( "Copied a business object." + copy.getChildren() );
        return (OtmBusinessObject) copy;
    }

    @Override
    public void delete(OtmObject child) {
        super.delete( child ); // will delete aliases
        TLContextualFacet tlFacet = null;
        if (child instanceof OtmContributedFacet) {
            tlFacet = ((OtmContributedFacet) child).getTL();
            remove( child );
            child = ((OtmContributedFacet) child).getContributor();
        }
        if (tlFacet == null && child instanceof OtmContextualFacet)
            tlFacet = ((OtmContextualFacet) child).getTL();
        remove( child );
        // Get TL - Use TLFacetType - tl from either contributor or contributed
        if (tlFacet == null)
            return;
        switch (tlFacet.getFacetType()) {
            case CUSTOM:
                getTL().removeCustomFacet( tlFacet );
                break;
            case QUERY:
                getTL().removeQueryFacet( tlFacet );
                break;
            case UPDATE:
                getTL().removeUpdateFacet( tlFacet );
                break;
            default:
                // Not remove-able
                break;
        }
        // if (child.getTL() instanceof TLContextualFacet) {
        // if (child instanceof OtmCustomFacet)
        // getTL().removeCustomFacet( (TLContextualFacet) child.getTL() );
        // else if (child instanceof OtmQueryFacet)
        // getTL().removeQueryFacet( (TLContextualFacet) child.getTL() );
        // else if (child instanceof OtmUpdateFacet)
        // getTL().removeUpdateFacet( (TLContextualFacet) child.getTL() );
        // }
    }
}
