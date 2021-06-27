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

package org.opentravel.model.otmContainers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OTM Version Chain.
 * <p>
 * Utilities for accessing libraries with the same name, namespace and major version number.
 * <p>
 * Should unmanaged libs have a chain?
 * 
 * @author Dave Hollander
 * 
 */
public class OtmVersionChainVersioned extends OtmVersionChainBase {
    private static Log log = LogFactory.getLog( OtmVersionChainVersioned.class );

    private List<OtmLibrary> libraries;
    private VersionChain<TLLibrary> tlVersionChain = null;
    private VersionChainFactory vcFactory = null;

    protected VersionChain<TLLibrary> getTlVersionChain(AbstractLibrary tlLib) {
        if (tlLib instanceof TLLibrary)
            tlVersionChain = getTlVersionChainFactory().getVersionChain( (TLLibrary) tlLib );
        return tlVersionChain;
    }

    /**
     * Get the version chain factory for the TLModel. Note, this is not a static factory and must be replaced when the
     * model's libraries change.
     * <p>
     * See {@linkplain TestOtmVersionChainVersioned#testGetTlVersionChain()} to demonstrate
     * 
     * @return
     */
    protected VersionChainFactory getTlVersionChainFactory() {
        if (vcFactory == null)
            vcFactory = new VersionChainFactory( modelManager.getTlModel() );
        return vcFactory;
    }


    /**
     * Use the TL Model to attempt to get a version chain factory.
     * 
     * @return the factory or null
     */
    @Deprecated
    public static VersionChainFactory getVersionChainFactory(TLModel tlModel) {
        VersionChainFactory versionChainFactory = null;
        versionChainFactory = new VersionChainFactory( tlModel );
        return versionChainFactory;
    }


    /**
     * Create a version chain.
     * 
     * @param library
     */
    public OtmVersionChainVersioned(OtmManagedLibrary library) {
        super( library );
        if (library == null)
            return;

        // libraries = library.getModelManager().getVersionChainLibraries( library );
        // // the library may not be found if unmanaged by namespace
        // if (!libraries.contains( library ))
        // libraries.add( library );
        // vcFactory = getVersionChainFactory( library.getModelManager().getTlModel() );
        // tlVersionChain = vcFactory.getVersionChain( (TLLibrary) library.getTL() );

        libraries = new ArrayList<>();
        add( library );

        // Should we pre-populate the chain?
    }

    /**
     * Simply add if not already in library list.
     * 
     * @param mLib
     */
    @Override
    public void add(OtmLibrary mLib) {
        if (mLib instanceof OtmManagedLibrary && !libraries.contains( mLib )) {
            libraries.add( mLib );
            tlVersionChain = null;
            // The factory itself must be updated.
            vcFactory = null;
        }
    }


    /**
     * Is there a newer minor version of the subject's assigned type provider?
     * <ul>
     * <li>The subject must be the latest version of the subject.
     * <li>The assigned type must not be the latest version.
     * </ul>
     * <p>
     * From language specification document:
     * <p>
     * For one term to be considered a later minor version of another term, all of the following conditions MUST be met:
     * <li>1. The terms must be of the same type (business object, core, etc.) and have the same name
     * <li>2. The terms MUST be declared in different libraries, and both libraries must have the same name, version
     * scheme, and base namespace URI
     * <li>3. The version of the extended term’s library MUST be lower than that of the extending term’s library
     * version, but both libraries MUST belong to the same major version chain
     */
    public boolean canAssignLaterVersion(OtmTypeUser subject) {
        if (subject == null || subject.getAssignedType() == null)
            return false;
        // log.debug( "Can assign later version? " + !isLatestVersion( subject.getAssignedType().getOwningMember() ) );

        // Return false if there is a later version of this subject
        if (!isLatestVersion( subject.getOwningMember() ))
            return false;
        // Return false if assigned type is the latest version
        return !isLatestVersion( subject.getAssignedType().getOwningMember() );
    }

    /**
     * Simply check if libraries list contains the library.
     * 
     * @param lib
     * @return
     */
    @Override
    public boolean contains(OtmLibrary lib) {
        return libraries.contains( lib );
    }

    /**
     * @return first editable library found, or null
     */
    @Override
    public OtmLibrary getEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return lib;
        return null;
    }

    /**
     * @return the library with the largest minor version number
     */
    @Override
    public OtmLibrary getLatestVersion() {
        OtmLibrary latest = null;
        if (libraries != null && !libraries.isEmpty()) {
            latest = libraries.get( 0 );
            for (OtmLibrary lib : libraries)
                if (lib.getMinorVersion() > latest.getMinorVersion())
                    latest = lib;
        }
        return latest;
    }

    @Override
    public OtmLibraryMember getLatestVersion(OtmLibraryMember member) {
        OtmLibraryMember latest = null;
        int vn = member.getLibrary().getMinorVersion();
        for (OtmLibrary lib : libraries) {
            if (lib.getMinorVersion() > vn && lib.getTL().getNamedMember( member.getName() ) != null) {
                LibraryMember tlMember = lib.getTL().getNamedMember( member.getName() );
                member = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) tlMember );
            }
        }
        return member;
    }

    @Override
    public List<OtmLibrary> getLibraries() {
        List<OtmLibrary> list = new ArrayList<>( libraries );
        Collections.sort( list, Collections.reverseOrder() );
        return Collections.unmodifiableList( list );
        // List<OtmLibrary> libs = Collections.sort( list );
        // return new ArrayList<>( libraries );
    }

    @Override
    public OtmLibrary getMajor() {
        for (OtmLibrary lib : libraries)
            if (!lib.isMinorVersion())
                return lib;
        return null;
    }



    /**
     * Return true if any library in the chain is editable.
     * 
     * @return
     */
    @Override
    public boolean isChainEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return true;
        return false;
    }

    /**
     * Can the candidate be assigned as type in a minor version for users are currently assigned to the member?
     * <p>
     * To do so, the candidate must be a later version in the same version chain of the member.
     * 
     * @param member
     * @param candidate
     * @return
     */
    @Override
    public boolean isLaterVersion(OtmObject member, OtmObject candidate) {
        if (member == null || candidate == null)
            return false;
        if (member.getLibrary() == null || candidate.getLibrary() == null)
            return false;
        if (member == candidate)
            return false;
        if (member.getClass() != candidate.getClass())
            return false;
        if (!member.getName().equals( candidate.getName() ))
            return false;
        // if (member.getLibrary() == candidate.getLibrary())
        // return false;
        // if (!member.getLibrary().getNameWithBasenamespace().equals( candidate.getLibrary().getNameWithBasenamespace()
        // ))
        // return false;
        return isLaterVersion( member.getLibrary(), candidate.getLibrary() );
    }

    // /**
    // * {@inheritDoc}
    // * <p>Look into the chain and return true if this is the latest version (next version = null)
    // *
    // * @param lib
    // * @return
    // */
    @Override
    public boolean isLatest(OtmLibrary lib) {
        VersionChain<TLLibrary> vc = getTlVersionChain( lib.getTL() );
        try {
            return vc != null && vc.getNextVersion( (TLLibrary) lib.getTL() ) == null;
        } catch (Exception e) {
            List<TLLibrary> versions = vc.getVersions();
            log.warn( "isLatest test of " + lib + " exception: " + e.getLocalizedMessage() );
            return false;
        }

        // VersionChain<TLLibrary> chain = get( lib.getNameWithBasenamespace() );
        // if (chain != null && lib.getTL() instanceof TLLibrary) {
        // // List<TLLibrary> versions = chain.getVersions();
        // return (chain.getNextVersion( (TLLibrary) lib.getTL() )) == null;
        // }
        // return true;
    }

    /**
     * Is there a minor version with a larger version number. Must have same name and be the same object type.
     * 
     * @param member
     * @return true if this member's library version is greater than all other members in the chain with the same name.
     */
    @Override
    public boolean isLatestVersion(OtmLibraryMember member) {
        int vn = member.getLibrary().getMinorVersion();
        for (OtmLibrary lib : libraries) {
            if (lib.getMinorVersion() > vn) {
                LibraryMember tl = lib.getTL().getNamedMember( member.getName() );
                if (tl != null && tl.getClass() == member.getTL().getClass())
                    return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNewToChain(OtmLibraryMember member) {
        // 3/30/2021 dmh - contextual facets are always considered new to chain
        if (member instanceof OtmContextualFacet)
            return true;

        // OtmLibrary mLib = member.getLibrary();
        for (OtmLibrary lib : libraries)
            if (lib != member.getLibrary() && lib.contains( member ))
                return false;
        return true;
    }

    /**
     * Clear the version chain from all libraries in this chain.
     */
    @Override
    public void refresh() {
        libraries.forEach( OtmLibrary::refreshVersionChain );
        // libraries.forEach( OtmLibrary::refresh );
    }

    @Override
    public int size() {
        return libraries.size();
    }

    /**
     * @param lib
     */
    @Override
    public void remove(OtmLibrary lib) {
        if (libraries.contains( lib )) {
            libraries.remove( lib );
            tlVersionChain = null;
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isEmpty() {
        return libraries.isEmpty();
    }
}
