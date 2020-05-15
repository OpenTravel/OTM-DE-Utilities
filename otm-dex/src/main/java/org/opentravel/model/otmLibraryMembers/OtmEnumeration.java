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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmEnumerationValueFacet;
import org.opentravel.model.otmProperties.OtmEnumerationValue;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * OTM Object open and closed enumerations.
 * <p>
 * As with the TL Enumeration, children are the values.
 * <p>
 * ChildrenHierarchy() will return abstract display facets.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
// NOTE - member filter depends on sub-types starting with this class name!
public abstract class OtmEnumeration<E extends TLAbstractEnumeration>
    extends OtmLibraryMemberBase<TLAbstractEnumeration> implements OtmObject, OtmPropertyOwner {
    private static Log log = LogFactory.getLog( OtmEnumeration.class );

    public OtmEnumeration(E tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public E getTL() {
        return (E) tlObject;
    }

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

    @Override
    public OtmEnumeration<E> getOwningMember() {
        return this;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    // /**
    // * Does NOT add to backing TL Enumeration
    // *
    // * @param child
    // */
    // private void add(OtmEnumerationValue child) {
    // if (child != null)
    // children.add( child );
    // }
    //
    // private void addInherited(OtmEnumerationValue child) {
    // if (inheritedChildren == null)
    // inheritedChildren = new ArrayList<>();
    // if (child != null)
    // inheritedChildren.add( child );
    // }

    @Override
    public void modelChildren() {
        for (TLEnumValue ev : getTL().getValues())
            new OtmEnumerationValue( ev, (OtmEnumeration<TLAbstractEnumeration>) this );
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> hierarchy = new ArrayList<>();
        hierarchy.add( new OtmEnumerationValueFacet( (OtmPropertyOwner) this ) );
        return hierarchy;
    }

    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // force re-compute

        if (getTL().getExtension() != null) {
            OtmEnumeration<?> base = getBaseType();
            if (base instanceof OtmEnumeration) {
                // Create new facades to the existing TLValues and add to inherited list
                for (TLEnumValue v : ((TLAbstractEnumeration) base.getTL()).getValues()) {
                    // Use the factory so it does not get added to this enumeration as a child
                    OtmProperty p = OtmPropertyFactory.create( v, null );
                    inheritedChildren.add( p );
                    p.setParent( this );
                    // New facade's parent must NOT be tlValue's owning enumeration to be inherited
                    // add(p);
                }
            }
            // ((TLAbstractEnumeration) base.getTL()).getValues().forEach(
            // v -> addInherited( new OtmEnumerationValue( v, (OtmEnumeration<TLAbstractEnumeration>) this ) ) );
        }
    }

    @Override
    public OtmProperty add(TLModelElement tlChild) {
        OtmEnumerationValue newValue = null;
        if (tlChild instanceof TLEnumValue) {
            newValue = new OtmEnumerationValue( (TLEnumValue) tlChild, (OtmEnumeration<TLAbstractEnumeration>) this );
            children.add( newValue );
        }
        return newValue;
    }

    @Override
    public void delete(OtmObject property) {
        remove( property );
        if (property.getTL() instanceof TLEnumValue)
            getTL().removeValue( (TLEnumValue) property.getTL() );
    }

    @Override
    public void remove(OtmObject property) {
        children.remove( property );
    }

}
