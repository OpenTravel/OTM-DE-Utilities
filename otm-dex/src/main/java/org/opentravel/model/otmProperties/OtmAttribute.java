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

package org.opentravel.model.otmProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.OtmTypeUserUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyType;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Abstract OTM Node for attribute properties.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmAttribute<T extends TLAttribute> extends OtmPropertyBase<TLAttribute> implements OtmTypeUser {
    private static Logger log = LogManager.getLogger( OtmAttribute.class );

    private StringProperty assignedTypeProperty;

    /**
     * Create a new attribute and set the parent. Adds tl to parent's tl.
     * 
     * @param property owner of this attribute
     */
    public OtmAttribute(T tl, OtmPropertyOwner parent) {
        super( tl, parent );
    }

    @Override
    public StringProperty assignedTypeProperty() {
        if (assignedTypeProperty == null)
            if (isEditable())
                assignedTypeProperty = new SimpleStringProperty();
            else
                assignedTypeProperty = new ReadOnlyStringWrapper();
        assignedTypeProperty.set( OtmTypeUserUtils.formatAssignedType( this ) );
        return assignedTypeProperty;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Apply initial lower case rule.
     */
    @Override
    public String fixName(String name) {
        return name != null && !name.isEmpty() ? name.substring( 0, 1 ).toLowerCase() + name.substring( 1 ) : "";
    }

    @Override
    public TLPropertyType getAssignedTLType() {
        return getTL().getType();
    }

    @Override
    public OtmTypeProvider getAssignedType() {
        return OtmTypeUserUtils.getAssignedType( this );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ATTRIBUTE;
    }

    @Override
    public String getName() {
        return getTL().getName();
    }

    @Override
    public TLAttribute getTL() {
        return (TLAttribute) tlObject;
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getTypeName();
    }

    @Override
    public boolean isInherited() {
        return getParent() != null && getTL().getOwner() != getParent().getTL();
    }

    @Override
    public boolean isEditable() {
        if (!isInherited())
            return super.isEditable();
        // If inherited, find out from the actual owner
        OtmObject owner = OtmModelElement.get( (TLModelElement) getTL().getOwner() );
        return owner != null && owner.isEditable();
    }

    @Override
    public boolean isManditory() {
        return getTL().isMandatory();
    }

    /**
     * Useful for types that are not in the model manager.
     */
    @Override
    public TLPropertyType setAssignedTLType(NamedEntity type) {
        if (type == null)
            getTL().setType( null );
        else if (type instanceof TLPropertyType)
            getTL().setType( (TLPropertyType) type );

        assignedTypeProperty = null;
        // log.debug("Set assigned TL type");
        return getTL().getType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        // Take this's owning member out of the current assigned type's where used list
        if (getAssignedType() != null && getAssignedType().getOwningMember() != null)
            getAssignedType().getOwningMember().changeWhereUsed( getOwningMember(), null );

        if (type == null)
            setAssignedTLType( null );
        else if (type.getTL() instanceof TLAttributeType) {
            setAssignedTLType( (TLAttributeType) type.getTL() );
            type.getOwningMember().changeWhereUsed( null, getOwningMember() );
        }
        // log.debug( "Set assigned type " + getAssignedType() + " to " + this );
        return getAssignedType();
    }

    @Override
    public void setManditory(boolean value) {
        getTL().setMandatory( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public void setTLTypeName(String typeName) {
        getTL().setType( null );
        getTL().setTypeName( typeName );
    }
}
