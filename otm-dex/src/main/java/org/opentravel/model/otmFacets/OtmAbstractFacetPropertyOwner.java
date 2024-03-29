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
 * Abstract OTM facade for abstract TL Facets that can own properties. Excludes list facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractFacetPropertyOwner<T extends TLAbstractFacet> extends OtmAbstractFacet<TLAbstractFacet>
    implements OtmPropertyOwner {
    private static Logger log = LogManager.getLogger( OtmAbstractFacetPropertyOwner.class );

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

    public OtmAbstractFacetPropertyOwner(T tl) {
        super( tl );
        setExpanded( true ); // Start out expanded
        setCollapsed( false ); // Start out expanded
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

    protected boolean contains(List<OtmObject> list, OtmObject child) {
        if (list.contains( child ))
            return true;
        for (OtmObject c : list)
            if (c.getTL() == child.getTL())
                return true;

        return false;
    }

    /**
     * Add to inheritedChildren list <i>if</i> it is a property, was already modeled, and its parent is not the passed
     * property owner
     */
    protected void createInherited(TLModelElement tlProp) {
        OtmObject otm = OtmModelElement.get( tlProp );
        if (otm instanceof OtmProperty && ((OtmProperty) otm).getParent() != this) {
            OtmProperty p = OtmPropertyFactory.create( tlProp, null );
            p.setParent( this );
            add( p );
        }
    }

    @Override
    public void delete(OtmObject property) {
        if (property == null)
            return;
        if (getTL() instanceof TLAttributeOwner && property.getTL() instanceof TLAttribute)
            ((TLAttributeOwner) getTL()).removeAttribute( ((TLAttribute) property.getTL()) );
        else if (getTL() instanceof TLIndicatorOwner && property.getTL() instanceof TLIndicator)
            ((TLIndicatorOwner) getTL()).removeIndicator( ((TLIndicator) property.getTL()) );
        else if (getTL() instanceof TLPropertyOwner && property.getTL() instanceof TLProperty)
            ((TLPropertyOwner) getTL()).removeProperty( ((TLProperty) property.getTL()) );
        else
            log.warn( "Invalid delete TL property owner and TL property pair." );
        remove( property ); // if children is empty, the deleted TL will not be modeled and this remove will do nothing
        refresh();
        // log.debug( "Deleted " + property + " from" + this + " with " + getChildren().size() + " kids." );
    }

    /**
     * Delete all children. For each child, invoke {@link OtmAbstractFacetPropertyOwner#delete(OtmObject)}
     */
    public void deleteAll() {
        List<OtmObject> kids = new ArrayList<>( getChildren() );
        kids.forEach( this::delete );
    }

    public abstract DexActionManager getActionManger();

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
    public List<OtmObject> getChildren() {
        synchronized (this) {
            if (children != null && children.isEmpty())
                modelChildren();
        }
        return children != null ? children : Collections.emptyList();
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
     * {@inheritDoc} - NOT IMPLEMENTED for facets
     */
    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc} - NOT IMPLEMENTED for facets
     */
    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc} - NOT IMPLEMENTED for facets
     */
    @Override
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc} - NOT IMPLEMENTED for facets
     */
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
        // 2/22/21 dmh - added inherited children that are type users
        if (getInheritedChildren() != null)
            getInheritedChildren().forEach( c -> {
                if (c instanceof OtmTypeUser)
                    users.add( (OtmTypeUser) c );
            } );
        return users;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren;
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
    public boolean isInherited() {
        // log.debug("Is " + this + " inherited? " + !getParent().contains(this));
        if (getParent() instanceof OtmLibraryMember)
            return !((OtmLibraryMember) getParent()).contains( this );
        return false;
    }

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

        // get the base type that provides inherited children
        if (getOwningMember().getBaseType() == null)
            return;

        // This should be overrides on the sub-types.
        // if (this instanceof OtmContributedFacet || this instanceof OtmListFacet)
        if (this instanceof OtmContributedFacet)
            return;
        // Why not get inherited from each?
        List<OtmFacet<TLFacet>> ancestors = getAncestors();

        // Get list of all kids from all ancestors
        List<TLModelElement> tlKids = new ArrayList<>();
        for (OtmFacet<TLFacet> a : ancestors)
            tlKids.addAll( a.getTLChildren() );

        // Create a new property to represent the tl in this facet
        for (TLModelElement k : tlKids) {
            OtmProperty p = OtmPropertyFactory.create( k, null );
            // If this is in a minor library AND a property with the same name already exists in some version of this
            // facet then it is not inherited, just ignore it.
            boolean skip = false;
            if (this.getLibrary() != null && this.getLibrary().isMinorVersion())
                for (OtmObject c : children) {
                    if (c.getName() == null || c.getName().equals( p.getName() )) {
                        skip = true;
                        break;
                    }
                }
            if (!skip) {
                inheritedChildren.add( p );
                p.setParent( this );
                add( p );
            }
            // assert (p.isInherited());
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

    @Override
    public void remove(OtmObject property) {
        if (property != null)
            if (getChildren() != null && children.contains( property ))
                children.remove( property );
            else if (getInheritedChildren() != null && inheritedChildren.contains( property ))
                inheritedChildren.remove( property );
    }

}
