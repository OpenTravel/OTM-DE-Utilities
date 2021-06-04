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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmModelMapsManager;
import org.opentravel.model.OtmModelMembersManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

/**
 * OTM Object for libraries. Does <b>NOT</b> provide access to members.
 * 
 * <P>
 * To Do:
 * <li>Create local, major, minor and builtIn library sub-types.
 * <li>Library Factory
 * <li>Create abstract managedLibrary sub-type
 * <li>Make this abstract.
 * <li>Make version chain persistence in model manager
 * <li>Only major and minor have version chains
 * <p>
 * Design Note: this is an abstract class and not an interface because the sub-types are so similar and easily discerned
 * by their sub-type.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmLibrary implements Comparable<OtmLibrary> {
    private static Log log = LogFactory.getLog( OtmLibrary.class );

    public static final String NO_LOCKEDBYUSER = "";

    protected OtmModelManager mgr;
    protected List<ProjectItem> projectItems = new ArrayList<>();
    protected AbstractLibrary tlLib;
    protected ValidationFindings findings;
    protected Map<OtmLibrary,List<OtmLibraryMember>> providerMap = null;
    protected Map<OtmLibrary,List<OtmLibraryMember>> usersMap = null;

    /**
     * Should only be called by Factory.
     * <p>
     * See {@link OtmLibraryFactory#newLibrary(AbstractLibrary, OtmModelManager)}
     * 
     * @param tl
     * @param mgr
     */
    protected OtmLibrary(AbstractLibrary tl, OtmModelManager mgr) {
        this.mgr = mgr;
        tlLib = tl;
    }

    // @Deprecated
    // protected OtmLibrary(OtmModelManager mgr) {
    // this.mgr = mgr;
    // }

    // // DONE - delete this constructor
    // @Deprecated
    // public OtmLibrary(ProjectItem pi, OtmModelManager mgr) {
    // this.mgr = mgr;
    // projectItems.add( pi );
    // tlLib = pi.getContent();
    // }

    @Override
    public int compareTo(OtmLibrary o) {
        return getVersionedName().compareTo( o.getVersionedName() );
        // return getName().compareTo( o.getName() );
    }

    public String getVersionedName() {
        return getName() + "_v" + getVersion();
    }

    /**
     * Adds member to this library by adding the TL member to the TL library and model manager.
     * <p>
     * Note: will not add second member if one with the same name already is in TL library
     * <p>
     * Note: will not add second service and return null if attempted.
     * <p>
     * <b>Note:</b> adds member to the model manager. See {@link OtmModelManager#add(OtmLibraryMember)}
     * 
     * @param a library member
     * @return the member if added OK
     */
    public OtmLibraryMember add(OtmLibraryMember member) {
        // Libraries can only have 1 service
        if (member instanceof OtmServiceObject && hasService())
            return null;

        if (member != null && member.getTL() instanceof LibraryMember)
            try {
                // make sure not already a member
                if (getTL().getNamedMember( ((LibraryMember) member.getTL()).getLocalName() ) == null)
                    getTL().addNamedMember( (LibraryMember) member.getTL() );
                else {
                    log.warn( "Did not add member " + member + " to library because it was already a member." );
                    return null;
                }
                // Add to model manager
                if (getModelManager() != null)
                    getModelManager().add( member );

                // Sanity check
                // FIXME - comment all this out! Name the junit where tested.
                if (member.getTlLM().getOwningLibrary() != getTL())
                    log.warn( "TLMember does not have correct owning library." );
                if (getModelManager().get( getTL() ) != this)
                    log.warn( "Could not find this library in manager." );
                if (getModelManager() != member.getModelManager())
                    log.warn( "Model managers are different." );
                if (member.getLibrary() != this) {
                    // if (member.getTlLM() == null || member.getTlLM().getOwningLibrary() == null)
                    // log.warn( "OOPs1" );
                    // OtmLibrary owningLib = mgr.get( member.getTlLM().getOwningLibrary() );
                    // OtmLibrary owningLib2 = member.getLibrary();
                    log.warn( "Newly added member does not have correct library." );
                }
                return member;
            } catch (IllegalArgumentException e) {
                log.warn( "Exception: " + e.getLocalizedMessage() );
            }
        else
            log.warn( "Tried to add a non-library member " + member + " to library." );
        return null;
    }


    /**
     * Add the project item to the list maintained by the library. Libraries can be members of multiple, open projects.
     * 
     * @param pi
     */
    public void add(ProjectItem pi) {
        if (pi.getContent() == null
            || (!(pi.getNamespace().equals( getTL().getNamespace() ) && pi.getContent().getName().equals( getName() ))))

            // if (pi.getContent() != tlLib)
            throw new IllegalArgumentException( "Can not add project item with wrong library." );
        projectItems.add( pi );
        // log.debug( "Added project item to " + this.getName() + ". Now has " + projectItems.size() + " items." );
    }

    /**
     * Can this library in its current state and status be locked?
     * 
     * @return false unless managed and allowed by {@linkplain OtmManagedLibrary#canBeLocked()}.
     */
    public boolean canBeLocked() {
        return false;
    }

    /**
     * Can this library be unlocked?
     * 
     * @return false unless managed and allowed by {@linkplain OtmManagedLibrary#canBeUnlocked()}.
     */
    public boolean canBeUnlocked() {
        return false;
    }

    public boolean contains(AbstractLibrary aLib) {
        if (tlLib == aLib)
            return true;
        for (ProjectItem pi : projectItems)
            if (pi.getContent() == aLib)
                return true;
        return false;
    }

    /**
     * Test the TL library to see if it has a named member with the same name.
     * 
     * @param member
     * @return
     */
    public boolean contains(OtmLibraryMember member) {
        return getTL().getNamedMember( member.getName() ) != null;
    }

    /**
     * Delete member. Remove from this library and underlying TL library and from the model manager.
     * <ul>
     * <li>{@linkplain OtmModelMembersManager#remove(OtmLibraryMember)}
     * <li>{@linkplain AbstractLibrary#removeNamedMember(LibraryMember)}
     * <li>Handle Contextual facet
     * </ul>
     */
    public void delete(OtmLibraryMember member) {
        if (member != null) {
            if (member.getModelManager() != null)
                member.getModelManager().remove( member );
            // assert (member.getModelManager().getMember( member.getTL() ) == null);
            if (getTL() != null)
                getTL().removeNamedMember( member.getTlLM() );
            // Contextual facets are the only library members that also are children of other members via the
            // contributed facet.
            if (member instanceof OtmContextualFacet && ((OtmContextualFacet) member).getContributedObject() != null)
                ((OtmContextualFacet) member).getContributedObject().delete( member );
            // TODO - what about base types?
            // Leave where used. It is needed for un-delete.
        }
    }

    public boolean sameBaseNamespace(OtmLibrary otherLibrary) {
        if (otherLibrary == null || this.getBaseNS() == null || otherLibrary.getBaseNS() == null)
            return false;
        return this.getBaseNS().equals( otherLibrary.getBaseNS() );
    }

    /**
     * Get Read-only, Minor or Full action manager.
     * 
     * @return action manager
     */
    public abstract DexActionManager getActionManager();

    /**
     * Get Read-only, Minor or Full action manager based on type of the library and its chain.
     *
     * @return action manager
     */
    public abstract DexActionManager getActionManager(OtmLibraryMember member);

    /**
     * Get the base namespace as reported by the underlying library.
     * <p>
     * The base namespace URI is the portion that does not include the version identifier suffix.
     * 
     * @return project item's base namespace or empty string
     */
    public abstract String getBaseNS();
    // * Get the base namespace from the first project item
    // * @return project item's base namespace or empty string
    // // TESTME
    // // Fail-safe: if fails the instance test, try the PI
    // return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();


    /**
     * 
     * @return namespace / name
     */
    public String getFullName() {
        return getTL() != null ? getTL().getNamespace() + "/" + getTL().getName() : null;
    }

    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
    }

    public Image getIcon() {
        return ImageManager.getImage( this.getIconType() );
    }

    public List<OtmLibrary> getIncludes() {
        List<OtmLibrary> libs = new ArrayList<>();
        List<TLInclude> includes = tlLib.getIncludes();
        for (TLInclude include : includes) {
            if (include.getOwningLibrary() != null)
                libs.add( mgr.get( include.getOwningLibrary() ) );
        }
        return libs;
    }

    /**
     * Override if lock-able
     * 
     * @return
     */
    public String getLockedBy() {
        // for (ProjectItem pi : projectItems)
        // if (pi.getLockedByUser() != null)
        // return pi.getLockedByUser();
        return NO_LOCKEDBYUSER;
    }

    /**
     * @param namespace
     * @return the major version number
     */
    public int getMajorVersion() {
        return OtmLibraryFactory.getMajorVersionNumber( getTL() );
    }

    // /**
    // * Get the managing project from the model manager. {@link OtmModelManager#getManagingProject(OtmLibrary)}
    // *
    // * @return
    // */
    // @Deprecated
    // public OtmProject getManagingProject() {
    // return mgr.getManagingProject( this );
    // }

    /**
     * Get the TL Project Manager from this library's model manager.
     * 
     * @return Project Manager or null
     */
    public ProjectManager getTLProjectManager() {
        return getModelManager() != null ? getModelManager().getProjectManager() : null;
    }

    /**
     * Get the members of this library from the model manager.
     * 
     * @return
     */
    public List<OtmLibraryMember> getMembers() {
        return getModelManager().getMembers( this );
    }

    /**
     * @return the minor version number
     * @throws VersionSchemeException
     */
    public int getMinorVersion() {
        return OtmLibraryFactory.getMinorVersionNumber( getTL() );
        //
        // int vn = 0;
        // if (!isBuiltIn() && getTL().getNamespace() != null) {
        // try {
        // String versionScheme = getTL().getVersionScheme();
        // VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
        // String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
        // vn = Integer.valueOf( vScheme.getMinorVersion( versionId ) );
        // } catch (NumberFormatException e) {
        // log.debug( "Error converting version string." + e.getCause() );
        // } catch (VersionSchemeException e) {
        // log.debug( "Error determining version. " + e.getCause() );
        // }
        // }
        // return vn;
    }

    /**
     * handles VersionSchemeException
     * 
     * @return the minor version number
     */
    public int getPatchVersion() {
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getPatchLevel( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    public OtmModelManager getModelManager() {
        return mgr;
    }

    /**
     * Get all type users in this library. For each member, add it if a type user and all descendant type users.
     * 
     * @return new list that can be empty
     */
    public List<OtmTypeUser> getUsers() {
        List<OtmTypeUser> users = new ArrayList<>();
        for (OtmLibraryMember member : getMembers()) {
            if (member instanceof OtmTypeUser)
                users.add( (OtmTypeUser) member );
            users.addAll( member.getDescendantsTypeUsers() );
        }
        return users;
    }

    /**
     * Get a users map.
     * <p>
     * The keys are each library that uses types from this library.
     * <p>
     * The values are an array of this library's members that use the key library types.
     * 
     * @see {@link OtmModelMapsManager#getUsersMap(OtmLibrary, boolean)}
     * 
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getUsersMap() {
        // Lazy eval
        if (getModelManager() == null || getModelManager().getMapManager() == null)
            return null;
        if (usersMap == null)
            usersMap = getModelManager().getMapManager().getUsersMap( this, false );
        return usersMap;
    }

    /**
     * Get a provider map.
     * <p>
     * The keys are each provider library -- libraries containing types assigned to type-users in this library.
     * <p>
     * The values are an array of this library's members that use the provided the types. Each value is a member that
     * uses types from the provider library.
     * 
     * @see {@link OtmModelMapsManager#getProvidersMap(OtmLibrary, boolean)}
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getProvidersMap() {
        // lazy evaluation
        if (getModelManager() == null || getModelManager().getMapManager() == null)
            return null;
        if (providerMap == null)
            providerMap = getModelManager().getMapManager().getProvidersMap( this, false );
        return providerMap;
    }


    public OtmProjectManager getProjectManager() {
        return mgr != null ? mgr.getOtmProjectManager() : null;
    }

    /**
     * @return the name from the TL Library or empty string if no Abstract Library.
     */
    public String getName() {
        return getTL() != null ? getTL().getName() : "";
    }

    /**
     * @deprecated use {@link #getChainName()}
     * @return baseNamespace + / + name
     */
    @Deprecated
    public String getNameWithBasenamespace() {
        return getChainName();
        // return getBaseNS() + "/" + getName();
    }

    /**
     * @return baseNamespace + / + name + /v+ major version number
     */
    public String getChainName() {
        return getBaseNS() + "/" + getName() + "/v" + getMajorVersion();
        // return getBaseNS() + "/" + getName();
    }

    /**
     * @return the prefix from the Abstract Library or empty string if no Abstract Library.
     */
    public String getPrefix() {
        return getTL() != null ? getTL().getPrefix() : "";
    }

    /**
     * Get the library name with prefix from the chain.
     * <p>
     * Used in library where used displays.
     * 
     * @return "prefix:name" which could be just ":"
     */
    public String getVersionChainName() {
        // if (getVersionChain() != null && versionChain.size() > 1)
        if (getVersionChain() != null)
            return getVersionChain().getPrefix() + " : " + getName();
        else
            return getPrefix() + " : " + getName();
    }

    /**
     * Simply get the project item for this TLLib from the TL Project Manager
     * 
     * @return PI or null
     */
    public ProjectItem getProjectItem() {
        return getTLProjectManager() != null ? getTLProjectManager().getProjectItem( getTL() ) : null;
    }

    // /**
    // * From the managing TL project, return the item that contains this library.
    // *
    // * @see #getManagingProject()
    // * @return the project item for this library in the managing project
    // */
    // @Deprecated
    // public ProjectItem getProjectItemOLD() {
    // ProjectItem pi = null;
    // OtmProject project = getManagingProject();
    // if (project != null)
    // for (ProjectItem candidate : project.getTL().getProjectItems())
    // if (getProjectItems().contains( candidate ))
    // pi = candidate;
    // return pi;
    // }

    public List<ProjectItem> getProjectItems() {
        return projectItems;
    }

    /**
     * Get the name(s) of the project(s) that contain this library.
     * 
     * @return new array of string containing the project names
     */
    public List<String> getProjectNames() {
        List<String> names = new ArrayList<>();
        getProjectManager().getProjects( getTL() ).forEach( p -> names.add( p.getName() ) );
        names.sort( null );
        return names;
    }

    /**
     * Get a list of all projects this library is in. Uses this library's ProjectItem list to get project names used to
     * get projects from OTM project manager.
     * 
     * @return new list of OtmProjects which may be empty
     */
    public List<OtmProject> getProjects() {
        List<OtmProject> projects = new ArrayList<>();
        if (projectItems != null)
            getProjectNames().forEach( pn -> projects.add( getProjectManager().getProject( pn ) ) );
        return projects;
    }

    /**
     * Get the repository item state. *
     * 
     * @return UNMANAGED unless managed, {@linkplain OtmManagedLibrary#getState()} otherwise
     */
    public abstract RepositoryItemState getState();

    /**
     * @return actual status from the underlying abstract library.
     */
    public abstract TLLibraryStatus getStatus();

    /**
     * 
     * @return the abstract library underlying this facade
     */
    public abstract AbstractLibrary getTL();

    /**
     * @return
     */
    public String getVersion() {
        return getTL().getVersion();
    }

    public boolean hasService() {
        for (OtmLibraryMember m : getMembers())
            if (m instanceof OtmServiceObject)
                return true;
        return false;
    }

    /**
     * @deprecated use instanceof OtmBuiltinLibrary
     * @return
     */
    @Deprecated
    public boolean isBuiltIn() {
        return getTL() instanceof BuiltInLibrary;
    }

    /**
     * Is the library editable based on its Abstract Library and its PI?
     * <p>
     * Note: A library is editable regardless of action manager.
     */
    public abstract boolean isEditable();

    /**
     * True if any library in the chain is editable.
     * <p>
     * Facade for {@link OtmVersionChain#isChainEditable()} False if no version chain.
     * 
     * @return
     */
    public boolean isChainEditable() {
        // if (isEditable())
        // return true;
        return getVersionChain() != null && getVersionChain().isChainEditable();
    }

    /**
     * Version chain for either managed or local library. Local libraries will have empty sub-class.
     * 
     * @return existing or new version chain for this library.
     */
    public OtmVersionChain getVersionChain() {
        return getModelManager().getVersionChain( this );
        // if (this instanceof OtmManagedLibrary)
        // getModelManager().getVersionChain( (OtmManagedLibrary) this );
        // return null;
        // // return null;
        // if (versionChain == null)
        // versionChain = new OtmVersionChain( this );
        // return versionChain;
    }

    /**
     * Ask the model manager if this is the latest version of the library. {@link OtmModelManager#isLatest(OtmLibrary)}
     * 
     * @return
     */
    public abstract boolean isLatestVersion();

    /**
     * @deprecated use instanceof OtmMinorLibrary
     * @return true if minor version number is > 0 and managed in repository
     */
    @Deprecated
    public boolean isMinorVersion() {
        return this instanceof OtmMinorLibrary;
        // if (getPatchVersion() > 0)
        // return false;
        // return (getMinorVersion() > 0 && getState() != RepositoryItemState.UNMANAGED);
    }

    /**
     * @deprecated use instanceof OtmMajorLibrary
     * @return true if minor version number is = 0 and managed in repository
     */
    @Deprecated
    public boolean isMajorVersion() {
        return this instanceof OtmMajorLibrary;
    }

    /**
     * @deprecated use instanceof OtmLocalLibrary
     * @return true if the state equals RepositoryItemStage.UNMANAGED
     */
    @Deprecated
    public boolean isUnmanaged() {
        return this instanceof OtmLocalLibrary;
        // return getState() == RepositoryItemState.UNMANAGED;
    }

    /**
     * Get the validation findings.
     * <p>
     * Lazy evaluated: If they are null, validate the library then return those findings.
     * 
     * @return
     */
    public ValidationFindings getFindings() {
        if (findings == null)
            isValid(); // Runs validation and creates findings
        return findings;
    }

    public boolean isValid() {
        findings = OtmModelElement.isValid( getTL() );
        return !ValidationUtils.hasErrors( findings );
        // return findings == null || findings.isEmpty();
    }

    /**
     * No-operation. (see {@link #delete(OtmLibraryMember)}
     * <p>
     * Note: This is not implemented because OtmLibrary does not maintain children list. Method is here just to make
     * finding the right delete method easier
     * 
     * @param a library member
     */
    public void remove(OtmLibraryMember member) {
        // No-op
    }

    /**
     * Simply remove the item from the list.
     * 
     * @param item
     */
    public void remove(ProjectItem item) {
        projectItems.remove( item );
    }

    /**
     * Run a new instance of the {@link LibraryModelSaver}
     * 
     * @see #save(LibraryModelSaver)
     */
    public String save() {
        return save( new LibraryModelSaver() );
    }

    /**
     * Use the passed saver. Intended for use in saving multiple libraries in one action.
     * 
     * @param lms
     * @return a string with user oriented results message
     */
    public String save(LibraryModelSaver lms) {
        String results = "Error saving library.";
        if (getTL() instanceof TLLibrary) {
            // final ValidationFindings findings = new ValidationFindings();
            try {
                // log.debug("Saving library: " + libraryName + " " + libraryUrl);
                // findings.addAll( lms.saveLibrary( (TLLibrary) getTL() ) );
                lms.saveLibrary( (TLLibrary) getTL() );
                results = "Saved " + this;
            } catch (final LibrarySaveException e) {
                final Throwable t = e.getCause();
                results = "Save error";
                if (t != null && t.getMessage() != null)
                    results += t.getMessage();
            }
        }
        return results;
    }

    public String toString() {
        return getChainName() + " " + getVersion();
    }

    /**
     * Run the validator to create new findings.
     * 
     * @see TLModelCompileValidator#validateModelElement(org.opentravel.schemacompiler.model.TLModelElement, boolean)
     */
    public void validate() {
        findings = TLModelCompileValidator.validateModelElement( getTL(), true );
    }

    /**
     * Get all type providers in this library.
     * 
     * @return new list that can be empty
     */
    public List<OtmTypeProvider> getProviders() {
        List<OtmTypeProvider> providers = new ArrayList<>();
        for (OtmLibraryMember member : getMembers()) {
            if (member instanceof OtmTypeProvider)
                providers.add( (OtmTypeProvider) member );
            providers.addAll( member.getDescendantsTypeProviders() );
        }
        return providers;
    }

    /**
     * Refresh each member
     */
    public void refresh() {
        // log.debug( this.getFullName() + " refreshed" );
        // getMembers().forEach( m -> m.refresh() );
        getMembers().forEach( OtmLibraryMember::refresh );
        refreshMaps();
        refreshVersionChain();
    }

    /**
     * Refresh (null out) just the provider and user maps
     */
    public void refreshMaps() {
        providerMap = null;
        usersMap = null;
        // log.debug( "Maps cleared from " + this );
    }

    /**
     * Refresh (null out) the version chain field
     * 
     * @deprecated - model manager and its chains manager keeps the chains.
     */
    @Deprecated
    public void refreshVersionChain() {
        // log.debug( this.getFullName() + " version chain refreshed" );
        // versionChain = null;
    }

    /**
     * @return prefix : name
     */
    public String getNameWithPrefix() {
        return getPrefix() + " : " + getName();
    }

    /**
     * @return new list of resources or empty list
     */
    public List<OtmResource> getResources() {
        List<OtmResource> resources = new ArrayList<>();
        getMembers().forEach( m -> {
            if (m instanceof OtmResource)
                resources.add( (OtmResource) m );
        } );
        return resources;
    }

    /**
     * Find library member with the passed name.
     * 
     * @param name
     * @return member or null
     */
    public OtmLibraryMember getMember(String name) {
        for (OtmLibraryMember m : getMembers())
            if (m.getName().equals( name ))
                return m;
        return null;
    }
}
