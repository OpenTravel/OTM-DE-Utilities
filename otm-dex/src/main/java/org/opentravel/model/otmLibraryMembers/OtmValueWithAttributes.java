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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.OtmTypeUserUtils;
import org.opentravel.model.otmFacets.OtmAbstractFacet;
import org.opentravel.model.otmFacets.OtmVWAAttributeFacet;
import org.opentravel.model.otmFacets.OtmVWAValueFacet;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.OtmIndicator;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OTM Object for Value-With-Attributes (VWA).
 * <p>
 * The TL model uses the parent for two purposes:
 * <ol>
 * <li>Value type (assigned type) - the type assigned to the "Value" of the VWA
 * <li>Inheritance parent (base type) - when the parent is another VWA, the attributes on the parent VWA are inherited
 * and the Value is the value of the parent.
 * </ol>
 * 
 * @author Dave Hollander
 * 
 */
public class OtmValueWithAttributes extends OtmLibraryMemberBase<TLValueWithAttributes>
    implements OtmTypeProvider, OtmChildrenOwner, OtmTypeUser, OtmPropertyOwner {
    private static Logger log = LogManager.getLogger( OtmValueWithAttributes.class );
    private StringProperty exampleProperty;

    public OtmValueWithAttributes(String name, OtmModelManager mgr) {
        super( new TLValueWithAttributes(), mgr );
        setName( name );
    }

    public OtmValueWithAttributes(TLValueWithAttributes tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public void delete(OtmObject property) {
        if (property instanceof OtmProperty)
            delete( (OtmProperty) property );
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
            return (OtmProperty) child;
        }
        return null;
    }

    @Override
    public OtmProperty add(TLModelElement tl) {
        if (tl instanceof TLIndicator)
            getTL().addIndicator( (TLIndicator) tl );
        else if (tl instanceof TLAttribute)
            getTL().addAttribute( (TLAttribute) tl );
        else
            log.debug( "unknown/not-implemented property type." );

        return OtmPropertyFactory.create( tl, this );
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for attributes and indicators
     */
    @Override
    public boolean canAdd(OtmProperty property) {
        return property instanceof OtmAttribute<?> || property instanceof OtmIndicator<?>;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Use the TlParentType if it is not a VWA.
     */
    @Override
    public StringProperty assignedTypeProperty() {
        // If it has a type, get its formatted name
        String typeName = "";

        if (getBaseType() == null && getAssignedType() == null)
            return new ReadOnlyStringWrapper( "" );

        // If parent is used for inheritance return its assigned type
        if (getBaseType() != null)
            typeName = getBaseType().assignedTypeProperty().get();
        else
            typeName = getAssignedType().getName();

        if (getLibrary() != null && getAssignedType() != null && getAssignedType().getLibrary() != null)
            typeName = OtmTypeUserUtils.assignedTypeWithPrefix( typeName, getLibrary().getTL(),
                getAssignedType().getLibrary().getTL() );

        if (isEditable())
            return new SimpleStringProperty( typeName );

        return new ReadOnlyStringWrapper( typeName );
    }

    /**
     * {@inheritDoc}
     * <p>
     * The base type is a parent that is a VWA. If the parent is not a VWA, the base type is empty/null.
     */
    @Override
    public StringProperty baseTypeProperty() {
        OtmValueWithAttributes base = getBaseType();
        if (base instanceof OtmValueWithAttributes)
            return new SimpleStringProperty( base.getNameWithPrefix() );
        else
            return new ReadOnlyStringWrapper( "" );
    }

    public StringProperty exampleProperty() {
        if (exampleProperty == null && getActionManager() != null) {
            exampleProperty = getActionManager().add( DexActions.EXAMPLECHANGE, getExample(), this );
        }
        return exampleProperty;
    }

    public void delete(OtmProperty property) {
        if (property.getTL() instanceof TLIndicator)
            getTL().removeIndicator( (TLIndicator) property.getTL() );
        else if (property.getTL() instanceof TLAttribute)
            getTL().removeAttribute( (TLAttribute) property.getTL() );
        remove( property );
    }

    /**
     * @return TL-parentType if it is not a Value with Attributes.
     */
    @Override
    public TLPropertyType getAssignedTLType() {
        if (getTL().getParentType() instanceof TLValueWithAttributes) {
            if (getBaseType() != null)
                return getBaseType().getAssignedTLType();
            else
                return null;
        } else {
            return getTL().getParentType();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Use the assigned TL type which will assure it is not a VWA
     */
    @Override
    public OtmTypeProvider getAssignedType() {
        OtmObject at = OtmModelElement.get( (TLModelElement) getAssignedTLType() );
        return at instanceof OtmTypeProvider ? (OtmTypeProvider) at : null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return TL-parentType if it is a value with attributes.
     * 
     */
    @Override
    public OtmValueWithAttributes getBaseType() {
        OtmObject p = OtmModelElement.get( (TLModelElement) getTL().getParentType() );
        if (p instanceof OtmValueWithAttributes)
            return (OtmValueWithAttributes) p;
        return null;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> hierarchy = new HashSet<>();
        // Value facet
        hierarchy.add( new OtmVWAValueFacet( (OtmPropertyOwner) this ) );
        // Attribute Facet
        hierarchy.add( new OtmVWAAttributeFacet( (OtmPropertyOwner) this ) );

        return hierarchy;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.VWA;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren;
    }

    @Override
    public OtmValueWithAttributes getOwningMember() {
        return this;
    }

    @Override
    public TLValueWithAttributes getTL() {
        return (TLValueWithAttributes) tlObject;
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getParentTypeName();
    }

    @Override
    public boolean isInherited() {
        return false;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates properties for attributes and indicators in the TL object.
     */
    @Override
    public void modelChildren() {
        getTL().getAttributes().forEach( tla -> OtmPropertyFactory.create( tla, this ) );
        getTL().getIndicators().forEach( tli -> OtmPropertyFactory.create( tli, this ) );
    }

    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // RE-model

        if (getBaseType() != null) {
            PropertyCodegenUtils.getInheritedAttributes( getTL() ).forEach( this::createInherited );
            PropertyCodegenUtils.getInheritedIndicators( getTL() ).forEach( this::createInherited );

            // // DEBUGGING
            // log.debug( "Modeled " + inheritedChildren.size() + " inherited children of " + this );
            // for (OtmObject child : inheritedChildren)
            // if (!child.isInherited())
            // log.error( "Inherited child doen't know it is inherited!." );
        }
    }

    /**
     * Add to inheritedChildren list <i>if</i> it is a property, was already modeled, and its parent is not the passed
     * property owner
     * <p>
     * Cloned from {@link OtmAbstractFacet#createInherited()}
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
    public void remove(OtmObject property) {
        if (property instanceof OtmProperty)
            remove( (OtmProperty) property );
    }

    public void remove(OtmProperty property) {
        if (getChildren().contains( property ))
            getChildren().remove( property );
        if (getInheritedChildren().contains( property ))
            getInheritedChildren().remove( property );
    }

    @Override
    public TLPropertyType setAssignedTLType(NamedEntity type) {
        if (type == null)
            getTL().setParentType( null );
        else if (type instanceof TLAttributeType)
            getTL().setParentType( (TLAttributeType) type );
        // log.debug( "Set assigned TL type" );
        return getAssignedTLType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        // Prevent circular assignment.
        if (type == this)
            return null;

        // Take this's owning member out of the current assigned type's where used list
        if (getAssignedType() != null && getAssignedType().getOwningMember() != null)
            getAssignedType().getOwningMember().changeWhereUsed( getOwningMember(), null );

        if (type == null)
            setAssignedTLType( null );
        else if (type.getTL() instanceof TLAttributeType) {
            setAssignedTLType( (TLAttributeType) type.getTL() );
            type.getOwningMember().changeWhereUsed( null, getOwningMember() );
        }
        // log.debug( "Set assigned type" );
        return getAssignedType();
    }

    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        if (baseObj instanceof OtmValueWithAttributes)
            setAssignedType( (OtmValueWithAttributes) baseObj );
        return getBaseType();
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        clearNameProperty();
        isValid( true );
        return getName();
    }

    @Override
    public void setTLTypeName(String name) {
        getTL().setParentType( null );
        getTL().setParentTypeName( name );
    }


}
