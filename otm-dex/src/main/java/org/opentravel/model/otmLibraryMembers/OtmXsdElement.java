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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.model.XSDElement;

/**
 * OTM Object Node for XSD Element.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
public class OtmXsdElement extends OtmLibraryMemberBase<XSDElement> implements OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmXsdElement.class );

    // private StringProperty assignedTypeProperty;

    // public OtmXsdSimple(String name, OtmModelManager mgr) {
    // super(new XSDSimpleType(), mgr);
    // setName(name);
    // }

    public OtmXsdElement(XSDElement tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public void delete(OtmObject property) {
        // NO-OP - no delete-able children
    }

    @Override
    public OtmObject add(OtmObject child) {
        return null; // has no children to add
    }

    @Override
    public OtmLibrary getLibrary() {
        // OtmLibrary l = super.getLibrary();
        return super.getLibrary();
    }

    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        return null; // No-Op
    }

    // @Override
    // public StringProperty assignedTypeProperty() {
    // if (assignedTypeProperty == null) {
    // if (isEditable())
    // assignedTypeProperty = new SimpleStringProperty(OtmTypeUserUtils.formatAssignedType(this));
    // else
    // assignedTypeProperty = new ReadOnlyStringWrapper(OtmTypeUserUtils.formatAssignedType(this));
    // }
    // return assignedTypeProperty;
    // }

    // @Override
    // public TLPropertyType getAssignedTLType() {
    // return getTL().getParentType();
    // }
    //
    // @Override
    // public OtmTypeProvider getAssignedType() {
    // return OtmTypeUserUtils.getAssignedType(this);
    // }
    //
    // @Override
    // public String getTlAssignedTypeName() {
    // return getTL().getParentTypeName();
    // }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.XSD_SIMPLE;
    }

    /**
     * @return this
     */
    @Override
    public OtmXsdElement getOwningMember() {
        return this;
    }

    @Override
    public XSDElement getTL() {
        return (XSDElement) tlObject;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

    // @Override
    // public TLPropertyType setAssignedTLType(NamedEntity type) {
    // if (type instanceof TLAttributeType)
    // getTL().setParentType((TLAttributeType) type);
    // assignedTypeProperty = null;
    // log.debug("Set assigned TL type");
    // return getAssignedTLType();
    // }

    // @Override
    // public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
    // if (type != null && type.getTL() instanceof TLAttributeType)
    // setAssignedTLType((TLAttributeType) type.getTL());
    // return getAssignedType();
    // }

    // @Override
    // public String setName(String name) {
    // getTL().setName(name);
    // isValid(true);
    // return getName();
    // }

    // @Override
    // public void setTLTypeName(String name) {
    // getTL().setParentType(null);
    // getTL().setParentTypeName(name);
    // }
}
