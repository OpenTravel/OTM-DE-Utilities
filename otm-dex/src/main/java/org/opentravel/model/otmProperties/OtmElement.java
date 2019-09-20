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
import org.opentravel.schemacompiler.model.TLProperty;
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
public class OtmElement<T extends TLProperty> extends OtmPropertyBase<TLProperty> implements OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmElement.class );

    private StringProperty assignedTypeProperty;

    /**
     * Create a new element and set the parent. Adds tl to parent's tl.
     * 
     * @param property owner of this element
     */
    public OtmElement(T tl, OtmPropertyOwner parent) {
        super( tl, parent );

        // // Set the TL owner if not set.
        // if (tl.getOwner() == null && parent != null && parent.getTL() instanceof TLPropertyOwner) {
        // ((TLPropertyOwner) parent.getTL()).addElement( tl );
        // parent.add( this );
        // }
    }

    @Override
    public StringProperty assignedTypeProperty() {
        if (assignedTypeProperty == null)
            if (isEditable())
                assignedTypeProperty = new SimpleStringProperty();
            else
                assignedTypeProperty = new ReadOnlyStringWrapper();
        assignedTypeProperty.set( OtmTypeUserUtils.formatAssignedType( this ) );
        // log.debug( "returning assigned type property: " + assignedTypeProperty.toString() );
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
        return ImageManager.Icons.ELEMENT;
    }

    @Override
    public String getName() {
        return getTL().getName();
    }

    @Override
    public OtmPropertyType getPropertyType() {
        // FIXME - attr ref?
        return OtmPropertyType.ELEMENT;
    }

    @Override
    public TLProperty getTL() {
        return (TLProperty) tlObject;
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getTypeName();
    }

    @Override
    public boolean isManditory() {
        return getTL().isMandatory();
    }

    @Override
    public boolean isInherited() {
        return getTL().getOwner() != getParent().getTL();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Setting TL type does NOT enforce name control
     */
    @Override
    public TLPropertyType setAssignedTLType(NamedEntity type) {
        if (type instanceof TLPropertyType)
            getTL().setType( (TLPropertyType) type );
        assignedTypeProperty = null;
        log.debug( "Set assigned TL type" );
        return getTL().getType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        if (type == null)
            return null; // May not be a modeled type on undo

        OtmLibraryMember oldUser = null;
        if (getAssignedType() != null)
            oldUser = getAssignedType().getOwningMember();
        if (type.getTL() instanceof TLPropertyType)
            setAssignedTLType( (TLPropertyType) type.getTL() );
        if (type.isNameControlled())
            setName( type.getName() );

        // add to type's typeUsers
        type.getOwningMember().addWhereUsed( oldUser, getOwningMember() );
        return getAssignedType();
    }

    @Override
    public void setManditory(boolean value) {
        getTL().setMandatory( value );
    }

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
