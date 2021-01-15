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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmAbstractFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyBase;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

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
    private OtmContributedFacet whereContributed = null;

    public OtmContextualFacet(TLContextualFacet tl, OtmModelManager manager) {
        super( tl, manager );
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

            if (!child.isInherited())
                children.add( child );
            else
                inheritedChildren.add( child );
            return child;
        }
        return null;
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



    @Override
    public void addAlias(TLAlias tla) {
        // NO-OP
    }
    // public OtmContextualFacet addCF(OtmContextualFacet child) {
    // if (children == null)
    // children = new ArrayList<>();
    // else if (contains( children, child ))
    // return null;
    //
    // if (inheritedChildren == null)
    // inheritedChildren = new ArrayList<>();
    // else if (contains( inheritedChildren, child ))
    // return null;
    //
    // if (!child.isInherited())
    // children.add( child );
    // else
    // inheritedChildren.add( child );
    // return child;
    // }


    @Override
    public StringProperty baseTypeProperty() {
        if (getBaseType() != null) {
            return new ReadOnlyStringWrapper( getBaseTypeNameWithPrefix() );
        }
        return super.baseTypeProperty();
    }

    /**
     * {@inheritDoc} Clear this name property <b>only</b>. In most cases use
     * {@link OtmContributedFacet#clearNameProperty()}
     */
    // @SuppressWarnings(value = {"squid:S1185"})
    @Override
    public void clearNameProperty() {
        super.clearNameProperty();
    }

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

    /**
     * Get the owning entity of this contextual facet. This is where the facet is contributed.
     * 
     * @return library member where contributed or null
     * @see OtmContextualFacet#getContributedObject()
     * @see OtmContextualFacet#getOwningMember()
     */
    @Override
    public OtmObject getBaseType() {
        if (getTL().getOwningEntity() != null)
            return OtmModelElement.get( (TLModelElement) getTL().getOwningEntity() );
        // Using the TL instead of contributed assures the facade is correct.
        // Must have same result as:
        // if (getWhereContributed() != null)
        // return getWhereContributed().getOwningMember();
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

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

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
        // String ln = getTL().getLocalName(); // ObjectName_FacetName
        // String sn = getTL().getName(); // FacetName
        return getTL().getLocalName();
        // return this.getClass().getSimpleName();
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
     * NOTE: detection of "ghost" inherited facets depends on Contributor will not have ghost set as where contributed.
     * 
     * @return the non-ghost contributed facet where this facet is used
     */
    public OtmContributedFacet getWhereContributed() {
        if (whereContributed == null) {
            // If the TL object has owning entity, use that to find the contributed facet
            OtmObject o = OtmModelElement.get( (TLModelElement) getTL().getOwningEntity() );
            if (o instanceof OtmContributedFacet)
                o = ((OtmContributedFacet) o).getContributor();

            // Try to find a contributed facet child with same TL
            if (o instanceof OtmChildrenOwner)
                findWhereContributed( (OtmChildrenOwner) o );

            // if still not found
            if (whereContributed == null) {
                if (o instanceof OtmLibraryMember) {
                    // Build a contributed facet and add to parent
                    whereContributed = new OtmContributedFacet( (OtmLibraryMember) o, this );
                    ((OtmLibraryMember) o).add( whereContributed );
                } else {
                    // Fail-safe - search the manager for a match
                    findWhereContributed();
                }
            }
        }

        return whereContributed;
    }

    /**
     * See if the children owner has a contributed facet with the same TL as this contextual facet
     * 
     * @param owner
     */
    private void findWhereContributed(OtmChildrenOwner owner) {
        for (OtmObject c : owner.getChildren()) {
            // NAME match is not reliable!
            // if (c instanceof OtmContributedFacet && c.getName().equals( this.getName() )) {
            if (c instanceof OtmContributedFacet && c.getTL() == this.getTL()) {
                whereContributed = (OtmContributedFacet) c;
                ((OtmContributedFacet) c).setContributor( this );
            }
        }
    }

    /**
     * Simply set the whereContributed field. This should not be used except by OtmContributedFacet constructor.
     * 
     * @param contribitor
     */
    public void setWhereContributed(OtmContributedFacet contribitor) {
        this.whereContributed = contribitor;
    }

    /*
     * See if the model manager has the owning entity by name. If so, create and set contributed facet.
     */
    private void findWhereContributed() {
        if (getTL().getOwningEntityName() == null)
            return;
        if (!getTL().getOwningEntityName().isEmpty()) {
            OtmLibraryMember candidate = mgr.getMember( getTL().getOwningEntityName() );
            if (candidate != null && candidate.getTL() instanceof TLFacetOwner) {
                log.debug( "Name Match Found for contextual facet with no owner: " + candidate );
                getTL().setOwningEntity( (TLFacetOwner) candidate.getTL() );
                whereContributed = new OtmContributedFacet( candidate, this );
                candidate.add( whereContributed );
            } else {
                log.warn( "Can't find where contributed. Member = " + this + "  owning entity = "
                    + getTL().getOwningEntityName() );
            }
        }
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringProperty nameEditingProperty() {
        return nameEditingProperty( getTL().getName() );
    }

    @Override
    public void modelChildren() {
        if (getTL() instanceof TLIndicatorOwner)
            ((TLIndicatorOwner) getTL()).getIndicators().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLAttributeOwner)
            ((TLAttributeOwner) getTL()).getAttributes().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLPropertyOwner)
            ((TLPropertyOwner) getTL()).getElements().forEach( p -> OtmPropertyFactory.create( p, this ) );
        // TODO - add other facets???
    }

    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
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
            // Set the TL Owning entity
            getTL().setOwningEntity( (TLFacetOwner) lm.getTL() );

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

}
