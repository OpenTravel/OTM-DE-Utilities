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

package org.opentravel.model.otmFacets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmRoleValue;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * OTM Object core object's role enumeration.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmRoleEnumeration extends OtmModelElement<TLRoleEnumeration>
    implements OtmPropertyOwner, OtmTypeProvider, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmRoleEnumeration.class );

    private OtmCore parent;

    public OtmRoleEnumeration(TLRoleEnumeration tlo, OtmCore parent) {
        super( tlo );
        this.parent = parent;
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmRoleValue add(OtmObject child) {
        if (child instanceof OtmRoleValue && !children.contains( child )) {
            children.add( child );
            return (OtmRoleValue) child;
        }
        return null;
    }

    /**
     * @param child
     */
    private OtmProperty add(TLRole child) {
        return add( new OtmRoleValue( child, this ) );
    }


    @Override
    public TLRoleEnumeration getTL() {
        return tlObject;
    }

    @Override
    public String setName(String name) {
        isValid( true );
        return getName();
    }

    @Override
    public OtmLibrary getLibrary() {
        return getParent().getLibrary();
    }

    public OtmCore getParent() {
        return parent;
    }

    @Override
    public OtmCore getOwningMember() {
        return getParent();
    }


    @Override
    public void modelChildren() {
        if (children == null)
            children = new ArrayList<>();
        else
            children.clear();
        for (TLRole role : getTL().getRoles())
            add( role );
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
    }

    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
    }

    @Override
    public List<OtmObject> getChildren() {
        synchronized (this) {
            if (children != null && children.isEmpty())
                modelChildren();
        }
        return children;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return getChildren();
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        return Collections.emptyList();
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public Icons getIconType() {
        // TODO Auto-generated method stub
        return ImageManager.Icons.FACET;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public OtmProperty add(TLModelElement tlChild) {
        return tlChild instanceof TLRole ? add( (TLRole) tlChild ) : null;
    }

    @Override
    public void delete(OtmObject child) {
        if (child instanceof OtmProperty)
            delete( (OtmProperty) child );
    }

    public void delete(OtmProperty property) {
        if (property.getTL() instanceof TLRole)
            getTL().removeRole( (TLRole) property.getTL() );
        remove( property );
    }

    @Override
    public void remove(OtmObject property) {
        children.remove( property );
    }

}
