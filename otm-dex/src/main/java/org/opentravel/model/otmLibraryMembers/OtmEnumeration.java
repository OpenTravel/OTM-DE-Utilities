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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmProperties.OtmEnumerationValue;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OTM Object open and closed enumerations.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
// NOTE - member filter depends on sub-types starting with this class name!
public abstract class OtmEnumeration<E extends TLAbstractEnumeration>
    extends OtmLibraryMemberBase<TLAbstractEnumeration> implements OtmObject, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmEnumeration.class );

    public OtmEnumeration(E tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public E getTL() {
        return (E) tlObject;
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmEnumerationValue add(OtmObject child) {
        if (child instanceof OtmEnumerationValue && !children.contains( child )) {
            children.add( child );
            return (OtmEnumerationValue) child;
        }
        return null;
    }

    /**
    * 
    */
    @Override
    public OtmEnumeration<?> getBaseType() {
        return (OtmEnumeration<?>) super.getBaseType();
    }

    @Override
    public List<OtmTypeProvider> getUsedTypes() {
        return Collections.emptyList();
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    // @Override
    // public OtmLibrary getLibrary() {
    // return mgr.get(getTL().getOwningLibrary());
    // }

    @Override
    public OtmEnumeration<E> getOwningMember() {
        return this;
    }

    // @Override
    // public String getLibraryName() {
    // String libName = "";
    // if (getTL().getOwningLibrary() != null)
    // libName = getTL().getOwningLibrary().getName();
    // return libName;
    // }

    // @Override
    // public boolean isEditable() {
    // OtmLibrary ol = null;
    // if (mgr != null || getTL() != null)
    // ol = mgr.get(getTL().getOwningLibrary());
    // return ol != null && ol.isEditable();
    // }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

    /**
     * Does NOT add to backing TL Enumeration
     * 
     * @param child
     */
    private void add(OtmEnumerationValue child) {
        if (child != null)
            children.add( child );
    }

    private void addInherited(OtmEnumerationValue child) {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        if (child != null)
            inheritedChildren.add( child );
    }

    @Override
    public void modelChildren() {
        for (TLEnumValue ev : getTL().getValues())
            add( new OtmEnumerationValue( ev, (OtmEnumeration<TLAbstractEnumeration>) this ) );
    }

    // @Override
    // public List<OtmObject> getInheritedChildren() {
    // return Collections.emptyList(); // TODO
    // }

    @Override
    public void modelInheritedChildren() {
        if (getTL().getExtension() != null) {
            OtmEnumeration<?> base = getBaseType();
            if (base instanceof OtmEnumeration)
                // TEST - try using the actual facade, not creating a new one
                base.getChildren().forEach( c -> addInherited( (OtmEnumerationValue) c ) );

            // ((TLAbstractEnumeration) tlBase).getValues().forEach(
            // v -> addInherited(new OtmEnumerationValue(v, (OtmEnumeration<TLAbstractEnumeration>) this)));
            log.warn( "TEST - modeled inherited children" );
        }
    }

}
