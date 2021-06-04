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
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Empty OTM Version Chain.
 * <p>
 * Library is not part of a version chain. It is a local, unmanaged library.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmVersionChainEmpty extends OtmVersionChainBase {
    private static Log log = LogFactory.getLog( OtmVersionChainEmpty.class );

    private OtmLocalLibrary library;

    /**
     * Is candidate a later minor version of the reference library? Return true if all are true about the candidate:
     * <ol>
     * <li>is different than library
     * <li>has the same name and base namespace {@link OtmLibrary#getNameWithBasenamespace()}
     * <li>has the same major version number
     * <li>has a minor version number greater than library
     * </ol>
     * 
     * @param library
     * @param candidate
     * @return
     */
    public static boolean isLaterVersion(OtmLibrary library, OtmLibrary candidate) {
        return false;
    }


    /**
     * Create an EMPTY version chain. An empty chain only has one library, a local, unmanaged library.
     * 
     * @param library
     */
    public OtmVersionChainEmpty(OtmLocalLibrary library) {
        super( library );
        this.library = library;
    }

    /**
     * No-Op
     */
    @Override
    public void add(OtmLibrary mLib) {
        // NO-OP
    }

    @Override
    public void remove(OtmLibrary lib) {
        if (library == lib)
            library = null;
    }

    @Override
    public boolean isEmpty() {
        return library == null;
    }

    // /**
    // * Is there a newer minor version of the subject's assigned type provider?
    // * <ul>
    // * <li>The subject must be the latest version of the subject.
    // * <li>The assigned type must not be the latest version.
    // * </ul>
    // * <p>
    // * From language specification document:
    // * <p>
    // * For one term to be considered a later minor version of another term, all of the following conditions MUST be
    // met:
    // * <li>1. The terms must be of the same type (business object, core, etc.) and have the same name
    // * <li>2. The terms MUST be declared in different libraries, and both libraries must have the same name, version
    // * scheme, and base namespace URI
    // * <li>3. The version of the extended term’s library MUST be lower than that of the extending term’s library
    // * version, but both libraries MUST belong to the same major version chain
    // */
    // public boolean canAssignLaterVersion(OtmTypeUser subject) {
    // if (subject == null || subject.getAssignedType() == null)
    // return false;
    // // log.debug( "Can assign later version? " + !isLatestVersion( subject.getAssignedType().getOwningMember() ) );
    //
    // // Return false if there is a later version of this subject
    // if (!isLatestVersion( subject.getOwningMember() ))
    // return false;
    // // Return false if assigned type is the latest version
    // return !isLatestVersion( subject.getAssignedType().getOwningMember() );
    // }

    @Override
    public boolean contains(OtmLibrary lib) {
        return lib == library;
    }

    // public String getBaseNamespace() {
    // return baseNSwithName;
    // }

    /**
     * @return if the sole library for this chain is editable
     */
    @Override
    public OtmLibrary getEditable() {
        return library.isEditable() ? library : null;
    }

    /**
     * @return the library with the largest version number
     */
    @Override
    public OtmLibrary getLatestVersion() {
        return library;
    }

    @Override
    public OtmLibraryMember getLatestVersion(OtmLibraryMember member) {
        return member;
    }

    @Override
    public List<OtmLibrary> getLibraries() {
        List<OtmLibrary> list = new ArrayList<>();
        list.add( library );
        return list;
    }

    @Override
    public OtmLibrary getMajor() {
        return library;
    }

    /**
     * Create a copy of the subject's owning member in an minor library.
     * <p>
     * Minor library must be in the same chain as the subject and editable. The latest version of the subject's owning
     * member will be used to make the minor version.
     * 
     * @param subject
     * @return a property owner in the new object with the matching name or null if the minor version could not be
     *         created or error
     */
    @Override
    public OtmLibraryMember getNewMinorLibraryMember(OtmLibraryMember subject) {
        return null;
    }

    /**
     * Create a new minor version of the owning member and VWA and enumerations will be returned, otherwise returns the
     * facet with matching name.
     * 
     * @param subject
     * @return property owner or null on error or facet not found.
     */
    @Override
    public OtmPropertyOwner getNewMinorPropertyOwner(OtmPropertyOwner subject) {
        return null;
    }

    @Override
    public OtmTypeUser getNewMinorTypeUser(OtmTypeUser subject) {
        return null;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * Return true if any library in the chain is editable.
     * 
     * @return
     */
    @Override
    public boolean isChainEditable() {
        return library.isEditable();
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
        return false;
    }

    /**
     * Is the major version of these libraries the latest in the model?
     * <p>
     * for unmanaged, it returns false.
     * 
     * @param member
     * @return
     */
    @Override
    public boolean isLatestChain() {
        return false;
    }

    /**
     * Is there a minor version with a larger version number. Must have same name and be the same object type.
     * 
     * @param member
     * @return true if this member's library version is greater than all other members in the chain with the same name.
     */
    @Override
    public boolean isLatestVersion(OtmLibraryMember member) {
        return false;
    }

    /**
     * Always true for empty chains since library is not versioned.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isNewToChain(OtmLibraryMember member) {
        return true;
    }

    /**
     * Clear the version chain from all libraries in this chain.
     */
    @Override
    public void refresh() {
        library.refreshVersionChain();
    }

    @Override
    public int size() {
        return library == null ? 0 : 1;
    }


    /**
     * @see org.opentravel.model.otmContainers.OtmVersionChainBase#isLatest(org.opentravel.model.otmContainers.OtmLibrary)
     */
    @Override
    public boolean isLatest(OtmLibrary lib) {
        return true;
    }
}
