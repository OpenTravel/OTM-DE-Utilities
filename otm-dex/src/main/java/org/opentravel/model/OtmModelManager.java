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

package org.opentravel.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexFileHandler;
import org.opentravel.common.DexLibraryException;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.library.LibraryRowFactory;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.model.TypeResolverTask;
import org.opentravel.dex.tasks.model.ValidateModelManagerItemsTask;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.model.otmContainers.OtmBuiltInLibrary;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLibraryFactory;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberFactory;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.concurrent.WorkerStateEvent;

/**
 * Manage access to all OTM Named objects.
 * 
 * @author dmh
 *
 */
public class OtmModelManager implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( OtmModelManager.class );

    public static final String OTA_EMPTY_NAME = "Empty";
    public static final String XSD_ID_NAME = "ID";
    public static final String XSD_DECIMAL_NAME = "decimal";
    public static final String XSD_INTEGER_NAME = "integer";
    public static final String XSD_STRING_NAME = "string";

    // Internal Managers
    private OtmProjectManager otmProjectManager;
    private OtmModelMapsManager otmMapManager;
    private OtmModelChainsManager chainsManager = null;
    private OtmModelNamespaceManager nsManager = null;
    private OtmModelMembersManager membersManager = null;

    // Open libraries - Abstract Libraries are built-in and user
    private Map<AbstractLibrary,OtmLibrary> libraries = new HashMap<>();
    // Domains - one entry per unique base namespace
    private List<OtmDomain> domains = new ArrayList<>();

    private DexActionManager readOnlyActionManager = new DexReadOnlyActionManager();
    private DexActionManager minorActionManager;
    private DexActionManager fullActionManager;
    private DexStatusController statusController;
    private UserSettings userSettings;
    private TLModel tlModel = null;

    private int backgroundTaskCount = 0;

    /**
     * Create a model manager.
     * 
     * @param fullActionManager edit-enabled action manager to assign to all members. If null, read-only action manager
     *        will be used.
     * @param controller
     * @param repositoryManager
     * @param userSettings used by object builders default values, can be null
     */
    public OtmModelManager(DexActionManager fullActionManager, RepositoryManager repositoryManager,
        UserSettings userSettings) {
        this.userSettings = userSettings;

        // Create a TL Model to manage and tell it to track changes to maintain its type integrity
        try {
            tlModel = new TLModel();
            tlModel.addListener( new ModelIntegrityChecker() );
        } catch (Exception e) {
            log.info( "Exception creating new model: " + e.getLocalizedMessage() );
        }

        // Create a compilier's project manager
        ProjectManager tlMgr = null;
        if (repositoryManager != null)
            tlMgr = new ProjectManager( tlModel, true, repositoryManager );
        else
            tlMgr = new ProjectManager( tlModel );
        otmProjectManager = new OtmProjectManager( this, tlMgr );

        // Action managers - Main controller will pass one if enabled by settings
        if (fullActionManager == null) {
            this.fullActionManager = new DexReadOnlyActionManager();
            minorActionManager = new DexReadOnlyActionManager();
        } else {
            this.fullActionManager = fullActionManager;
            minorActionManager = new DexMinorVersionActionManager( fullActionManager );
        }
        // Set main controller for warnings if one is provided.
        readOnlyActionManager.setMainController( this.fullActionManager.getMainController() );
        minorActionManager.setMainController( this.fullActionManager.getMainController() );

        // Initialize internal managers
        chainsManager = new OtmModelChainsManager();
        otmMapManager = new OtmModelMapsManager( this );
        nsManager = new OtmModelNamespaceManager( this );
        membersManager = new OtmModelMembersManager( this );

        // Bring in the built-in libraries. Do last - relies on managers
        addLibraries_BuiltIn( tlModel );

        // log.debug( "Model Manager constructor complete." );
    }

    // Domains are added when libraries are added.
    private OtmDomain add(OtmDomain domain) {
        if (!domains.contains( domain )) {
            domains.add( domain );
            // log.debug( "Added " + domain + " to domain list." );
        }
        return domain;
    }

    /**
     * Simply add the member to the members maps if it is not already in the map.
     * <p>
     * See {@link OtmLibrary#add(OtmLibraryMember)} to add to both TL library and manager.
     * <p>
     * Facade for {@linkplain OtmModelMembersManager#add(OtmLibraryMember)}
     * 
     * @param member
     */
    public void add(OtmLibraryMember member) {
        membersManager.add( member );
    }

    private void addDomain(AbstractLibrary absLibrary) {
        String dn = null;
        if (absLibrary instanceof TLLibrary)
            dn = ((TLLibrary) absLibrary).getBaseNamespace();
        if (dn != null && !dn.isEmpty() && getDomain( dn ) == null) {
            add( new OtmDomain( dn, this ) );
        }
    }

    /**
     * Add all user defined libraries in the TLModel. Will skip those already loaded. When finished, start validation
     * and resolve tasks.
     * <p>
     * The library loader may open more than the selected file to handle includes and imports. This generic add method
     * is safest because it checks all the libraries in the TL Model and add those that have not already been added.
     * <p>
     * Used by {@link DexFileHandler#openLibrary(java.io.File, OtmModelManager)}
     * <p>
     * <b>Note:</b> because these are loaded without a project, they are unmanaged, local libraries.
     * 
     * @see LibraryModelLoader#loadLibraryModel(org.opentravel.schemacompiler.loader.LibraryInputSource)
     */
    public void addLibraries() {
        getTlModel().getUserDefinedLibraries().forEach( this::addLibrary );
        startValidatingAndResolvingTasks();
        // log.debug( "Added Libraries. Model has " + members.size() + " members." );
    }

    /**
     * Add the built in libraries to the libraries and member maps
     */
    protected void addLibraries_BuiltIn(TLModel tlModel) {
        OtmLibrary otmLib;
        for (BuiltInLibrary builtInLib : tlModel.getBuiltInLibraries()) {
            try {
                otmLib = OtmLibraryFactory.newLibrary( builtInLib, this );
                putLibrary( builtInLib, otmLib );
            } catch (DexLibraryException e) {
                log.warn( "Exception: " + e.getLocalizedMessage() );
            }

            // Consider moving this code to OtmBuiltInLibrary constructor or method
            for (LibraryMember tlMember : builtInLib.getNamedMembers()) {
                // If it has listeners, remove them
                if (!tlMember.getListeners().isEmpty()) {
                    ArrayList<ModelElementListener> listeners = new ArrayList<>( tlMember.getListeners() );
                    listeners.forEach( l -> tlMember.removeListener( l ) );
                }
                OtmLibraryMemberFactory.create( tlMember, this ); // creates and adds
            }
        }
    }

    /**
     * Add an {@link AbstractLibrary} (TL library) to the model. If the library was already added, that library is
     * returned. Otherwise, if the TLModel's ProjectManager has a ProjectItem associated with the library, its content
     * is used. If not, a local library is created.
     * <p>
     * Creates OTM library, which is added to the maps and models its content.
     * 
     * @param absLibrary
     * @return newly created OTM library or one from the map if it already existed or null.
     */
    public OtmLibrary addLibrary(AbstractLibrary absLibrary) {
        if (absLibrary == null)
            return null;

        // If already added, just return the OtmLibrary facade.
        if (contains( absLibrary ))
            return get( absLibrary );

        // New to model: Create Otm library, add to maps and model contents
        OtmLibrary otmLibrary = null;
        try {
            otmLibrary = OtmLibraryFactory.newLibrary( absLibrary, this );
            addToMaps( otmLibrary ); // Add library to maps
            OtmLibraryFactory.modelMembers( absLibrary, this );
        } catch (DexLibraryException e) {
            log.error( "Library Factory exception: " + e.getLocalizedMessage() );
            otmLibrary = null;
        }

        return otmLibrary;
    }

    /**
     * Add this TL-ProjectItem's managed TL library to the model.
     * <p>
     * If it is new to the model, a new {@link OtmLibrary} is created and modeled using the {@link OtmLibraryFactory}
     * and added to internal data structures.
     * <p>
     * If it was already added, the PI will be added to the library.
     * 
     * @param project item
     * @return local, major or minor library
     */
    public OtmLibrary addLibrary(ProjectItem pi) {
        // OtmLibrary mLib = null;
        if (pi == null || !(pi.getContent() instanceof TLLibrary))
            return null;
        OtmLibrary newlib = null;
        AbstractLibrary absLibrary = pi.getContent();

        // Add new content or return existing OtmManagedLibrary
        if (contains( absLibrary )) {
            newlib = get( absLibrary );
        } else {
            // Create new library
            try {
                newlib = OtmLibraryFactory.newLibrary( pi, this );
                addToMaps( newlib );
                OtmLibraryFactory.modelMembers( pi.getContent(), this );
            } catch (DexLibraryException e) {
                log.warn( "Library Factory exception: " + e.getLocalizedMessage() );
                return null;
            }
        }
        // log.debug( "Adding project item: " + absLibrary.getName() + " in " + absLibrary.getNamespace() );
        return newlib;
    }

    /**
     * Get all the projects from the project manager. Create libraries for all project items if they have not already be
     * modeled. Start validation and type resolution task.
     * <p>
     * Used by
     * {@link DexFileHandler#openProject(java.io.File, OtmModelManager, org.opentravel.common.OpenProjectProgressMonitor)}
     */
    public void addProjects() {
        // Add projects to project map
        for (Project project : getProjectManager().getAllProjects())
            otmProjectManager.add( project );

        // Get the built in libraries, will do nothing if already added
        addLibraries_BuiltIn( getTlModel() );

        // Get Libraries - Libraries can belong to multiple projects. Will ignore if already added.
        for (ProjectItem pi : getProjectManager().getAllProjectItems()) {
            addLibrary( pi );
        }

        startValidatingAndResolvingTasks();
        // log.debug( "Model has " + members.size() + " members." );
    }

    /**
     * Add this library to the maps. Should only be called by the OtmLibraryFactory.
     * <P>
     * OtmProject also uses it.
     * 
     * @param lib
     */
    private void addToMaps(OtmLibrary lib) {
        AbstractLibrary absLibrary = lib.getTL();
        putLibrary( absLibrary, lib );
        addDomain( absLibrary );
        nsManager.add( lib );
        chainsManager.add( lib );
    }

    /**
     * Used by the OtmLibraryFactory when converting local to major library.
     * <li>{@link OtmProjectManager#publish(OtmLocalLibrary, Repository)
     * <li>{@link ManageLibraryTask}
     * <li>{@link LibraryRowFactory}
     * 
     * @param lib
     * @param newLib
     */
    public void changeToManaged(OtmLocalLibrary oldLib, OtmMajorLibrary newLib) {
        removeFromMaps( oldLib );
        addToMaps( newLib );
    }

    /**
     * Clear the model. Clears the model manager's data, the TL Model, and Project Manager.
     */
    public void clear() {
        nsManager.clear();
        chainsManager.clear();
        membersManager.clear();
        libraries.clear();
        domains.clear();
        getTlModel().clearModel();
        if (otmProjectManager != null)
            otmProjectManager.clear();
        addLibraries_BuiltIn( getTlModel() );
        // log.debug( "Cleared model. " + tlModel.getAllLibraries().size() );
    }

    /**
     * Is this library in the libraries map?
     * 
     * @param absLibrary
     * @return
     */
    public boolean contains(AbstractLibrary absLibrary) {
        return absLibrary != null && libraries.containsKey( absLibrary );
    }

    /**
     * @return true if the TL Library Member exists as a key in the members map.
     */
    public boolean contains(LibraryMember tlMember) {
        return membersManager.contains( tlMember );
    }

    /**
     * NOTE - this is slow compared with {@link #contains(tlMember)}
     * 
     * @return true if the member exists as a value in the members map.
     */
    public boolean contains(OtmLibraryMember member) {
        return membersManager.contains( member );
    }

    /**
     * Examine all members. Return list of members that use the passed member as a base type.
     * 
     * @param member
     * @return
     */
    public List<OtmLibraryMember> findSubtypesOf(OtmLibraryMember member) {
        return membersManager.findSubtypesOf( member );
    }

    /**
     * Examine all members. Return list of owners that have a of a descendant type user that is assigned to provider.
     * <p>
     * Facade for {@linkplain OtmModelMembersManager#findUsersOf(OtmTypeProvider)}
     * 
     * @param provider
     * @return
     */
    public List<OtmLibraryMember> findUsersOf(OtmTypeProvider provider) {
        return membersManager.findUsersOf( provider );
    }

    /**
     * Simply get the matching OTM library directly from the map.
     * 
     * @param absLibrary TL Abstract Library
     * @return OtmLibrary associated with the abstract library
     */
    public OtmLibrary get(AbstractLibrary absLibrary) {
        return libraries.get( absLibrary );
    }

    /**
     * @return the abstract library associated with the otm library parameter.
     */
    public AbstractLibrary get(OtmLibrary library) {
        for (Entry<AbstractLibrary,OtmLibrary> set : libraries.entrySet())
            if (set.getValue() == library)
                return set.getKey();
        return null;
    }

    public OtmLibrary get(String fullName) {
        for (OtmLibrary lib : libraries.values()) {
            // log.debug("testing: " + fullName + " against " + lib.getFullName());
            if (lib.getFullName().equals( fullName ))
                return lib;
        }
        return null;
    }

    /**
     * @param TL Library
     * @return OtmLibrary associated with the TL Library
     */
    public OtmLibrary get(TLLibrary tlLibrary) {
        if (!libraries.containsKey( tlLibrary ))
            log.warn( "Missing library associated with: " + tlLibrary.getName() );
        return libraries.get( tlLibrary );
    }

    /**
     * Return an action manager. Intended only for use by libraries.
     * 
     * @param full if false, only return readOnly action manager
     * @return read-only or full action manager
     */
    public DexActionManager getActionManager(boolean full) {
        return full ? fullActionManager : readOnlyActionManager;
    }

    /**
     * Exposed only for testing.
     * 
     * @return
     */
    public int getBackgroundTaskCount() {
        return backgroundTaskCount;
    }

    /**
     * @return unmodifiableList of strings for both managed and unmanaged base namespaces.
     */
    public List<String> getBaseNamespaces() {
        return nsManager.getBaseNamespaces();
    }

    public List<OtmLibrary> getBaseNSLibraries(String baseNS) {
        return nsManager.getBaseNsLibraries( baseNS );
    }

    public OtmBuiltInLibrary getBuiltInLibrary() {
        OtmBuiltInLibrary biLib = null;
        for (OtmLibrary lib : getLibraries())
            if (lib.getTL() instanceof BuiltInLibrary
                && lib.getTL().getNamespace().equals( OtmModelNamespaceManager.OTA_LIBRARY_NAMESPACE ))
                return (OtmBuiltInLibrary) lib;
        return biLib;
    }

    public List<OtmLibrary> getChainLibraries(OtmLibrary lib) {
        return getChainLibraries( lib.getNameWithBasenamespace() );
    }

    /**
     * Get list of libraries in the chain. Facade for {@linkplain OtmModelChainsManager#getChainLibraries(String)}
     * 
     * @param chainName
     * @return
     */
    public List<OtmLibrary> getChainLibraries(String chainName) {
        return chainsManager.getChainLibraries( chainName );
    }

    /**
     * return unmodifiable list of string for each chain name.
     */
    public Set<String> getChainNames() {
        return chainsManager.getChainNames();
    }

    /**
     * Facade for {@linkplain OtmModelChainsManager#getChains()}
     * 
     * @return unmodifiable collection of all chains
     */
    public Collection<OtmVersionChain> getChains() {
        return chainsManager.getChains();
    }

    // Exposed for testing
    protected OtmModelChainsManager getChainsManager() {
        return chainsManager;
    }

    public OtmDomain getDomain(String baseNamespace) {
        for (OtmDomain d : domains)
            if (d.getBaseNamespace().equals( baseNamespace ))
                return d;
        return null;
    }

    /**
     * @return Live list of all domains (baseNamespaces) in the model.
     */
    public List<OtmDomain> getDomains() {
        return domains;
    }

    /**
     * @return new list of editable libraries, may be empty
     */
    public List<OtmLibrary> getEditableLibraries() {
        List<OtmLibrary> libs = new ArrayList<>();
        libraries.values().forEach( l -> {
            if (l.isEditable())
                libs.add( l );
        } );
        return libs;
    }

    /**
     * @return the ota 2.0 Empty simple type
     */
    public OtmXsdSimple getEmptyType() {
        return membersManager.getXsdMember( OTA_EMPTY_NAME, getBuiltInLibrary() );
    }

    /**
     * Try to find the XSD ID type and return it
     * 
     * @return
     */
    public OtmXsdSimple getIdType() {
        return getXsdMember( XSD_ID_NAME );
    }

    /**
     * Return a library member with the same name that is in the latest version of the libraries with the same base
     * namespace
     * 
     * @param member
     * @return
     */
    public OtmLibraryMember getLatestMember(OtmLibraryMember member) {
        return membersManager.getLatestMember( member );
    }


    public Collection<OtmLibrary> getLibraries() {
        return Collections.unmodifiableCollection( libraries.values() );
    }

    /**
     * Get all libraries in the base namespace.
     * <p>
     * Note, some of these may be in a chain, see {@link OtmLibrary#getVersionChain()}
     * 
     * @param baseNamespace
     * @return new list of libraries
     */
    public List<OtmLibrary> getLibraries(String baseNamespace) {
        List<OtmLibrary> libList = new ArrayList<>();
        getLibraries().forEach( l -> {
            if (l.getBaseNS().equals( baseNamespace ))
                libList.add( l );
        } );
        return libList;
    }

    public OtmModelMapsManager getMapManager() {
        return otmMapManager;
    }

    /**
     * Get the member with matching prefix and name
     * 
     * @param nameWithPrefix formatted as prefix + ":" + name
     * @return member if found or null
     */
    public OtmLibraryMember getMember(String nameWithPrefix) {
        return membersManager.getMember( nameWithPrefix );
    }

    public OtmLibraryMember getMember(TLModelElement tlMember) {
        return membersManager.getMember( tlMember );
    }

    /**
     * Synchronized access to members.values()
     * 
     * @return all the library members being managed in a unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers() {
        return membersManager.getMembers();
    }

    /**
     * Notes: using the commented out sync'ed code causes TestInheritance#testInheritedCustomFacets() to time out.
     * getMembers() uses the synchronized member list.
     * <p>
     * 
     * @param filter DexFilter to use to select members. If null, all members are selected.
     * @return all the filter selected library members in an unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers(DexFilter<OtmLibraryMember> filter) {
        return membersManager.getMembers( filter );
    }

    /**
     * @return new list with all the library members in that library
     */
    public List<OtmLibraryMember> getMembers(OtmLibrary library) {
        return membersManager.getMembers( library );
    }

    /**
     * @param name
     * @return list of members with matching names
     */
    public List<OtmLibraryMember> getMembers(OtmLibraryMember m) {
        return membersManager.getMembers( m );
    }

    /**
     * 
     * @return new collection of all contextual facets in the model.
     */
    public Collection<OtmLibraryMember> getMembersContextualFacets() {
        return membersManager.getMembersContextualFacets();
    }


    /**
     * 
     * @param minor
     * @return minor action manager if minor == true
     */
    public DexActionManager getMinorActionManager(boolean minor) {
        return minor ? minorActionManager : readOnlyActionManager;
    }

    /**
     * Exposed for testing only.
     * 
     * @return
     */
    public OtmModelMembersManager getOtmMembersManager() {
        return membersManager;
    }

    /**
     * @return the OTM Project Manager
     */
    public OtmProjectManager getOtmProjectManager() {
        return otmProjectManager;
    }

    /**
     * Facade for TL {@link ProjectManager#getProjectItem(AbstractLibrary)}
     * 
     * @return PI if library is TLLibrary (not built-in or XSD) and project manager defined
     */
    public ProjectItem getProjectItem(AbstractLibrary absLib) {
        return absLib instanceof TLLibrary && getProjectManager() != null ? getProjectManager().getProjectItem( absLib )
            : null;
    }

    /**
     * Facade for {@link OtmProjectManager#getTLProjectManager()}
     */
    public ProjectManager getProjectManager() {
        return otmProjectManager.getTLProjectManager();
    }

    /**
     * Facade for {@link OtmProjectManager#getUserProjects()
     */
    public List<OtmProject> getProjects() {
        return otmProjectManager.getUserProjects();
    }

    /**
     * @param sort if true, sort the list by member name
     * @return new list of members that are resources
     */
    // FUTURE - make facade and move logic to member manager.
    public List<OtmResource> getResources(boolean sort) {
        List<OtmResource> resources = new ArrayList<>();
        getMembers().forEach( m -> {
            if (m instanceof OtmResource)
                resources.add( (OtmResource) m );
        } );
        if (sort)
            resources.sort( (one, other) -> one.getName().compareTo( other.getName() ) );
        return resources;
    }

    // FUTURE - make facade and move logic to member manager.
    public List<OtmResource> getResources(DexFilter<OtmLibraryMember> filter, boolean sort) {
        if (filter == null)
            return getResources( sort );
        List<OtmResource> resources = new ArrayList<>();
        getMembers().forEach( m -> {
            if (m instanceof OtmResource && filter.isSelected( m ))
                resources.add( (OtmResource) m );
        } );
        if (sort)
            resources.sort( (one, other) -> one.getName().compareTo( other.getName() ) );
        return resources;
    }

    /**
     * Try to find the XSD String type and return it
     * 
     * @return
     */
    public OtmXsdSimple getStringType() {
        return getXsdMember( XSD_STRING_NAME );
    }

    /**
     * Get the TL Model used by this model manager.
     * 
     * @return
     */
    public TLModel getTlModel() {
        return tlModel;
    }

    /**
     * @return new list with just user libraries, not built-in
     */
    public List<OtmLibrary> getUserLibraries() {
        List<OtmLibrary> libList = new ArrayList<>();
        libraries.values().forEach( lib -> {
            if (lib.getTL() instanceof TLLibrary)
                libList.add( lib );
        } );
        return libList;
    }

    /**
     * 
     * @return user settings or null
     */
    public UserSettings getUserSettings() {
        return userSettings;
    }

    /**
     * Simply get the version chain from the chains manager.
     * 
     * @param lib
     * @return version chain if any or null
     */
    public OtmVersionChain getVersionChain(OtmLibrary lib) {
        return chainsManager.get( lib );
    }

    public OtmVersionChain getVersionChain(String chainName) {
        return chainsManager.get( chainName );
    }

    public OtmLibrary getXsdLibrary() {
        for (OtmLibrary lib : getLibraries()) {
            if (lib.getTL() instanceof BuiltInLibrary
                && lib.getTL().getNamespace().equals( OtmModelNamespaceManager.XSD_LIBRARY_NAMESPACE ))
                return lib;
        }
        return null;
    }

    public OtmXsdSimple getXsdMember(String name) {
        return membersManager.getXsdMember( name, getXsdLibrary() );
    }

    public void handleEvent(DexChangeEvent e) {
        // WARNING - This runs often so keep this light weight or put into a background task.
        // Something has happened, let the libraries know
        getLibraries().forEach( OtmLibrary::refreshMaps );
    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        // NO-OP
        // if (event != null && event.getTarget() != null) {
        // if (event.getTarget() instanceof TypeResolverTask)
        // log.debug( "Type Resolver Task complete" );
        // else if (event.getTarget() instanceof ValidateModelManagerItemsTask)
        // log.debug( "Validation Task complete" );
        // } else
        // log.debug( "Task complete" );
        // if (backgroundTaskCount == 1) {
        // // model change event
        // }
        backgroundTaskCount--;
    }

    /**
     * @return true if any library is editable
     */
    public boolean hasEditableLibraries() {
        for (OtmLibrary l : libraries.values())
            if (l.isEditable())
                return true;
        return false;
    }


    /**
     * @param library which can be null
     * @return true if any library except the passed library is editable
     */
    public boolean hasEditableLibraries(OtmLibrary library) {
        for (OtmLibrary l : libraries.values())
            if (l != library && l.isEditable())
                return true;
        return false;
    }


    /**
     * Look into the chain and return true if this is the latest version (next version = null)
     * <p>
     * True if not in a chain.
     * 
     * @param lib
     * @return
     */
    public boolean isLatest(OtmLibrary lib) {
        if (chainsManager.get( lib ) != null) {
            return chainsManager.get( lib ).isLatest( lib );
        }
        log.warn( "Chain manager did not find a chain for " + lib );
        return true;
    }

    public void printLibraries() {
        libraries.entrySet().forEach( l -> log.debug( l.getValue().getName() ) );
    }

    /**
     * Simply put the pair into the libraries map.
     */
    private void putLibrary(AbstractLibrary alib, OtmLibrary otmLib) {
        if (alib != null && otmLib != null)
            libraries.put( alib, otmLib );
    }

    /**
     * Simply remove the member from the members map. To delete a member use {@link OtmLibrary#delete(OtmLibraryMember)}
     * <p>
     * Facade for {@linkplain OtmModelMembersManager#remove(OtmLibraryMember)}
     * 
     * @param member
     */
    public void remove(OtmLibraryMember member) {
        membersManager.remove( member );
    }

    // Exposed for testing
    protected void removeFromMaps(OtmLibrary lib) {
        AbstractLibrary absLibrary = lib.getTL();
        libraries.remove( absLibrary );
        chainsManager.remove( lib );
        nsManager.remove( lib );
        // TODO - what to do here??? addDomain( absLibrary );
    }


    public void setStatusController(DexStatusController statusController) {
        this.statusController = statusController;
    }

    /**
     * Start the validation and type resolver tasks. Use this model manager to handle results and its status controller.
     */
    public void startValidatingAndResolvingTasks() {
        // Start a background task to validate the objects
        new ValidateModelManagerItemsTask( this, this, statusController ).go();
        // Start a background task to resolve type relationships
        new TypeResolverTask( this, this, statusController ).go();
        backgroundTaskCount = 2;
    }

    /**
     * Exposed for testing only. Used only in testing when the default repository location is mocked after the model
     * manager and its project manager are created.
     * 
     * @param repositoryManager
     */
    public void updateProjectManager(RepositoryManager repositoryManager) {
        ProjectManager tlMgr = null;
        if (repositoryManager != null)
            tlMgr = new ProjectManager( tlModel, true, repositoryManager );
        else
            tlMgr = new ProjectManager( tlModel );
        otmProjectManager = new OtmProjectManager( this, tlMgr );

        // log.debug( "OtmProjMgr = " + otmProjectManager.hashCode() + " ProjectManager = " + tlMgr.hashCode()
        // + " repositoryManager = " + repositoryManager.hashCode() );
    }
}
