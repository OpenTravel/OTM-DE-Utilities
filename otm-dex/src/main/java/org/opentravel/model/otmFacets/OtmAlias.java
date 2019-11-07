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
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract OTM Node for Aliases. Unlike the TL model, all aliases on an LibraryMember are collected under the OtmAlias
 * as OtmAliasFacets.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmAlias extends OtmModelElement<TLAlias> implements OtmTypeProvider, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmAlias.class );

    private OtmLibraryMember parent = null;

    /**
     * @param tlAlias
     */
    public OtmAlias(TLAlias tl, OtmLibraryMember parent) {
        super( tl );
        this.parent = parent;

        if (this.parent == null)
            throw new IllegalStateException( "Created alias without parent." );
    }

    @Override
    public void delete(OtmObject property) {
        // NO-OP - no delete-able children
    }

    @Override
    public void remove(OtmObject property) {
        // NO-OP - no delete-able children
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ALIAS;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return parent;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public List<OtmObject> getChildren() {
        return children;
    }

    // @Override
    // public boolean isExpanded() {
    // return false;
    // }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return children;
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        List<OtmTypeProvider> cps = new ArrayList<>();
        children.forEach( c -> {
            if (c instanceof OtmTypeProvider)
                cps.add( (OtmTypeProvider) c );
        } );
        return cps;
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return getChildrenTypeProviders();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        return Collections.emptyList();
    }

    @Override
    public void modelChildren() {
        // no-op
    }

    /**
     * @param tla
     */
    public void add(TLAlias tla) {
        // Protect against duplicates -- the underlying facets may refresh more often than the root alias.
        for (OtmObject child : children) {
            if (child.getName().equals( tla.getName() ))
                return;
        }
        children.add( new OtmAliasFacet( tla, this ) );
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {
        // TODO - model child ?? do we need to?
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmAliasFacet add(OtmObject child) {
        if (child instanceof OtmAliasFacet && !children.contains( child )) {
            children.add( child );
            return (OtmAliasFacet) child;
        }
        return null;
    }

}
