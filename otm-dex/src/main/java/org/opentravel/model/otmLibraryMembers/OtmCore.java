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
import org.opentravel.common.OtmTypeUserUtils;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmListFacet;
import org.opentravel.model.otmFacets.OtmRoleEnumeration;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    // @Override
    // public OtmObject setBaseType(OtmObject baseObj) {
    // if (baseObj instanceof OtmCore) {
    // TLExtension tlExt = getTL().getExtension();
    // if (tlExt == null)
    // tlExt = new TLExtension();
    // tlExt.setExtendsEntity( ((OtmCore) baseObj).getTL() );
    // getTL().setExtension( tlExt );
    // }
    // return getBaseType();
    // }

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
        log.debug( "FIXME - Needs simple facet" );
        // Add
        // Role Enumeration - TLRoleEnumeration / TLRole
        children.add( new OtmRoleEnumeration( getTL().getRoleEnumeration(), this ) );
        // Simple Facet - TLSimpleFacet
        children.add( new OtmListFacet( getTL().getSimpleListFacet(), this ) );
        children.add( new OtmListFacet( getTL().getSummaryListFacet(), this ) );
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
        Collection<OtmObject> ch = new ArrayList<>();
        children.forEach( c -> {
            // TODO - shouldn't simple be here too?
            if (c instanceof OtmSummaryFacet)
                ch.add( c );
            if (c instanceof OtmAlias)
                ch.add( c );
        } );

        // TODO - roles
        // TODO - lists
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
        OtmLibraryMember oldUser = null;
        if (getAssignedType() != null)
            oldUser = getAssignedType().getOwningMember();
        if (type != null && type.getTL() instanceof NamedEntity)
            setAssignedTLType( (NamedEntity) type.getTL() );

        // add to type's typeUsers
        type.getOwningMember().changeWhereUsed( oldUser, getOwningMember() );

        return getAssignedType();
    }

    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        if (type instanceof NamedEntity)
            getTL().getSimpleFacet().setSimpleType( type );
        assignedTypeProperty = null;
        log.debug( "Set assigned TL type" );
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


    //
    // @Override
    // public ComponentNode createMinorVersionComponent() {
    // TLBusinessObject tlMinor = (TLBusinessObject) createMinorTLVersion(this);
    // if (tlMinor != null)
    // return super.createMinorVersionComponent(new BusinessObjectNode(tlMinor));
    // return null;
    // }
    //
}
