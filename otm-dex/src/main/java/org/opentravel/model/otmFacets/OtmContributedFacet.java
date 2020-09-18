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
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLContextualFacet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract OTM Node for Contributed Facets (not library members).
 * <p>
 * Contributed facets have:
 * <ul>
 * <li>a parent that identifies where the facet is contributed (injected).
 * <li>a contributor that identifies the contextual facet library member.
 * <li>the same TL contextual facet as the contributor contextual facet.
 * </ul>
 * <p>
 * Access to data maintained on the contributor:
 * <ul>
 * <li>All children can be accessed from here but are maintained on the contextual facet. {@link #add(OtmObject)} is a
 * facade for getContributor().add(child).
 * <li>Name
 * </ul>
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

        // If the contextual facet has been modeled, set its contributor
        OtmObject obj = OtmModelElement.get( tl );
        if (obj instanceof OtmContextualFacet) {
            setContributor( (OtmContextualFacet) obj );
            ((OtmContextualFacet) obj).setWhereContributed( this );
        }
    }

    /**
     * Create contributed facet with injection point (parent) and contributor set. Does <b>not</b> add this to the
     * parent. (it can't because it thinks it is inherited until CF where contributed is set.)
     * 
     * @param parent object where contributed (injection point)
     * @param contributor OTM with TL facet that is contributed
     */
    public OtmContributedFacet(OtmLibraryMember parent, OtmContextualFacet contributor) {
        super( contributor.getTL(), parent );
        setContributor( contributor );
    }

    @Override
    public String setName(String name) {
        if (getContributor() == null)
            return "";
        return getContributor().setName( name );
    }

    /**
     * Set the parent, contributor and tlObject fields. Add this to parent. When set to null, the facet is no longer
     * contributed to an object.
     * 
     * @param parent is the object where the contextual facet is contributed (injected)
     * @param cf provides the TLContextualFacet to set into the TL Object
     */
    public void setParent(OtmLibraryMember parent, OtmContextualFacet cf) {
        if (getParent() != null)
            getParent().delete( cf );
        this.parent = parent;
        parent.add( this );
        tlObject = cf.getTL();
        setContributor( cf );
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note: </b> Contextual facets can <b>not</b> be added to the contributed facet. They must be directly added to
     * the contextual facet.
     * 
     * @param child the OtmProperty to be added to the contextual facet.
     * @see org.opentravel.model.otmFacets.OtmAbstractFacet#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmProperty add(OtmObject child) {
        if (getContributor() != null && child instanceof OtmProperty)
            return (OtmProperty) getContributor().add( child );
        return null;
    }

    /**
     * {@inheritDoc} Clear this name property and the contributor's name property.
     * 
     * @see org.opentravel.model.OtmModelElement#clearNameProperty()
     */
    @Override
    public void clearNameProperty() {
        super.clearNameProperty(); // null out this name property
        if (getContributor() != null)
            getContributor().clearNameProperty();
    }

    /**
     * Children are maintained on the contextual facet not the contributed. Children must be maintained even when a
     * contextual facet is not injected into an object.
     * 
     */
    @Override
    public List<OtmObject> getChildren() {
        if (getContributor() != null)
            return getContributor().getChildren();
        return Collections.emptyList();
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

    @Override
    public boolean isRenameable() {
        return true;
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
     * {@inheritDoc}
     * <p>
     * Contributed facets do not have children. Do nothing.
     */
    @Override
    public void modelChildren() {
        // No-Op
    }

    @Override
    public void delete(OtmObject property) {
        if (getContributor() != null)
            getContributor().delete( property );
        else
            log.warn( "Could not delete property." );
    }

    @Override
    public void remove(OtmObject property) {
        if (getContributor() != null)
            getContributor().remove( property );
        else
            log.warn( "Could not remove property." );
    }


    /**
     * Simply set the contributor field.
     * 
     * @param otmContextualFacet
     */
    public void setContributor(OtmContextualFacet contextualFacet) {
        contributor = contextualFacet;
    }
}
