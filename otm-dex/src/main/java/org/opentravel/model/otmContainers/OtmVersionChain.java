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

import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

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
public interface OtmVersionChain {



    /**
     * Simply add if not already in library list.
     * 
     * @param mLib
     */
    public abstract void add(OtmLibrary mLib);



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
    public boolean canAssignLaterVersion(OtmTypeUser subject);

    /**
     * Simply check if libraries list contains the library.
     * 
     * @param lib
     * @return
     */
    public abstract boolean contains(OtmLibrary lib);

    /**
     * @return baseNSwithName
     */
    // TODO - rename to chainName
    public String getBaseNamespace();

    /**
     * @return first editable library found, or null
     */
    public abstract OtmLibrary getEditable();

    /**
     * @return the library with the largest version number
     */
    public abstract OtmLibrary getLatestVersion();

    public abstract OtmLibraryMember getLatestVersion(OtmLibraryMember member);

    public abstract List<OtmLibrary> getLibraries();

    public abstract OtmLibrary getMajor();

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
    public OtmLibraryMember getNewMinorLibraryMember(OtmLibraryMember subject);

    /**
     * Create a new minor version of the owning member and VWA and enumerations will be returned, otherwise returns the
     * facet with matching name.
     * 
     * @param subject
     * @return property owner or null on error or facet not found.
     */
    public OtmPropertyOwner getNewMinorPropertyOwner(OtmPropertyOwner subject);

    public OtmTypeUser getNewMinorTypeUser(OtmTypeUser subject);

    public String getPrefix();

    /**
     * Return true if any library in the chain is editable.
     * 
     * @return
     */
    public abstract boolean isChainEditable();

    /**
     * Can the candidate be assigned as type in a minor version for users are currently assigned to the member?
     * <p>
     * To do so, the candidate must be a later version in the same version chain of the member.
     * 
     * @param member
     * @param candidate
     * @return
     */
    public abstract boolean isLaterVersion(OtmObject member, OtmObject candidate);

    /**
     * Is the major version of these libraries the latest in the model?
     * 
     * @param member
     * @return
     */
    // TODO - this seems very wrong. it only checks the minor versions.
    // for unmanaged, it returns false.
    public boolean isLatestChain();

    /**
     * Look into the chain and return true if this is the latest version (next version = null)
     * <p>
     * True if not in a chain.
     * 
     * @param lib
     * @return
     */
    public abstract boolean isLatest(OtmLibrary lib);

    /**
     * Is there a minor version with a larger version number. Must have same name and be the same object type.
     * 
     * @param member
     * @return true if this member's library version is greater than all other members in the chain with the same name.
     */
    public abstract boolean isLatestVersion(OtmLibraryMember member);

    /**
     * False if another library in the chain has a member with the same name.
     * <p>
     * True for contextual facets.
     * <p>
     * Check all libraries in the chain and return false if one of them contains the member. <br>
     * {@link OtmLibrary#contains(OtmLibraryMember) }
     * 
     * @param member
     * @return
     */
    public abstract boolean isNewToChain(OtmLibraryMember member);

    /**
     * Clear the version chain from all libraries in this chain.
     */
    public abstract void refresh();

    public abstract int size();

    /**
     * @param lib
     */
    public abstract void remove(OtmLibrary lib);

    /**
     * @return
     */
    public abstract boolean isEmpty();
}
