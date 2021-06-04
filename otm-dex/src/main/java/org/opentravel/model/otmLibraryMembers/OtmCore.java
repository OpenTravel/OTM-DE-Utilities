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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.OtmTypeUserUtils;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmCoreValueFacet;
import org.opentravel.model.otmFacets.OtmListFacet;
import org.opentravel.model.otmFacets.OtmRoleEnumeration;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OTM Object Node for Core objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmCore extends OtmComplexObjects<TLCoreObject> implements OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmCore.class );

    private StringProperty assignedTypeProperty;

    public OtmCore(TLCoreObject tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmCore(String name, OtmModelManager mgr) {
        super( new TLCoreObject(), mgr );
        if (mgr != null && mgr.getEmptyType() != null)
            setAssignedTLType( mgr.getEmptyType().getTL() );
        setName( name );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.CORE;
    }

    @Override
    public TLCoreObject getTL() {
        return (TLCoreObject) tlObject;
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    /**
     * {@inheritDoc} Creates facets to represent facets in the TL object.
     */
    @Override
    public void modelChildren() {
        super.modelChildren(); // Will model facets

        // Role Enumeration - TLRoleEnumeration / TLRole
        if (OtmModelElement.get( getTL().getRoleEnumeration() ) != null)
            children.add( OtmModelElement.get( getTL().getRoleEnumeration() ) );
        else
            children.add( new OtmRoleEnumeration( getTL().getRoleEnumeration(), this ) );

        // Simple Facet - TLSimpleFacet
        if (OtmModelElement.get( getTL().getSimpleListFacet() ) != null)
            children.add( OtmModelElement.get( getTL().getSimpleListFacet() ) );
        else
            children.add( new OtmListFacet( getTL().getSimpleListFacet(), this ) );

        if (OtmModelElement.get( getTL().getSummaryListFacet() ) != null)
            children.add( OtmModelElement.get( getTL().getSummaryListFacet() ) );
        else
            children.add( new OtmListFacet( getTL().getSummaryListFacet(), this ) );

        if (OtmModelElement.get( getTL().getDetailListFacet() ) != null)
            children.add( OtmModelElement.get( getTL().getDetailListFacet() ) );
        else
            children.add( new OtmListFacet( getTL().getDetailListFacet(), this ) );
    }

    public OtmRoleEnumeration getRoles() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmRoleEnumeration)
                return (OtmRoleEnumeration) c;
        return null;
    }

    public OtmListFacet getDetailList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.DETAIL)
                return (OtmListFacet) c;
        return null;
    }

    public OtmListFacet getSummaryList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.SUMMARY)
                return (OtmListFacet) c;
        return null;
    }

    public OtmListFacet getSimpleList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.SIMPLE)
                return (OtmListFacet) c;
        return null;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new HashSet<>();
        ch.add( new OtmCoreValueFacet( this ) );
        // Add aliases first
        getChildren().forEach( c -> {
            if (c instanceof OtmAlias)
                ch.add( c );
        } );
        getChildren().forEach( c -> {
            if (c instanceof OtmSummaryFacet)
                ch.add( c );
            if (c instanceof OtmRoleEnumeration)
                ch.add( c );
            if (c instanceof OtmListFacet)
                ch.add( c );
        } );
        return ch;
    }

    @Override
    public OtmTypeProvider getAssignedType() {
        return OtmTypeUserUtils.getAssignedType( this );
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getSimpleFacet().getSimpleTypeName();
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

    @Override
    public NamedEntity getAssignedTLType() {
        NamedEntity tlType = getTL().getSimpleFacet().getSimpleType();
        return tlType instanceof NamedEntity ? (NamedEntity) tlType : null;
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        // Take this's owning member out of the current assigned type's where used list
        if (getAssignedType() != null && getAssignedType().getOwningMember() != null)
            getAssignedType().getOwningMember().changeWhereUsed( getOwningMember(), null );

        // OtmLibraryMember oldUser = null;
        // if (getAssignedType() != null)
        // oldUser = getAssignedType().getOwningMember();

        if (type == null) {
            setAssignedTLType( null );
        } else {
            if (type.getTL() instanceof NamedEntity) {
                NamedEntity result = setAssignedTLType( (NamedEntity) type.getTL() );
                if (result != null)
                    type.getOwningMember().changeWhereUsed( null, getOwningMember() );
            }
        }
        // What about typesUsed ?
        // log.debug( "Set assigned type to: " + getAssignedType() );
        return getAssignedType();
    }

    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        if (type instanceof NamedEntity || type == null)
            getTL().getSimpleFacet().setSimpleType( type );
        assignedTypeProperty = null;
        // log.debug( "Set assigned TL type" );
        return getAssignedTLType();
    }

    @Override
    public void setTLTypeName(String name) {
        getTL().getSimpleFacet().setSimpleType( null );
        getTL().getSimpleFacet().setSimpleTypeName( name );
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {
        if (getTL().getExtension() != null)
            log.warn( "TODO - model inherited children" );
    }
}
