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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.model.TLService;

import java.util.Collection;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmServiceObject extends OtmLibraryMemberBase<TLService> {
    private static Logger log = LogManager.getLogger( OtmServiceObject.class );

    public OtmServiceObject(TLService tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmServiceObject(String name, OtmModelManager mgr) {
        super( new TLService(), mgr );
        setName( name );
    }

    @Override
    public OtmOperation add(OtmObject child) {
        if (child instanceof OtmOperation) {
            if (!contains( children, child )) {
                children.add( child );
            }
            return (OtmOperation) child;
        }
        return null;
    }

    @Override
    public void delete(OtmObject property) {
        // TODO - delete-able children
    }

    // @Override
    // public OtmServiceObject copy() {
    // return null; // You can't copy services
    // }

    @Override
    public void remove(OtmObject property) {
        // TODO - delete-able children
    }


    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public TLService getTL() {
        return (TLService) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.SERVICE;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        // Collection<OtmObject> ch = new ArrayList<>();
        return getChildren();
    }

    @Override
    public void modelChildren() {
        getTL().getOperations().forEach( o -> children.add( new OtmOperation( o, this ) ) );
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return this;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        return null; // No-Op
    }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

}
