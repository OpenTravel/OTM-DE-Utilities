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

import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Abstract base class for all OTM properties.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmPropertyBase<T extends TLModelElement> extends OtmModelElement<TLModelElement>
    implements OtmProperty {

    private OtmPropertyOwner parent;

    /**
     * @param tl property owner
     */
    public OtmPropertyBase(T tl, OtmPropertyOwner parent) {
        super( tl );
        this.parent = parent;
        // Note: TL's owner must be set to add to the right list (isInherited())
        if (parent != null)
            parent.add( this );
    }

    @Override
    public void clone(OtmProperty source) {
        setName( fixName( source.getName() ) );
        setDescription( source.getDescription() );
        setExample( source.getExample() );
        if (this instanceof OtmTypeUser && source instanceof OtmTypeUser)
            ((OtmTypeUser) this).setAssignedType( ((OtmTypeUser) source).getAssignedType() );
        setManditory( source.isManditory() );
    }

    @Override
    public String getNamespace() {
        return getOwningMember().getNamespace();
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return parent.getOwningMember();
    }

    public OtmPropertyOwner getParent() {
        return parent;
    }

    public OtmPropertyOwner setParent(OtmPropertyOwner parent) {
        this.parent = parent;
        return parent;
    }

    public OtmPropertyType getPropertyType() {
        return OtmPropertyType.getType( this );
    }

    // @Override
    // public boolean isEditable() {
    // return getOwningMember() != null && getOwningMember().isEditable();
    // }

    @Override
    public String toString() {
        return getName();
    }
}
