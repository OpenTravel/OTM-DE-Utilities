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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.common.OtmTypeUserUtils;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
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
public class OtmAttribute<T extends TLAttribute> extends OtmProperty<TLAttribute> implements OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmAttribute.class );

    private StringProperty assignedTypeProperty;

    /**
     * Create a new attribute and set the parent.
     * 
     * @param property owner of this attribute
     */
    public OtmAttribute(T tl, OtmPropertyOwner parent) {
        super( tl, parent );
        tlObject = tl;
    }

    @Override
    public StringProperty assignedTypeProperty() {
        if (assignedTypeProperty == null) {
            if (isEditable())
                assignedTypeProperty = new SimpleStringProperty( OtmTypeUserUtils.formatAssignedType( this ) );
            else
                assignedTypeProperty = new ReadOnlyStringWrapper( OtmTypeUserUtils.formatAssignedType( this ) );
        }
        return assignedTypeProperty;
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
    public String getRole() {
        return UserSelectablePropertyTypes.Attribute.toString();
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
        return getTL().getOwner() != getParent().getTL();
    }

    @Override
    public boolean isManditory() {
        return getTL().isMandatory();
    }

    /**
     * Useful for types that are not in the model manager.
     */
    // TESTME - FIXME - how to limit to acceptable attribute types? TLAttributeType MAY be too limiting
    @Override
    public TLPropertyType setAssignedTLType(NamedEntity type) {
        if (type instanceof TLPropertyType)
            getTL().setType( (TLPropertyType) type );
        assignedTypeProperty = null;
        // log.debug("Set assigned TL type");
        return getTL().getType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        OtmLibraryMember oldUser = getAssignedType() == null ? null : getAssignedType().getOwningMember();
        if (type != null && type.getTL() instanceof TLAttributeType) {
            setAssignedTLType( (TLAttributeType) type.getTL() );

            // add to type's typeUsers
            type.getOwningMember().addWhereUsed( oldUser, getOwningMember() );
        }
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

    @Override
    public String toString() {
        return getName();
    }

}
