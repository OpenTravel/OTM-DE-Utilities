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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmProperties.OtmEnumerationValue;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;

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
    implements OtmTypeProvider, OtmChildrenOwner {
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
    public OtmEnumerationValue add(OtmObject child) {
        if (child instanceof OtmEnumerationValue && !children.contains( child )) {
            children.add( child );
            return (OtmEnumerationValue) child;
        }
        return null;
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

    /**
     * @param child
     */
    private void add(TLRole child) {
        // if (child != null)
        // children.add(child);
    }

    @Override
    public void modelChildren() {
        for (TLRole role : getTL().getRoles())
            add( role ); // TODO - model child ?? do we need to?
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
    }

    @Override
    public void modelInheritedChildren() {}

    @Override
    public List<OtmObject> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        // TODO Auto-generated method stub
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

    // @Override
    // public boolean isExpanded() {
    // return false;
    // }

    @Override
    public Icons getIconType() {
        // TODO Auto-generated method stub
        return ImageManager.Icons.FACET;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

}
