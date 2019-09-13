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
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
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
    }

    // /**
    // * Create a facet for OtmOperations which are not library members
    // *
    // * @param tl
    // * @param actionMgr
    // */
    // // The only time this is used is for operations - operationFacet/operations which are not library members
    // public OtmAbstractFacet(T tl) {
    // super( tl );
    // }

    @Override
    public OtmProperty<?> add(TLModelElement tl) {
        if (getTL() instanceof TLAttributeOwner && tl instanceof TLAttribute)
            ((TLAttributeOwner) getTL()).addAttribute( (TLAttribute) tl );

        if (getTL() instanceof TLIndicatorOwner && tl instanceof TLIndicator)
            ((TLIndicatorOwner) getTL()).addIndicator( (TLIndicator) tl );

        if (getTL() instanceof TLPropertyOwner && tl instanceof TLProperty)
            ((TLPropertyOwner) getTL()).addElement( (TLProperty) tl );

        return OtmPropertyFactory.create( tl, this );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates properties to represent facet children.
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

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        // Will only run for "leaf" detail facets
        Collection<OtmObject> hierarchy = new ArrayList<>();
        getInheritedChildren().forEach( hierarchy::add );
        getChildren().forEach( hierarchy::add );
        return hierarchy;
    }

    @Override
    public void modelInheritedChildren() {
        // Only model once
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // RE-model
        // return;

        // All properties, local and inherited
        // List<TLProperty> inheritedElements = PropertyCodegenUtils.getInheritedProperties(getTL());

        // Get only the directly inherited properties
        if (getOwningMember().getBaseType() != null && getTL() instanceof TLFacet) {
            PropertyCodegenUtils.getInheritedFacetProperties( (TLFacet) getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
            PropertyCodegenUtils.getInheritedFacetAttributes( (TLFacet) getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
            PropertyCodegenUtils.getInheritedFacetIndicators( (TLFacet) getTL() )
                .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
        }
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren;
    }

    public abstract DexActionManager getActionManger();

    public abstract OtmObject getParent();

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

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
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
    public String getNamespace() {
        return tlObject.getNamespace();
    }

    @Override
    public String getName() {
        return tlObject.getLocalName();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public String getRole() {
        return tlObject.getFacetType().getIdentityName();
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
        return children;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    // @Override
    // public abstract OtmLibraryMember getOwningMember();

    // @Override
    // public OtmProperty<?> add(TLModelElement tl) {
    // if (tl instanceof TLIndicator)
    // getTL().addIndicator( (TLIndicator) tl );
    // else if (tl instanceof TLProperty)
    // getTL().addElement( (TLProperty) tl );
    // else if (tl instanceof TLAttribute)
    // getTL().addAttribute( (TLAttribute) tl );
    // else
    // log.debug( "unknown/not-implemented property type." );
    //
    // return OtmPropertyFactory.create( tl, this );
    // }

    // /**
    // * {@inheritDoc}
    // * <p>
    // * Creates properties to represent facet children.
    // */
    // @Override
    // public void modelChildren() {
    // tlObject.getIndicators().forEach( i -> OtmPropertyFactory.create( i, this ) );
    // tlObject.getAttributes().forEach( i -> OtmPropertyFactory.create( i, this ) );
    // tlObject.getElements().forEach( i -> OtmPropertyFactory.create( i, this ) );
    // // for (TLAttribute c : tlObject.getAttributes())
    // // add( OtmPropertyFactory.create( c, this ) );
    // // for (TLProperty c : tlObject.getElements())
    // // add( OtmPropertyFactory.create( c, this ) );
    // // for (TLAlias c : tlObject.getAliases())
    // // getOwningMember().addAlias( c );
    // }

    // @Override
    // public void modelInheritedChildren() {
    // // Only model once
    // if (inheritedChildren == null)
    // inheritedChildren = new ArrayList<>();
    // else
    // inheritedChildren.clear(); // RE-model
    // // return;
    //
    // // All properties, local and inherited
    // // List<TLProperty> inheritedElements = PropertyCodegenUtils.getInheritedProperties(getTL());
    //
    // // Get only the directly inherited properties
    // if (getOwningMember().getBaseType() != null) {
    // PropertyCodegenUtils.getInheritedFacetProperties( getTL() )
    // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
    // PropertyCodegenUtils.getInheritedFacetAttributes( getTL() )
    // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
    // PropertyCodegenUtils.getInheritedFacetIndicators( getTL() )
    // .forEach( ie -> OtmPropertyFactory.create( ie, this ) );
    //
    // // log.debug("Found " + inheritedElements.size() + " inherited element children of " + this.getName() + " of
    // // "
    // // + this.getOwningMember().getName());
    // // log.debug("Found " + inheritedAttrs.size() + " inherited attribute children of " + this.getName() + " of
    // // "
    // // + this.getOwningMember().getName());
    // // log.debug("Found " + inheritedIndicators.size() + " inherited indicator children of " + this.getName()
    // // + " of " + this.getOwningMember().getName());
    // }
    // }

    @Override
    public OtmProperty<?> add(OtmObject child) {
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
        return (OtmProperty<?>) child;
    }

    private boolean contains(List<OtmObject> list, OtmObject child) {
        if (list.contains( child ))
            return true;
        for (OtmObject c : list)
            if (c.getTL() == child.getTL())
                return true;

        return false;
    }
}
