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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmOperationFacet;
import org.opentravel.schemacompiler.model.TLOperation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * OTM Object for Operations. These are NOT library members, they are children of services.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmOperation extends OtmModelElement<TLOperation> implements OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmOperation.class );

    private OtmServiceObject parent;

    public OtmOperation(TLOperation tlo, OtmServiceObject parent) {
        super( tlo );
        this.parent = parent;
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmOperationFacet add(OtmObject child) {
        if (child instanceof OtmOperationFacet && !children.contains( child )) {
            children.add( child );
            return (OtmOperationFacet) child;
        }
        return null;
    }

    @Override
    public void delete(OtmObject property) {
        // TODO - delete-able children
    }

    @Override
    public void remove(OtmObject property) {
        // TODO - delete-able children
    }

    // public OtmOperation(String name, OtmLibraryMember parent) {
    // super(new TLOperation(), parent.getActionManager());
    // setName(name);
    // }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public TLOperation getTL() {
        return tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.OPERATION;
    }

    @Override
    public void modelChildren() {
        children.add( new OtmOperationFacet( getTL().getRequest(), this ) );
        children.add( new OtmOperationFacet( getTL().getResponse(), this ) );
        children.add( new OtmOperationFacet( getTL().getNotification(), this ) );
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return parent;
    }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
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
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

}
