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
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.ArrayList;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmResource extends OtmLibraryMemberBase<TLResource> {
    private static Log log = LogFactory.getLog( OtmResource.class );

    public OtmResource(TLResource tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmResource(String name, OtmModelManager mgr) {
        super( new TLResource(), mgr );
        setName( name );
    }



    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public TLResource getTL() {
        return (TLResource) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE;
    }

    @Override
    public OtmResource getBaseType() {
        return (OtmResource) super.getBaseType();
    }

    // @Override
    // public Collection<OtmObject> getChildrenHierarchy() {
    // Collection<OtmObject> ch = new ArrayList<>();
    // // children.forEach(c -> {
    // // if (c instanceof OtmIdFacet)
    // // ch.add(c);
    // // if (c instanceof OtmAlias)
    // // ch.add(c);
    // // });
    // return ch;
    // }

    @Override
    public OtmLibraryMember getOwningMember() {
        return this;
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }


    @Override
    public OtmResourceChild add(OtmObject child) {
        if (child instanceof OtmResourceChild) {
            // Make sure it has not already been added
            if (children == null)
                children = new ArrayList<>();
            else if (contains( children, child ))
                return null;

            if (inheritedChildren == null)
                inheritedChildren = new ArrayList<>();
            else if (contains( inheritedChildren, child ))
                return null;

            if (!child.isInherited())
                children.add( child );
            else
                inheritedChildren.add( child );
            return (OtmResourceChild) child;
        }
        return null;
    }


    /**
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#modelChildren()
     */
    @Override
    public void modelChildren() {
        // List<TLActionFacet> af = getTL().getActionFacets();
        getTL().getActions();
        getTL().getParamGroups();
        getTL().getParentRefs();

        getTL().getActionFacets().forEach( a -> new OtmActionFacet( a, this ) );;
        log.debug( "Modeled resource children" );
    }
}
