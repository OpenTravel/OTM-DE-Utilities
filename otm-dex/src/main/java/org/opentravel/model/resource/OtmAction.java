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

package org.opentravel.model.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmAction extends OtmResourceChildBase<TLAction> implements OtmResourceChild, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmAction.class );

    public OtmAction(TLAction tla, OtmResource parent) {
        super( tla, parent );
        // tla.getActionId();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_ACTION;
    }

    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        return getTL().getActionId();
    }

    public OtmAction(String name, OtmResource parent) {
        super( new TLAction(), parent );
        setName( name );
    }

    public OtmActionRequest getRequest() {
        return (OtmActionRequest) OtmModelElement.get( getTL().getRequest() );
    }

    public List<OtmActionResponse> getResponses() {
        List<OtmActionResponse> list = new ArrayList<>();
        getTL().getResponses().forEach( r -> list.add( (OtmActionResponse) OtmModelElement.get( r ) ) );
        return list;
    }

    @Override
    public TLAction getTL() {
        return (TLAction) tlObject;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return getChildren();
    }

    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmActionRequest || child instanceof OtmActionResponse)
            if (!children.contains( child ))
                children.add( child );
        return null;
    }

    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
        return children;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
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
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelChildren()
     */
    @Override
    public void modelChildren() {
        if (getTL().getRequest() != null)
            new OtmActionRequest( getTL().getRequest(), this );
        if (getTL().getResponses() != null)
            getTL().getResponses().forEach( r -> new OtmActionResponse( r, this ) );
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelInheritedChildren()
     */
    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#isExpanded()
     */
    @Override
    public boolean isExpanded() {
        // TODO Auto-generated method stub
        return false;
    }
}
