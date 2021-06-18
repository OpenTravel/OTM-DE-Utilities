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
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmAbstractFacetPropertyOwner;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyBase;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
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
 * Abstract OTM Facade for all extension point facets. These are library members. Extension point facets have links to
 * the non-library member contributed facet that is a child of the object where the contextual facet is injected.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmExtensionPointFacet extends OtmLibraryMemberBase<TLExtensionPointFacet> implements OtmPropertyOwner {
    private static Log log = LogFactory.getLog( OtmExtensionPointFacet.class );

    /**
    * 
    */
    public OtmExtensionPointFacet(String name, OtmModelManager mgr) {
        super( new TLExtensionPointFacet(), mgr );
    }

    public OtmExtensionPointFacet(TLExtensionPointFacet tl, OtmModelManager manager) {
        super( tl, manager );
        // FIXME
        // this logic
        // ability to create and filter
        // JUNIT
        // ICON
        // Inheritance representation into base type
        // Member filter controller
    }

    /**
     * @see org.opentravel.model.OtmPropertyOwner#add(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    public OtmProperty add(TLModelElement tlChild) {
        OtmObject otm = OtmModelElement.get( tlChild );
        if (OtmAbstractFacetPropertyOwner.addTL( tlChild, getTL() )) {
            if (otm instanceof OtmProperty)
                add( otm );
            else
                otm = OtmPropertyFactory.create( tlChild, this );
        }
        return (OtmProperty) otm;
    }

    @Override
    public OtmObject add(OtmObject child) {
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
    public StringProperty baseTypeProperty() {
        if (getBaseType() != null) {
            return new ReadOnlyStringWrapper( getBaseTypeName() );
        }
        return super.baseTypeProperty();
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

    /**
     * Get the owning entity of this contextual facet. This is where the facet is contributed.
     * 
     * @return library member where contributed or null
     * @see OtmExtensionPointFacet#getContributedObject()
     * @see OtmExtensionPointFacet#getOwningMember()
     */
    @Override
    public OtmObject getBaseType() {
        OtmObject bt = null;
        if (getTL().getExtension() != null && getTL().getExtension().getExtendsEntity() != null)
            bt = OtmModelElement.get( (TLModelElement) getTL().getExtension().getExtendsEntity() );
        return bt;
    }

    @Override
    public String getBaseTypeName() {
        return getBaseType() != null ? getBaseType().getNameWithPrefix() : "";
    }

    // /**
    // * {@inheritDoc}
    // * <p>
    // * Children are maintained on the contextual facet not the contributed. Children must be maintained even when a
    // * contextual facet is not injected into an object.
    // *
    // */
    // @Override
    // public List<OtmObject> getChildren() {
    // synchronized (this) {
    // if (children != null && children.isEmpty())
    // modelChildren();
    // }
    // return children != null ? children : Collections.emptyList();
    // }

    // /**
    // * @see OtmExtensionPointFacet#getBaseType()
    // * @see OtmExtensionPointFacet#getOwningMember()
    // * @return the object where this facet is contributed.
    // */
    // public OtmLibraryMember getContributedObject() {
    // if (getBaseType() instanceof OtmLibraryMember)
    // return (OtmLibraryMember) getBaseType();
    // return null;
    // }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET_CONTEXTUAL;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public String getName() {
        return getTL().getLocalName();
    }

    /**
     * This is a library member so it returns itself. See {@link #getBaseType()} which returns object where contributed
     * 
     * @return this contextual facet
     * @see OtmExtensionPointFacet#getBaseType()
     * @see OtmExtensionPointFacet#getContributedObject()
     */
    @Override
    public OtmExtensionPointFacet getOwningMember() {
        return this;
    }

    @Override
    public TLExtensionPointFacet getTL() {
        return (TLExtensionPointFacet) tlObject;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public StringProperty nameEditingProperty() {
        return nameEditingProperty( getTL().getLocalName() );
    }

    @Override
    public void modelChildren() {
        if (getTL() instanceof TLIndicatorOwner)
            (getTL()).getIndicators().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLAttributeOwner)
            (getTL()).getAttributes().forEach( p -> OtmPropertyFactory.create( p, this ) );
        if (getTL() instanceof TLPropertyOwner)
            (getTL()).getElements().forEach( p -> OtmPropertyFactory.create( p, this ) );
    }

    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

    // @Override
    // public void remove(OtmObject child) {
    // children.remove( child );
    // }

    /**
     * {@inheritDoc} Sets base type into the TL extension object
     * 
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#setBaseType(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        TLExtension tle = getTL().getExtension();
        if (tle == null) {
            tle = new TLExtension();
            getTL().setExtension( tle );
        }
        if (baseObj.getTL() instanceof NamedEntity)
            tle.setExtendsEntity( (NamedEntity) baseObj.getTL() );
        return getBaseType();
    }

    // @Override
    // public String setName(String name) {
    //// getTL().setLocalName( name );
    // nameProperty = null;
    // // if (getWhereContributed() != null)
    // // getWhereContributed().clearNameProperty();
    // return getTL().getLocalName();
    // }

}
