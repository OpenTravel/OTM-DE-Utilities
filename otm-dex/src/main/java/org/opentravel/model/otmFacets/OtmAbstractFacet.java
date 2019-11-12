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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract OTM facade for abstract TL Facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractFacet<T extends TLAbstractFacet> extends OtmModelElement<TLAbstractFacet>
    implements OtmPropertyOwner, OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmAbstractFacet.class );

    // private OtmLibraryMember parent;

    public OtmAbstractFacet(T tl) {
        super( tl );
        setExpanded( true ); // Start out expanded
    }

    @Override
    public OtmProperty add(OtmObject child) {
        if (child instanceof OtmProperty) {
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
        }
        return (OtmProperty) child;
    }

    @Override
    public OtmProperty add(TLModelElement tl) {
        OtmObject otm = OtmModelElement.get( tl );
        if (addTL( tl, getTL() )) {
            // Add the facade to this
            if (otm instanceof OtmProperty)
                add( otm );
            else
                otm = OtmPropertyFactory.create( tl, this );
        }
        return (OtmProperty) otm;
    }

    /**
     * Static add of TL property to TL owner.
     * <p>
     * Use the type of the property and owner to determine how to add the property to the owner.
     * 
     * @param tlProperty
     * @param tlOwner
     * @return true if successful, false otherwise
     */
    public static boolean addTL(TLModelElement tlProperty, TLModelElement tlOwner) {
        // Add the TL object to the TL owner
        if (tlOwner instanceof TLAttributeOwner && tlProperty instanceof TLAttribute)
            ((TLAttributeOwner) tlOwner).addAttribute( (TLAttribute) tlProperty );

        else if (tlOwner instanceof TLIndicatorOwner && tlProperty instanceof TLIndicator)
            ((TLIndicatorOwner) tlOwner).addIndicator( (TLIndicator) tlProperty );

        else if (tlOwner instanceof TLPropertyOwner && tlProperty instanceof TLProperty)
            ((TLPropertyOwner) tlOwner).addElement( (TLProperty) tlProperty );
        else
            return false;
        return true;
    }

    private boolean contains(List<OtmObject> list, OtmObject child) {
        if (list.contains( child ))
            return true;
        for (OtmObject c : list)
            if (c.getTL() == child.getTL())
                return true;

        return false;
    }

    @Override
    public void delete(OtmObject property) {
        if (getTL() instanceof TLAttributeOwner && property.getTL() instanceof TLAttribute)
            ((TLAttributeOwner) getTL()).removeAttribute( ((TLAttribute) property.getTL()) );
        if (getTL() instanceof TLIndicatorOwner && property.getTL() instanceof TLIndicator)
            ((TLIndicatorOwner) getTL()).removeIndicator( ((TLIndicator) property.getTL()) );
        if (getTL() instanceof TLPropertyOwner && property.getTL() instanceof TLProperty)
            ((TLPropertyOwner) getTL()).removeProperty( ((TLProperty) property.getTL()) );
        remove( property );
    }

    /**
     * Delete all children. For each child, invoke {@link OtmAbstractFacet#delete(OtmObject)}
     */
    public void deleteAll() {
        List<OtmObject> kids = new ArrayList<>( getChildren() );
        kids.forEach( this::delete );
    }

    public abstract DexActionManager getActionManger();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OtmObject> getChildren() {
        synchronized (this) {
            if (children != null && children.isEmpty())
                modelChildren();
        }
        return children;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        // Will only run for "leaf" detail facets
        Collection<OtmObject> hierarchy = new ArrayList<>();
        getInheritedChildren().forEach( hierarchy::add );
        getChildren().forEach( hierarchy::add );
        return hierarchy;
    }

    /**
     * Get children attributes, indicators and elements(properties). Does not return facets.
     * 
     * @return new list of tl model elements
     */
    public List<TLModelElement> getTLChildren() {
        List<TLModelElement> tlProperties = new ArrayList<>();
        if (getTL() instanceof TLAttributeOwner)
            tlProperties.addAll( ((TLAttributeOwner) getTL()).getAttributes() );
        if (getTL() instanceof TLPropertyOwner)
            tlProperties.addAll( ((TLPropertyOwner) getTL()).getElements() );
        if (getTL() instanceof TLIndicatorOwner)
            tlProperties.addAll( ((TLIndicatorOwner) getTL()).getIndicators() );
        return tlProperties;
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        Collection<OtmTypeUser> users = new ArrayList<>();
        if (getChildren() != null)
            getChildren().forEach( c -> {
                if (c instanceof OtmTypeUser)
                    users.add( (OtmTypeUser) c );
            } );
        return users;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren;
    }

    @Override
    public String getName() {
        return tlObject.getLocalName();
    }

    @Override
    public String getNamespace() {
        return tlObject.getNamespace();
    }

    public abstract OtmObject getParent();


    public String getRole() {
        return tlObject.getFacetType().getIdentityName();
    }

    // /**
    // * Facet edit-ability is the ability to add/remove properties.
    // *
    // * @see org.opentravel.model.OtmModelElement#isEditable()
    // */
    // @Override
    // public boolean isEditable() {
    // return getOwningMember().isEditable();
    // }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

    /**
     * {@inheritDoc}
     * <p>
     * When force is true, run validation on all children.
     */
    @Override
    public boolean isValid(boolean force) {
        if (force) {
            getChildren().forEach( c -> {
                if (c != this)
                    c.isValid( force );
            } );
        }
        return super.isValid( force );
    }

    @Override
    public boolean isInherited() {
        // log.debug("Is " + this + " inherited? " + !getParent().contains(this));
        if (getParent() instanceof OtmLibraryMember)
            return !((OtmLibraryMember) getParent()).contains( this );
        return false;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Find all properties (elements, indicators, attributes) and create OtmProperties for all their children and add to
     * Children list.
     */
    @Override
    public void modelChildren() {
        if (getTL() instanceof TLIndicatorOwner)
            ((TLIndicatorOwner) getTL()).getIndicators().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLAttributeOwner)
            ((TLAttributeOwner) getTL()).getAttributes().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLPropertyOwner)
            ((TLPropertyOwner) getTL()).getElements().forEach( p -> OtmPropertyFactory.create( p, this ) );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Find all inheritance ancestors and create OtmProperties for all their children and add to inheritedChildren list.
     */
    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // RE-model

        if (getOwningMember().getBaseType() == null)
            return;
        // This should be overrides on the sub-types.
        if (this instanceof OtmContributedFacet || this instanceof OtmListFacet)
            return;

        List<OtmFacet<TLFacet>> ancestors = getAncestors();

        List<TLModelElement> tlKids = new ArrayList<>();
        for (OtmFacet<TLFacet> a : ancestors)
            tlKids.addAll( a.getTLChildren() );

        for (TLModelElement k : tlKids) {
            OtmProperty p = OtmPropertyFactory.create( k, null );
            p.setParent( this );
            // assert (p.isInherited());
            add( p );
            // assert inheritedChildren.contains( p );
        }

        // The preferred approach would be to use the codeGenUtils, but they fail. See TestInheritance for test case.
        // All properties, local and inherited
        // List<TLProperty> inheritedElements = PropertyCodegenUtils.getInheritedProperties((TLFacet) getTL());
        // Get only the directly inherited properties
        // if (getOwningMember().getBaseType() != null && getTL() instanceof TLFacet) {
        // PropertyCodegenUtils.getInheritedFacetProperties( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // PropertyCodegenUtils.getInheritedFacetAttributes( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // PropertyCodegenUtils.getInheritedFacetIndicators( (TLFacet) getTL() )
        // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        // }
    }

    private List<OtmFacet<TLFacet>> getAncestors() {
        if (!(this instanceof OtmFacet))
            return Collections.emptyList();

        List<OtmFacet<TLFacet>> ancestors = new ArrayList<>();
        OtmObject obj = getOwningMember().getBaseType();
        while (obj != null) {
            if (obj instanceof OtmLibraryMember) {
                OtmFacet<TLFacet> a = ((OtmLibraryMember) obj).getFacet( (OtmFacet<TLFacet>) this );
                if (a == null || ancestors.contains( a )) {
                    // No ancestor or Loop detected - exit
                    obj = null;
                } else {
                    ancestors.add( a );
                    obj = ((OtmLibraryMember) obj).getBaseType();
                }
            } else
                obj = null;
        }
        return ancestors;
    }

    @Override
    public boolean isRenameable() {
        return false;
    }

    @Override
    public void remove(OtmObject property) {
        if (children.contains( property ))
            children.remove( property );
        if (inheritedChildren.contains( property ))
            inheritedChildren.remove( property );
    }

    @Override
    public String toString() {
        return getName();
    }

}
