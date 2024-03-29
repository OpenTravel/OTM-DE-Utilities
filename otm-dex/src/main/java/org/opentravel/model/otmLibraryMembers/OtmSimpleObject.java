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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.OtmTypeUserUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OTM Object Node for Simple objects.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
public class OtmSimpleObject extends OtmSimpleObjects<TLSimple> implements OtmTypeUser {
    private static Logger log = LogManager.getLogger( OtmSimpleObject.class );

    private StringProperty assignedTypeProperty;

    public OtmSimpleObject(String name, OtmModelManager mgr) {
        super( new TLSimple(), mgr );
        setName( name );
    }

    public OtmSimpleObject(TLSimple tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public void delete(OtmObject property) {
        // NO-OP - no delete-able children
    }

    @Override
    public StringProperty assignedTypeProperty() {
        if (assignedTypeProperty == null) {
            if (isEditable())
                assignedTypeProperty = new SimpleStringProperty();
            else
                assignedTypeProperty = new ReadOnlyStringWrapper();
        }
        assignedTypeProperty.set( OtmTypeUserUtils.formatAssignedType( this ) );
        return assignedTypeProperty;
    }

    @Override
    public TLPropertyType getAssignedTLType() {
        return getTL().getParentType();
    }

    @Override
    public OtmTypeProvider getAssignedType() {
        return OtmTypeUserUtils.getAssignedType( this );
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getParentTypeName();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.SIMPLE;
    }

    /**
     * @return this
     */
    @Override
    public OtmSimpleObject getOwningMember() {
        return this;
    }

    @Override
    public TLSimple getTL() {
        return (TLSimple) tlObject;
    }

    @Override
    public TLPropertyType setAssignedTLType(NamedEntity type) {
        // 11/11/2020 - Will fail if namespace is null
        if (type instanceof TLAttributeType || type == null)
            getTL().setParentType( (TLAttributeType) type );
        assignedTypeProperty = null;
        // log.debug( "Set assigned TL type" );
        return getAssignedTLType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        OtmLibraryMember oldUser = null;
        if (getAssignedType() != null)
            oldUser = getAssignedType().getOwningMember();
        if (type == null) {
            setAssignedTLType( null );
        } else {
            if (type.getTL() instanceof TLAttributeType) {
                setAssignedTLType( (TLAttributeType) type.getTL() );

                // add to type's typeUsers
                type.getOwningMember().changeWhereUsed( oldUser, getOwningMember() );
            }
        }
        return getAssignedType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * No-Op. Use {@link OtmSimpleObject#setAssignedType(OtmTypeProvider)}
     * 
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#setBaseType(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        return null; // No-Op
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    public boolean isList() {
        return getTL().isListTypeInd();
    }

    public void setList(boolean value) {
        getTL().setListTypeInd( value );
    }

    @Override
    public void setTLTypeName(String name) {
        getTL().setParentType( null );
        getTL().setParentTypeName( name );
    }
}
