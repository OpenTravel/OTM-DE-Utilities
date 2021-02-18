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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelElementListener;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmAbstractFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.OtmFacetFactory;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyBase;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

/**
 * Abstract OTM Facade for all contextual facets. These are library members. Contextual facets have links to the
 * non-library member contributed facet that is a child of the object where the contextual facet is injected.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmContextualFacet extends OtmLibraryMemberBase<TLContextualFacet> implements OtmPropertyOwner {
    private static Log log = LogFactory.getLog( OtmContextualFacet.class );

    // The contributed facet that is child of a library member.
    // Must be lazy evaluated because the owner may not have been loaded or known when constructor runs.
    private OtmContributedFacet whereContributed = null;

    public OtmContextualFacet(TLContextualFacet tl, OtmModelManager manager) {
        super( tl, manager );
    }

    /**
     * {@inheritDoc}
     * <p>
     * This add is very different than {@link OtmBusinessObject#add(OtmContextualFacet)}
     * 
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmContextualFacet)
            child = ((OtmContextualFacet) child).getWhereContributed();
        if (child instanceof OtmProperty || child instanceof OtmContributedFacet) {
            // Make sure it has not already been added
            if (children == null)
                children = new ArrayList<>();
            else if (contains( children, child ))
                return null;

            if (inheritedChildren == null)
                inheritedChildren = new ArrayList<>();
            else if (contains( inheritedChildren, child ))
                return null;

            // If the contextual facet is not owned, set the owner now
            if (child instanceof OtmContributedFacet
                && ((OtmContributedFacet) child).getTL().getOwningEntity() == null) {
                OtmContextualFacet cf = ((OtmContributedFacet) child).getContributor();
                cf.setOwningEntity( this );
                log.debug( "Set owning entity on " + cf + " to " + this );
            }

            if (!child.isInherited())
                children.add( child );
            else
                inheritedChildren.add( child );
            return child;
        }
        return null;
    }

    /**
     * @see org.opentravel.model.OtmPropertyOwner#add(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    public OtmProperty add(TLModelElement tlChild) {
        OtmObject otm = OtmModelElement.get( tlChild );
        if (OtmAbstractFacet.addTL( tlChild, getTL() )) {
            if (otm instanceof OtmProperty)
                add( otm );
            else
                otm = OtmPropertyFactory.create( tlChild, this );
        }
        return (OtmProperty) otm;
    }

    @Override
    public void addAlias(TLAlias tla) {
        // NO-OP
    }

    /**
     * This may already have listener for contributed facet. If so, still add listener.
     * 
     * @see org.opentravel.model.OtmModelElement#addListener()
     */
    @Override
    protected void addListener() {
        for (ModelElementListener l : tlObject.getListeners())
            if (l instanceof OtmModelElementListener) {
                OtmObject o = ((OtmModelElementListener) l).get();
                if (o instanceof OtmContributedFacet && o.getTL() != tlObject)
                    return; // already has listener for something else
            }
        tlObject.addListener( new OtmModelElementListener( this ) );
    }



    @Override
    public StringProperty baseTypeProperty() {
        if (getBaseType() != null) {
            return new ReadOnlyStringWrapper( getBaseTypeNameWithPrefix() );
        }
        return super.baseTypeProperty();
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

    // /**
    // * {@inheritDoc} Clear this name property <b>only</b>. In most cases use
    // * {@link OtmContributedFacet#clearNameProperty()}
    // */
    // // @SuppressWarnings(value = {"squid:S1185"})
    // @Override
    // public void clearNameProperty() {
    // super.clearNameProperty();
    // }

    @Override
    public void delete(OtmObject property) {
        super.delete( property );
        if (property.getTL() instanceof TLAttribute)
            getTL().removeAttribute( ((TLAttribute) property.getTL()) );
        if (property.getTL() instanceof TLIndicator)
            getTL().removeIndicator( ((TLIndicator) property.getTL()) );
        if (property.getTL() instanceof TLProperty)
            getTL().removeProperty( ((TLProperty) property.getTL()) );
        remove( property );
    }

    /*
     * Use the model manager to find the owning entity by name.
     */
    protected OtmLibraryMember findWhereContributedByName() {
        OtmLibraryMember candidate = null;
        if (getTL().getOwningEntityName() != null && !getTL().getOwningEntityName().isEmpty()) {
            candidate = mgr.getMember( getTL().getOwningEntityName() );
            if (candidate != null && !(candidate.getTL() instanceof TLFacetOwner))
                candidate = null;
        }
        // log.debug( "findWhereContributedByName() found where " + this + " is contributed: " + candidate );
        return candidate;
    }

    /**
     * Get the owning entity of this contextual facet. This is where the facet is contributed.
     * 
     * @return library member where contributed or null
     * @see OtmContextualFacet#getContributedObject()
     * @see OtmContextualFacet#getOwningMember()
     */
    @Override
    public OtmObject getBaseType() {
        // Using the TL instead of contributed assures the facade is correct.
        if (getTL().getOwningEntity() != null)
            return OtmModelElement.get( (TLModelElement) getTL().getOwningEntity() );
        return null;
    }

    @Override
    public String getBaseTypeName() {
        return getBaseType() != null ? getBaseType().getName() : "";
    }

    public String getBaseTypeNameWithPrefix() {
        return getBaseType() != null ? getBaseType().getNameWithPrefix() : "";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Children are maintained on the contextual facet not the contributed. Children must be maintained even when a
     * contextual facet is not injected into an object.
     * 
     */
    @Override
    public List<OtmObject> getChildren() {
        synchronized (this) {
            if (children != null && children.isEmpty())
                modelChildren();
        }
        return children != null ? children : Collections.emptyList();
    }

    /**
     * @see OtmContextualFacet#getBaseType()
     * @see OtmContextualFacet#getOwningMember()
     * @return the object where this facet is contributed.
     */
    public OtmLibraryMember getContributedObject() {
        if (getBaseType() instanceof OtmLibraryMember)
            return (OtmLibraryMember) getBaseType();
        return null;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET_CONTEXTUAL;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
        // TODO
    }

    /**
     * {@inheritDoc} Get the full name complete with owning object's prefix
     */
    @Override
    public String getName() {
        return getTL().getLocalName();
    }

    /**
     * This is a library member so it returns itself. See {@link #getBaseType()} which returns object where contributed
     * 
     * @return this contextual facet
     * @see OtmContextualFacet#getBaseType()
     * @see OtmContextualFacet#getContributedObject()
     */
    @Override
    public OtmContextualFacet getOwningMember() {
        return this;
    }


    @Override
    public TLContextualFacet getTL() {
        return (TLContextualFacet) tlObject;
    }

    /**
     * @return Simply return whereContributed field. Do <b>not</b> attempt to find or create one.
     */
    public OtmContributedFacet getExistingContributed() {
        return whereContributed;
    }

    /**
     * Get the contributed facet that is a child of the owning object.
     * <p>
     * Will attempt to find missing owners using model manager search.
     * <p>
     * NOTE: detection of "ghost" inherited facets depends on Contributor will not have ghost set as where contributed.
     * 
     * @see #getExistingContributed()
     * @see #findWhereContributedByName()
     * @see #getWhereContributed(OtmChildrenOwner)
     * @return the non-ghost contributed facet where this facet is used
     */
    public OtmContributedFacet getWhereContributed() {
        if (whereContributed == null) {
            OtmContributedFacet contrib = null;

            // When the TL object has owning entity, use that to get the contributed facet
            OtmObject o = OtmModelElement.get( (TLModelElement) getTL().getOwningEntity() );
            if (o instanceof OtmChildrenOwner)
                contrib = getWhereContributed( (OtmChildrenOwner) o );

            // When the TL Owning Entity is not known, try a lookup by name
            if (contrib == null) {
                OtmLibraryMember owner = findWhereContributedByName();
                if (owner != null) {
                    contrib = new OtmContributedFacet( owner, this );
                    setWhereContributed( contrib );
                    owner.add( contrib );
                }
            }

            // Save the value
            if (contrib != null)
                setWhereContributed( contrib );

            log.debug( "getWhereContributed() for " + this + " evaluated to: " + whereContributed );
        }
        return whereContributed;
    }

    /**
     * Get the contributed facet from the owner's children. The TLFacets must be the same.
     * <p>
     * If the children owner has a contributed facet with the same TL as this contextual facet,
     * <p>
     * Do <b>not</b> set whereContributed and the contributed facets's contributor.
     * <p>
     * This is more reliable than NAME match used in {@link #findWhereContributed()}!
     * 
     * @param owner
     */
    public OtmContributedFacet getWhereContributed(OtmChildrenOwner owner) {
        for (OtmObject c : owner.getChildren()) {
            if (c instanceof OtmContributedFacet && c.getTL() == this.getTL()) {
                return (OtmContributedFacet) c;
            }
        }
        return null;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public void modelChildren() {
        getTL().getIndicators().forEach( p -> OtmPropertyFactory.create( p, this ) );
        getTL().getAttributes().forEach( p -> OtmPropertyFactory.create( p, this ) );
        getTL().getElements().forEach( p -> OtmPropertyFactory.create( p, this ) );

        for (TLFacet f : getTL().getAllFacets()) {
            // 2/12/21 - Added as needed to model nested facets
            OtmFacet<?> facet = OtmFacetFactory.create( f, this );
            if (facet != null)
                children.add( facet );
            // log.debug( "TEST - model facet child." );
        }
    }

    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringProperty nameEditingProperty() {
        return nameEditingProperty( getTL().getName() );
    }

    @Override
    public void remove(OtmObject child) {
        children.remove( child );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the owningEntity on this TLContextualFacet.
     * <p>
     * Create or update contributed facet and set this whereContributed property.
     * 
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#setBaseType(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        if (baseObj instanceof OtmContributedFacet)
            baseObj = ((OtmContributedFacet) baseObj).getContributor();
        if (baseObj instanceof OtmLibraryMember && baseObj.getTL() instanceof TLFacetOwner) {
            OtmLibraryMember lm = (OtmLibraryMember) baseObj;

            // 1/23/2021 - changed to add to owner instead of setting owning entity
            setOwningEntity( lm );
            //// DONE: Dave - Please review this change to make sure there are no unintended consequences
            //// Set the TL Owning entity
            //// getTL().setOwningEntity( (TLFacetOwner) lm.getTL() );
            //// setOwningEntity( getTL(), (TLFacetOwner) lm.getTL() );

            // Create or change where contributed
            if (getWhereContributed() == null)
                whereContributed = new OtmContributedFacet( lm, this );
            whereContributed.setParent( lm, this );
            whereContributed.clearNameProperty();
        }
        // Where used
        return getBaseType();
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        nameProperty = null;
        if (getWhereContributed() != null)
            getWhereContributed().clearNameProperty();
        return getTL().getName();
    }

    /**
     * When assigning the owner of a facet, the relationship must be established by adding the facet to the owner - not
     * simply by directly assigning the owning entity of the facet. By assigning it to the owner, not only will the
     * facet assignment be done, but the owner will be aware that the facet was added as one of its children.
     */
    protected void setOwningEntity(OtmLibraryMember owner) {
        if (owner instanceof OtmContextualFacet)
            ((OtmContextualFacet) owner).getTL().addChildFacet( getTL() );
    }

    /**
     * Simply set the whereContributed field and the contributor's contributor field.
     * <p>
     * This should not be used except by OtmContributedFacet constructor or lazy evaluation code.
     * 
     * @param contribitor
     */
    public void setWhereContributed(OtmContributedFacet contribitor) {
        this.whereContributed = contribitor;
        contribitor.setContributor( this );
    }

}
