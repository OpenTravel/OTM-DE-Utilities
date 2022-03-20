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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;

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
    private static Logger log = LogManager.getLogger( OtmAlias.class );

    private OtmLibraryMember parent = null;

    /**
     * @param tlAlias
     */
    public OtmAlias(TLAlias tl, OtmLibraryMember parent) {
        super( tl );
        this.parent = parent;
        parent.add( this );

        if (tl.getOwningEntity() == null && parent.getTL() instanceof TLAliasOwner)
            try {
                ((TLAliasOwner) parent.getTL()).addAlias( tl );
            } catch (UnsupportedOperationException e) {
                log.warn( "Could not add alias to parent TL object: " + parent.getTL().getClass().getSimpleName() );
            }
        // if (this.parent == null)
        // throw new IllegalStateException( "Created alias without parent." );
    }

    /**
     * Create an alias and add to the TL model and OTM parent
     *
     * @param owning library member
     */
    public OtmAlias(String name, OtmLibraryMember parent) {
        this( new TLAlias(), parent );
        if (parent.getTL() instanceof TLAliasOwner) {
            ((TLAliasOwner) parent.getTL()).addAlias( tlObject );
            parent.addAlias( getTL() );
            setName( name );
            // Consider adding an description or other comment about being inherited.
        }
    }

    @Override
    public String setName(String name) {
        tlObject.setName( name );
        return getName();
    }

    // @Override
    // public void delete(OtmObject property) {
    // // NO-OP - no delete-able children
    // }

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
    public boolean isInherited() {
        if (getLibrary().isMajorVersion())
            return false;
        // TODO - inherited aliases should not be edited. How to know if inherited?
        // for (OtmLibrary cl : getLibrary().getVersionChain().getLibraries())
        // for (OtmLibraryMember m : cl.getMembers())
        // if ((m.getAliases()))
        return false;
    }

    @Override
    public List<OtmObject> getChildren() {
        return children != null ? children : Collections.emptyList();
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return children != null ? children : Collections.emptyList();
    }

    @Override
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
        return Collections.emptyList();
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
        // No-Op
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
        return Collections.emptyList(); // No-Op
    }

    @Override
    public void modelInheritedChildren() {
        // No-Op
    }

    @Override
    public OtmAliasFacet add(OtmObject child) {
        if (child instanceof OtmAliasFacet && !children.contains( child )) {
            children.add( child );
            return (OtmAliasFacet) child;
        }
        return null;
    }

    @Override
    public void delete(OtmObject property) {
        // NO-OP
    }

}
