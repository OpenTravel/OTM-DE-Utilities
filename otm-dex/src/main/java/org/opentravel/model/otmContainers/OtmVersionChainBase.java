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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * OTM Version Chain - common base class.
 * <p>
 * Methods for accessing and management of libraries with the same name, namespace and major version number.
 * <p>
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmVersionChainBase implements OtmVersionChain {
    private static Log log = LogFactory.getLog( OtmVersionChainBase.class );

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
    // TODO - resolve where this should be and what it should do
    public static boolean isLaterVersion(OtmLibrary library, OtmLibrary candidate) {
        if (library == candidate)
            return false;
        if (!library.getNameWithBasenamespace().equals( candidate.getNameWithBasenamespace() ))
            return false;
        // try {
        if (library.getMajorVersion() != candidate.getMajorVersion())
            return false;
        if (library.getMinorVersion() >= candidate.getMinorVersion())
            return false;
        // } catch (VersionSchemeException e) {
        // log.debug( "Version scheme excption: " + e.getLocalizedMessage() );
        // return false;
        // }
        return true;
    }

    protected String chainName;
    protected String prefix;
    protected String name;
    protected OtmModelManager modelManager;


    /**
     * Create a version chain.
     * 
     * @param library
     */
    public OtmVersionChainBase(OtmLibrary library) {
        if (library == null)
            throw new IllegalArgumentException( "Library parameter missing. " );
        if (!(library.getTL() instanceof TLLibrary))
            throw new IllegalArgumentException( "Library parameter must be a TLLibrary. " );

        if (library.getModelManager() == null || library.getModelManager().getTlModel() == null)
            throw new IllegalArgumentException( "Library parameter missing model manager or TLModel. " );
        modelManager = library.getModelManager();

        // TODO - are these needed?
        chainName = library.getNameWithBasenamespace();
        name = library.getName();
        // prefix = getPrefix( library );
        prefix = library.getPrefix();
    }


    /** ********************************************************** **/

    // private String getPrefix(OtmLibrary library) {
    // if (library == null)
    // return "";
    // String result = library.getPrefix();
    // // // If there is more than one library in the minor version chain, change the prefix
    // // int dash = result.indexOf( '-' );
    // // if (dash > 0)
    // // result = result.substring( 0, dash );
    // // try {
    // // result += "-" + Integer.toString( library.getMajorVersion() );
    // // result += ".*";
    // // } catch (Exception e) {
    // // // NO-OP
    // // }
    // return result;
    // }

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
    /**
     * {@inheritDoc}
     * 
     * @see org.opentravel.model.otmContainers.OtmVersionChain#canAssignLaterVersion(org.opentravel.model.OtmTypeUser)
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


    public String getBaseNamespace() {
        return chainName;
    }

    // public abstract void add(OtmLibrary mLib);
    // public abstract boolean contains(OtmLibrary lib);
    // public abstract OtmLibrary getEditable();
    // public abstract OtmLibrary getLatestVersion();
    // public abstract OtmLibraryMember getLatestVersion(OtmLibraryMember member);
    // public abstract List<OtmLibrary> getLibraries();
    // public abstract OtmLibrary getMajor();
    // public abstract boolean isChainEditable();
    // public abstract boolean isEmpty();
    // public abstract boolean isLaterVersion(OtmObject member, OtmObject candidate);
    // public abstract boolean isLatestVersion(OtmLibraryMember member);
    // public abstract boolean isNewToChain(OtmLibraryMember member);
    // public abstract void refresh();
    // public abstract void remove(OtmLibrary lib);
    // public abstract int size();

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

        // If the latest member is in the target minor library use it
        OtmLibraryMember newMinorLibraryMember = null;
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

    /**
     * Create a new minor version of the owning member and VWA and enumerations will be returned, otherwise returns the
     * facet with matching name.
     * 
     * @param subject
     * @return property owner or null on error or facet not found.
     */
    public OtmPropertyOwner getNewMinorPropertyOwner(OtmPropertyOwner subject) {

        // Create minor version of owning member
        OtmLibraryMember newMinorLibraryMember = getNewMinorLibraryMember( subject.getOwningMember() );

        // Find the property owner to return
        OtmPropertyOwner newPropertyOwner = null;
        // VWA and Enum do not have descendant property owners, they are the property owner
        if (newMinorLibraryMember instanceof OtmValueWithAttributes)
            newPropertyOwner = (OtmValueWithAttributes) newMinorLibraryMember;
        else if (newMinorLibraryMember instanceof OtmEnumeration)
            newPropertyOwner = (OtmEnumeration<?>) newMinorLibraryMember;
        // Find name matching propertyOwner
        else if (newMinorLibraryMember != null) {
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

    public String getPrefix() {
        return prefix;
    }


    // /**
    // * Look into the chain and return true if this is the latest version (next version = null)
    // * <p>
    // * True if not in a chain.
    // *
    // * @param lib
    // * @return
    // */
    // public abstract boolean isLatest(OtmLibrary lib);

    /**
     * Is the major version of these libraries the latest in the model?
     * 
     * @param member
     * @return
     */
    // TODO - this seems very wrong. it only checks the minor versions.
    // for unmanaged, it returns false.
    public boolean isLatestChain() {
        return getLatestVersion() != null ? getLatestVersion().isLatestVersion() : false;
    }

    public abstract boolean isNewToChain(OtmLibraryMember member);



    public String toString() {
        return "Version Chain: " + chainName;
    }
}
