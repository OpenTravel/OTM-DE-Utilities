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
    private static Log log = LogFactory.getLog( OtmSimpleObject.class );

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
        if (type instanceof TLAttributeType)
            getTL().setParentType( (TLAttributeType) type );
        assignedTypeProperty = null;
        log.debug( "Set assigned TL type" );
        return getAssignedTLType();
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        OtmLibraryMember oldUser = null;
        if (getAssignedType() != null)
            oldUser = getAssignedType().getOwningMember();
        if (type != null && type.getTL() instanceof TLAttributeType) {
            setAssignedTLType( (TLAttributeType) type.getTL() );

            // add to type's typeUsers
            type.getOwningMember().changeWhereUsed( oldUser, getOwningMember() );
        }
        return getAssignedType();
    }

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

    //
    // @Override
    // public String getName() {
    // return emptyIfNull(getTLModelObject().getName());
    // }
    //
    // @Override
    // public FacetProviderNode getFacet_Default() {
    // return getFacet_Summary();
    // }
    //
    // @Override
    // public Image getImage() {
    // return Images.getImageRegistry().get(Images.BusinessObject);
    // }
    //
    // @Override
    // public void remove(AliasNode alias) {
    // getTLModelObject().removeAlias(alias.getTLModelObject());
    // clearAllAliasHolders();
    // }
    //
    // @Override
    // public ComponentNode createMinorVersionComponent() {
    // TLBusinessObject tlMinor = (TLBusinessObject) createMinorTLVersion(this);
    // if (tlMinor != null)
    // return super.createMinorVersionComponent(new BusinessObjectNode(tlMinor));
    // return null;
    // }
    //
    //
    // @Override
    // public NavNode getParent() {
    // return (NavNode) parent;
    // }
    // @Override
    // public void delete() {
    // // Must delete the contextual facets separately because they are separate library members.
    // for (Node n : getChildren_New())
    // if (n instanceof ContextualFacetNode)
    // n.delete();
    // super.delete();
    // }
    //
    // @Override
    // public void setName(String name) {
    // getTLModelObject().setName(NodeNameUtils.fixBusinessObjectName(name));
    // updateNames(NodeNameUtils.fixBusinessObjectName(name));
    // }
    //
}
