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
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
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
 * @author Dave Hollander
 * 
 */
public class OtmLibrary implements Comparable<OtmLibrary> {
    private static Log log = LogFactory.getLog( OtmLibrary.class );

    protected OtmModelManager mgr;
    protected List<ProjectItem> projectItems = new ArrayList<>();
    protected AbstractLibrary tlLib;
    protected OtmVersionChain versionChain = null;
    protected ValidationFindings findings;
    protected Map<OtmLibrary,List<OtmLibraryMember>> providerMap = null;
    protected Map<OtmLibrary,List<OtmLibraryMember>> usersMap = null;

    /**
     * Should only be called by OtmModelManager. See {@link OtmModelManager#add(AbstractLibrary)}
     * 
     * @param tl
     * @param mgr
     */
    public OtmLibrary(AbstractLibrary tl, OtmModelManager mgr) {
        this.mgr = mgr;
        tlLib = tl;
    }

    protected OtmLibrary(OtmModelManager mgr) {
        this.mgr = mgr;
    }

    public OtmLibrary(ProjectItem pi, OtmModelManager mgr) {
        this.mgr = mgr;
        projectItems.add( pi );
        tlLib = pi.getContent();
    }

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
                if (member.getTlLM().getOwningLibrary() != getTL())
                    log.warn( "Member does not have correct owning library." );
                if (member.getLibrary() != this)
                    log.warn( "Newly added member does not have correct library." );
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
     * @return
     */
    public boolean canBeLocked() {
        if (getStatus() == TLLibraryStatus.DRAFT && getState() == RepositoryItemState.MANAGED_UNLOCKED
            && getManagingProject() != null && getManagingProject().getPermission() != null)
            return getManagingProject().getPermission().equals( RepositoryPermission.WRITE );
        return false;
    }

    public boolean canBeUnlocked() {
        // TODO - check to see if this is the user that locked it
        return getState() == RepositoryItemState.MANAGED_LOCKED || getState() == RepositoryItemState.MANAGED_WIP;
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
        if (otherLibrary == null || this.getBaseNamespace() == null || otherLibrary.getBaseNamespace() == null)
            return false;
        return this.getBaseNamespace().equals( otherLibrary.getBaseNamespace() );
    }

    /**
     * Get Read-only, Minor or Full action manager. To determine which manager to return, consider
     * <ul>
     * <li>library version
     * <li>library status
     * </ul>
     * See also {@link #getActionManager(OtmLibraryMember)}
     * 
     * @return action manager
     */
    public DexActionManager getActionManager() {
        if (isMinorVersion())
            return getModelManager().getMinorActionManager( isEditable() );
        return getModelManager().getActionManager( isEditable() );
    }

    /**
     * Get Read-only, Minor or Full action manager. To determine which manager to return, consider
     * <ul>
     * <li>managed or unmanaged - when unmanaged, use isEditable() to return the action manager from model manager
     * <li>library version
     * <li>library status
     * <li>if the member is new to the chain and editable library
     * <li>if the member is the latest in the version chain.
     * </ul>
     *
     * @return action manager
     */
    public DexActionManager getActionManager(OtmLibraryMember member) {
        if (isUnmanaged())
            return getModelManager().getActionManager( isEditable() );
        if (isMajorVersion() && isEditable())
            return getModelManager().getActionManager( true );
        if (isChainEditable()) {
            if (isEditable() && getVersionChain().isNewToChain( member ))
                return getModelManager().getActionManager( true );
            return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );
        }
        return getModelManager().getActionManager( false );
    }

    /**
     * Get the base namespace from the first project item
     * 
     * @return project item's base namespace or empty string
     */
    public String getBaseNamespace() {
        if (getTL() instanceof TLLibrary)
            return ((TLLibrary) getTL()).getBaseNamespace();
        // Fail-safe: if fails the instance test, try the PI
        return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();
    }

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

    public String getLockedBy() {
        for (ProjectItem pi : projectItems)
            if (pi.getLockedByUser() != null)
                return pi.getLockedByUser();
        return "";
    }

    /**
     * @param namespace
     * @return the major version number
     * @throws VersionSchemeException
     */
    public int getMajorVersion() throws VersionSchemeException {
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getMajorVersion( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    public OtmProject getManagingProject() {
        return mgr.getManagingProject( this );
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
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getMinorVersion( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    /**
     * @return the minor version number
     * @throws VersionSchemeException
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
     * Get all type users in this library.
     * 
     * @return new list that can be empty
     */
    // FIXME - add to junit tests
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

    public String getName() {
        return getTL() != null ? getTL().getName() : "";
    }

    public String getNameWithBasenamespace() {
        return getBaseNamespace() + "/" + getName();
    }

    public String getPrefix() {
        return getTL().getPrefix();
    }

    public String getVersionChainName() {
        if (versionChain != null && versionChain.size() > 1)
            return versionChain.getPrefix() + " : " + getName();
        else
            return getPrefix() + " : " + getName();
    }

    /**
     * @see #getManagingProject()
     * @return the project item for this library in the managing project
     */
    public ProjectItem getProjectItem() {
        ProjectItem pi = null;
        OtmProject project = getManagingProject();
        if (project != null)
            for (ProjectItem candidate : project.getTL().getProjectItems())
                if (getProjectItems().contains( candidate ))
                    pi = candidate;
        return pi;
    }

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

    public List<OtmProject> getProjects() {
        List<OtmProject> projects = new ArrayList<>();
        if (projectItems != null)
            getProjectNames().forEach( pn -> projects.add( getProjectManager().getProject( pn ) ) );
        return projects;
    }

    /**
     * Examine all project items and return the state that grants the user the most rights.
     * 
     * @return
     */
    public RepositoryItemState getState() {
        RepositoryItemState state = RepositoryItemState.MANAGED_UNLOCKED; // the weakest state
        if (projectItems != null) {
            // If not in a project, it must be unmanaged.
            if (projectItems.isEmpty())
                return RepositoryItemState.UNMANAGED;

            for (ProjectItem pi : projectItems) {
                // log.debug("state = " + pi.getState());
                switch (pi.getState()) {
                    case MANAGED_UNLOCKED:
                        break;
                    case BUILT_IN:
                    case UNMANAGED:
                        // These are true regardless of user or user actions
                        return pi.getState();

                    case MANAGED_LOCKED:
                        if (state != RepositoryItemState.MANAGED_WIP)
                            state = pi.getState();
                        break;

                    case MANAGED_WIP:
                        // This gives user most rights and is therefore always used as state
                        return pi.getState();
                }
            }
        }
        return state;
    }

    /**
     * @return actual status of TL Libraries otherwise DRAFT
     */
    public TLLibraryStatus getStatus() {
        if (tlLib instanceof TLLibrary)
            return ((TLLibrary) tlLib).getStatus();
        else
            return TLLibraryStatus.FINAL;
    }

    public AbstractLibrary getTL() {
        return tlLib;
    }

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

    public boolean isBuiltIn() {
        return getTL() instanceof BuiltInLibrary;
    }

    /**
     * A library is editable regardless of action manager.
     * 
     * @return false if TLLibraryStatus is not DRAFT. Else, return true if any associated project item state is
     *         Managed_WIP -OR- unmanaged.
     * 
     * 
     */
    public boolean isEditable() {
        // log.debug( getName() + " State = " + getState().toString() + " Status = " + getStatus() );
        if (getStatus() != TLLibraryStatus.DRAFT)
            return false;
        return getState() == RepositoryItemState.MANAGED_WIP || getState() == RepositoryItemState.UNMANAGED;
    }

    /**
     * Are any of the libraries in the version chain (same major version number) editable? Always true if this library
     * is editable.
     * 
     * @return
     */
    public boolean isChainEditable() {
        if (isEditable())
            return true;
        return getVersionChain().isChainEditable();
    }

    // FIXME - clear version chain when new library added
    /**
     * @return existing or new version chain for this library
     */
    public OtmVersionChain getVersionChain() {
        if (versionChain == null)
            versionChain = new OtmVersionChain( this );
        return versionChain;
    }

    /**
     * Ask the model manager if this is the latest version of the library {@link OtmModelManager#isLatest(OtmLibrary)}
     * 
     * @return
     */
    public boolean isLatestVersion() {
        return mgr.isLatest( this );
    }

    /**
     * @return true if minor version number is > 0 and managed in repository
     */
    public boolean isMinorVersion() {
        if (getPatchVersion() > 0)
            return false;
        return (getMinorVersion() > 0 && getState() != RepositoryItemState.UNMANAGED);
    }

    /**
     * @return true if minor version number is = 0 and managed in repository
     */
    public boolean isMajorVersion() {
        if (getPatchVersion() > 0)
            return false;
        return (getMinorVersion() == 0 && getState() != RepositoryItemState.UNMANAGED);
    }

    /**
     * @return
     */
    public boolean isUnmanaged() {
        return getState() == RepositoryItemState.UNMANAGED;
    }

    public ValidationFindings getFindings() {
        if (findings == null)
            isValid();
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

    public void remove(ProjectItem item) {
        projectItems.remove( item );
    }

    /**
     * 
     */
    public String save() {
        return save( new LibraryModelSaver() );
    }

    /**
     * Use the passed saver. Intended for use in saving multiple libraries in one action.
     * 
     * @param lms
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
        return getNameWithBasenamespace() + " " + getVersion();
    }

    public void validate() {
        findings = TLModelCompileValidator.validateModelElement( getTL(), true );
    }

    /**
     * Get all type providers in this library.
     * 
     * @return new list that can be empty
     */
    // FIXME - add to junit tests
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
     * Refresh (null out) just the version chain field
     */
    public void refreshVersionChain() {
        // log.debug( this.getFullName() + " version chain refreshed" );
        versionChain = null;
    }

    /**
     * @return
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
     * @param name
     * @return
     */
    public OtmLibraryMember getMember(String name) {
        for (OtmLibraryMember m : getMembers())
            if (m.getName().equals( name ))
                return m;
        return null;
    }
}
