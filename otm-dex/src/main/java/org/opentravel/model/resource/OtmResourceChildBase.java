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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * OTM Object for Resource objects.
 * 
 * FIXME - inheritance is different than for other otmObjects. Those create otm facades for inherited objects, resources
 * share the otmResourceChild
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmResourceChildBase<C> extends OtmModelElement<TLModelElement> implements OtmResourceChild {
    private static Logger log = LogManager.getLogger( OtmResourceChildBase.class );

    protected OtmResource owner = null;
    protected OtmResourceChild parent = null;
    protected OtmResource inheritedFrom = null;

    /**
     * Add this object to parent and set owner to parent.
     * 
     * @param tlo
     * @param parent
     */
    public OtmResourceChildBase(TLModelElement tlo, OtmResource parent) {
        super( tlo );
        this.owner = parent;
        if (parent != null)
            parent.add( this );
    }

    public OtmResourceChildBase(TLModelElement tlo, OtmResourceChild parent) {
        super( tlo );
        this.parent = parent;
        this.owner = parent.getOwningMember();
        if (parent instanceof OtmChildrenOwner) {
            ((OtmChildrenOwner) parent).add( this );
            // FIXME - Not Set Yet
            // this.inheritedFrom = parent.getInheritedFrom();
        }
    }

    // @Override
    // public int compareTo(OtmResourceChild o) {
    // return getName() != null && o != null ? this.getName().compareTo( o.getName() ) : 1;
    // }

    // @Override
    // public boolean equals(OtmResourceChild o) {
    // return o != null ? this.getName().equals( o.getName() ) : false;
    // }

    @Override
    public OtmResource getOwningMember() {
        return owner;
    }

    /**
     * 
     * @return resource or resourceChild parent
     */
    public OtmObject getParent() {
        return owner;
    }

    // ActionResponses can be inherited from either the base or parentRef???
    @Override
    public boolean isInherited() {
        // boolean result = false;
        // if (getParent() instanceof OtmChildrenOwner)
        // result = ((OtmChildrenOwner) getParent()).getChildren().contains( this );
        return getInheritedFrom() != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * When force is true, run validation on all children and where used library members.
     */
    @Override
    public boolean isValid(boolean force) {
        if (getLibrary() == null)
            return false; // Can't be valid if not in a library.
        if (force) {
            if (this instanceof OtmChildrenOwner) {
                synchronized (this) {
                    ((OtmChildrenOwner) this).getChildren().forEach( c -> {
                        if (c != this)
                            c.isValid( force );
                    } );

                }
            }
        }
        return super.isValid( force );
    }

    @Override
    public void setInheritedFrom(OtmResource base) {
        this.inheritedFrom = base;
        if (this instanceof OtmChildrenOwner)
            ((OtmChildrenOwner) this).getChildren().forEach( c -> ((OtmResourceChild) c).setInheritedFrom( base ) );
    }

    @Override
    public OtmResource getInheritedFrom() {
        return inheritedFrom;
    }

}
