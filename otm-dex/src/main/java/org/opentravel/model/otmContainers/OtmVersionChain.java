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
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.List;

/**
 * OTM Version Chain. Utilities for accessing libraries with the same name, namespace and major version number.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmVersionChain {
    private static Log log = LogFactory.getLog( OtmVersionChain.class );

    List<OtmLibrary> libraries;
    String baseNSwithName;

    public OtmVersionChain(OtmLibrary library) {
        libraries = library.getModelManager().getVersionChain( library );
        baseNSwithName = library.getNameWithBasenamespace();

        // Verify - comment out when not debugging/testing
        for (OtmLibrary lib : libraries) {
            assert lib.getNameWithBasenamespace().equals( baseNSwithName );
            try {
                assert lib.getMajorVersion() == library.getMajorVersion();
            } catch (VersionSchemeException e) {
                log.debug( "Version Scheme exception creating version chain. " + e.getLocalizedMessage() );
                assert false; // Error
            }
        }
    }

    public OtmLibrary getEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return lib;
        return null;
    }

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
    public boolean isChainEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return true;
        return false;
    }

    /**
     * Is there a minor version with a larger version number. Must have same name and be the same object type.
     * 
     * @param member
     * @return true if this member's library version is greater than all other members in the chain with the same name.
     */
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
     * Is the major version of these libraries the latest in the model?
     * 
     * @param member
     * @return
     */
    public boolean isLatestChain() {
        return getLatestVersion() != null ? getLatestVersion().isLatestVersion() : false;
    }

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

    public boolean contains(OtmLibrary lib) {
        return libraries.contains( lib );
    }

    /**
     * @param member
     * @return
     */
    public boolean isNewToChain(OtmLibraryMember member) {
        for (OtmLibrary lib : libraries)
            if (lib != member.getLibrary() && lib.contains( member ))
                return false;
        return true;
    }

    /**
     * Create a copy of the subject's owning member in an minor library. Minor library must be in the chain as the
     * subject and editable. The latest version of the subject's owning member will be used to make the minor version.
     * 
     * @param subject
     * @return a property owner in the new object with the matching name or null
     */
    public OtmLibraryMember getNewMinorLibraryMember(OtmLibraryMember subject) {
        OtmLibrary subjectLibrary = subject.getLibrary();
        if (subjectLibrary == null)
            return null;
        OtmLibrary minorLibrary = subjectLibrary.getVersionChain().getEditable();
        if (minorLibrary == null)
            return null;
        // Get the latest version of this member
        OtmLibraryMember latestMember = subjectLibrary.getVersionChain().getLatestVersion( subject );
        if (latestMember == null)
            return null;
        OtmLibraryMember newMinorLibraryMember = null;

        // If the latest member is in the target minor library us it
        if (latestMember.getLibrary() == minorLibrary)
            newMinorLibraryMember = latestMember;
        else
            // Create new minor version of this member
            newMinorLibraryMember = latestMember.createMinorVersion( minorLibrary );

        if (newMinorLibraryMember == null)
            return null; // how to inform user of error?

        // Contextual facets?
        for (OtmContributedFacet cf : newMinorLibraryMember.getChildrenContributedFacets())
            log.debug( "What to do here? " );

        return newMinorLibraryMember;
    }

    public OtmPropertyOwner getNewMinorPropertyOwner(OtmPropertyOwner subject) {
        OtmLibraryMember newMinorLibraryMember = getNewMinorLibraryMember( subject.getOwningMember() );
        OtmPropertyOwner newPropertyOwner = null;
        if (newMinorLibraryMember != null) {
            // Find matching propertyOwner
            for (OtmPropertyOwner p : newMinorLibraryMember.getDescendantsPropertyOwners())
                if (p.getName().equals( subject.getName() ))
                    newPropertyOwner = p;
        }
        return newPropertyOwner;
    }

    public OtmTypeUser getNewMinorTypeUser(OtmTypeUser subject) {
        OtmPropertyOwner np = getNewMinorPropertyOwner( ((OtmProperty) subject).getParent() );
        if (np == null)
            return null;
        // OtmLibraryMember newMinorLibraryMember = getNewMinorLibraryMember( subject.getOwningMember() );
        OtmTypeUser newTypeUser = null;
        OtmProperty newProperty = null;
        // New objects will NOT have any type users!
        LibraryElement newTL = subject.getTL().cloneElement( np.getLibrary().getTL() );
        if (newTL instanceof TLModelElement)
            newProperty = OtmPropertyFactory.create( (TLModelElement) newTL, np );
        if (newProperty instanceof OtmTypeUser)
            newTypeUser = (OtmTypeUser) newProperty;

        return newTypeUser;
    }

    // From language specification document:
    // . For one term to be considered a later minor version of another term, all of the following conditions MUST be
    // met:
    // 1. The terms must be of the same type (business object, core, etc.) and have the same name
    // 2. The terms MUST be declared in different libraries, and both libraries must have the same name, version scheme,
    // and base namespace URI
    // 3. The version of the extended term’s library MUST be lower than that of the extending term’s library version,
    // but both libraries MUST belong to the same major version chain
    // Is there a newer minor version of this type provider?
    public boolean canAssignLaterVersion(OtmTypeUser subject) {
        if (subject == null || subject.getAssignedType() == null)
            return false;
        return !isLatestVersion( subject.getAssignedType().getOwningMember() );
    }

    /**
     * Can the candidate be assigned as type in a minor version for users are currently assigned to the member? To do
     * so, the candidate must be a later version in the same version chain of the member.
     * 
     * @param member
     * @param candidate
     * @return
     */
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
        if (member.getLibrary() == candidate.getLibrary())
            return false;
        if (!member.getLibrary().getNameWithBasenamespace().equals( candidate.getLibrary().getNameWithBasenamespace() ))
            return false;
        try {
            if (member.getLibrary().getMajorVersion() != candidate.getLibrary().getMajorVersion())
                return false;
            if (member.getLibrary().getMinorVersion() >= candidate.getLibrary().getMinorVersion())
                return false;
        } catch (VersionSchemeException e) {
            return false;
        }

        return true;
    }
}
