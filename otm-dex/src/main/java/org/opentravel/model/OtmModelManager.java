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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.action.manager.DexReadOnlyActionManager;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.model.TypeResolverTask;
import org.opentravel.dex.tasks.model.ValidateModelManagerItemsTask;
import org.opentravel.model.otmContainers.OtmBuiltInLibrary;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberFactory;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
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
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;

/**
 * Manage access to all OTM Named objects.
 * 
 * @author dmh
 *
 */
public class OtmModelManager implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( OtmModelManager.class );


    // Project Facade
    private OtmProjectManager otmProjectManager;
    private OtmModelMapsManager otmMapManager;

    // Map of base namespaces with all managed libraries in that namespace
    private Map<String,VersionChain<TLLibrary>> baseNSManaged = new HashMap<>();
    private Map<String,OtmLibrary> baseNSUnmanaged = new HashMap<>();

    // Open libraries - Abstract Libraries are built-in and user
    private Map<AbstractLibrary,OtmLibrary> libraries = new HashMap<>();

    // All members - Library Members are TLLibraryMembers and contextual facets
    private Map<LibraryMember,OtmLibraryMember> members = new HashMap<>();

    // Domains - one entry per unique base namespace
    private List<OtmDomain> domains = new ArrayList<>();

    private DexActionManager readOnlyActionManager = new DexReadOnlyActionManager();
    private DexActionManager minorActionManager;
    private DexActionManager fullActionManager;

    private DexStatusController statusController;
    // private RepositoryManager repositoryManager;
    private TLModel tlModel = null;

    public static final String OTA_LIBRARY_NAMESPACE = "http://www.opentravel.org/OTM/Common/v0";
    public static final String OTA_EMPTY_NAME = "Empty";

    private DialogBoxContoller dialogBox = null;
    private boolean showingError = false;

    private UserSettings userSettings;



    /**
     * Create a model manager.
     * 
     * @param fullActionManager edit-enabled action manager to assign to all members. If null, read-only action manager
     *        will be used.
     * @param controller
     * @param userSettings used by object builders default values
     */
    public OtmModelManager(DexActionManager fullActionManager, RepositoryManager repositoryManager,
        UserSettings userSettings) {
        this( fullActionManager, repositoryManager );
        this.userSettings = userSettings;
    }

    /**
     * @deprecated - pass in null user settings if they are not known.
     * @param fullActionManager
     * @param repositoryManager
     */
    public OtmModelManager(DexActionManager fullActionManager, RepositoryManager repositoryManager) {

        // Create a TL Model to manage
        //
        try {
            tlModel = new TLModel();
        } catch (Exception e) {
            log.info( "Exception creating new model: " + e.getLocalizedMessage() );
        }
        // Tell model to track changes to maintain its type integrity
        tlModel.addListener( new ModelIntegrityChecker() );
        // Bring in the built-in libraries
        addBuiltInLibraries( tlModel );

        // Create a master project manager
        //
        ProjectManager tlMgr = null;
        if (repositoryManager != null)
            tlMgr = new ProjectManager( tlModel, true, repositoryManager );
        else
            tlMgr = new ProjectManager( tlModel );
        otmProjectManager = new OtmProjectManager( this, tlMgr );
        // projectManager = tlMgr;

        // Action managers
        // Main controller will pass one if enabled by settings
        //
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

        otmMapManager = new OtmModelMapsManager( this );
        // log.debug( "Model Manager constructor complete." );
    }

    public OtmModelMapsManager getMapManager() {
        return otmMapManager;
    }

    /**
     * Simply add the member to the maps if it is not already in the map. See {@link OtmLibrary#add(OtmLibraryMember)}
     * to add to both TL library and manager.
     * 
     * @param member
     */
    public void add(OtmLibraryMember member) {
        if (member != null && member.getTL() instanceof LibraryMember && !contains( member.getTlLM() ))
            members.put( member.getTlLM(), member );
    }

    /**
     * Simply remove the member from the map. To delete a member use {@link OtmLibrary#delete(OtmLibraryMember)}
     * 
     * @param member
     */
    public void remove(OtmLibraryMember member) {
        if (member != null && member.getTL() instanceof LibraryMember && contains( member.getTlLM() ))
            members.remove( member.getTlLM(), member );
    }

    /**
     * Add the TL library to the model if it is not already in the model. Adds all the members.
     * <p>
     * Does <b>not</b> resolve types. Does <b>not</b> validate the objects.
     * <p>
     * Create OtmLibrary facade.
     * 
     * @see #startValidatingAndResolvingTasks()
     * 
     * @param library to add
     * @return newly created OtmLibrary
     */
    public OtmLibrary add(AbstractLibrary absLibrary) {
        return add( absLibrary, getVersionChainFactory() );
    }

    public OtmLibrary addUnmanaged(AbstractLibrary absLibrary) {
        return add( absLibrary, null );
    }

    /**
     * Check all the libraries in the TL Model and add those that have not already been added.
     */
    public void add() {
        // Add will prevent duplicate entries
        getTlModel().getUserDefinedLibraries().forEach( tlLib -> add( tlLib ) );
    }

    // Fixme - should be ok. fixed OtmLibrary to return baseNamespace without PI.
    // Todo - refactor add(*) to simplify
    // Not used on open project task
    protected OtmLibrary add(AbstractLibrary absLibrary, VersionChainFactory versionChainFactory) {
        if (absLibrary == null)
            return null;
        if (contains( absLibrary ))
            return libraries.get( absLibrary );

        OtmLibrary otmLibrary = new OtmLibrary( absLibrary, this );

        libraries.put( absLibrary, otmLibrary );
        addDomain( absLibrary );

        // Map of base namespaces with all libraries in that namespace
        if (absLibrary instanceof TLLibrary)
            if (versionChainFactory != null) {
                baseNSManaged.put( otmLibrary.getNameWithBasenamespace(),
                    versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
                // log.debug( "Added " + otmLibrary.getNameWithBasenamespace() + otmLibrary.getVersion()
                // + " to base NS managed." );
            } else {
                baseNSUnmanaged.put( otmLibrary.getNameWithBasenamespace(), otmLibrary );
                // log.debug( "Added " + otmLibrary.getNameWithBasenamespace() + otmLibrary.getVersion()
                // + " to base NS UN-managed." );
            }

        // For each named member use the factory to create and add OtmObject
        absLibrary.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, this ) );

        return otmLibrary;
    }

    // 11/26/2020 - the baseNamespace maps are "funky"
    // Start using Domains and assure they are junit tested.
    private void addDomain(AbstractLibrary absLibrary) {
        String dn = null;
        if (absLibrary instanceof TLLibrary)
            dn = ((TLLibrary) absLibrary).getBaseNamespace();
        if (dn != null && !dn.isEmpty() && getDomain( dn ) == null) {
            add( new OtmDomain( dn, this ) );
        }
    }

    // Domains are added when libraries are added.
    private OtmDomain add(OtmDomain domain) {
        if (!domains.contains( domain )) {
            domains.add( domain );
            // log.debug( "Added " + domain + " to domain list." );
        }
        return domain;
    }

    public OtmDomain getDomain(String baseNamespace) {
        for (OtmDomain d : domains)
            if (d.getBaseNamespace().equals( baseNamespace ))
                return d;
        return null;
    }

    protected OtmLibrary add(ProjectItem pi, VersionChainFactory versionChainFactory) {
        if (pi == null)
            return null;

        AbstractLibrary absLibrary = pi.getContent();
        if (absLibrary == null)
            return null;
        if (contains( absLibrary ))
            return libraries.get( absLibrary );

        // Create new library
        OtmLibrary otmLibrary = new OtmLibrary( absLibrary, this );
        otmLibrary.add( pi ); // Needed to know base namespace

        // Add new library to the maps
        libraries.put( absLibrary, otmLibrary );
        addDomain( absLibrary );
        // Map of base namespaces with all libraries in that namespace
        if (absLibrary instanceof TLLibrary)
            if (versionChainFactory != null) {
                baseNSManaged.put( otmLibrary.getNameWithBasenamespace(),
                    versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
                // log.debug( "Added chain for manged base namespace: " + otmLibrary.getNameWithBasenamespace() );
            } else {
                baseNSUnmanaged.put( otmLibrary.getNameWithBasenamespace(), otmLibrary );
                // log.debug( "Added unmanged base namespace: " + otmLibrary.getNameWithBasenamespace() );
            }

        // For each named member use the factory to create and add OtmObject
        absLibrary.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, this ) );

        return otmLibrary;
    }

    /**
     * NOTE - this is slow compared with {@link #contains(tlMember)}
     * 
     * @return true if the member exists as a value in the members map.
     */
    public boolean contains(OtmLibraryMember member) {
        return member != null && members.containsValue( member );
        // return member != null && member.getTL() instanceof LibraryMember && members.containsKey( member.getTlLM() );
    }

    /**
     * @return true if the TL Library Member exists as a key in the members map.
     */
    public boolean contains(LibraryMember tlMember) {
        return members.containsKey( tlMember );
    }

    public boolean contains(AbstractLibrary absLibrary) {
        return absLibrary != null && libraries.containsKey( absLibrary );
    }

    /**
     * Use the TL Model to attempt to get a version chain factory.
     * 
     * @return the factory or null if factory throws exception
     */
    private VersionChainFactory getVersionChainFactory() {
        VersionChainFactory versionChainFactory = null;
        try {
            versionChainFactory = new VersionChainFactory( tlModel );
        } catch (Exception e) {
            if (!showingError) {
                showingError = true;
                Platform.runLater( this::chainError );
            }
            // log.debug( "Exception trying to construct version chain factory: " + e.getLocalizedMessage() );
        }
        return versionChainFactory;
    }

    private static String CHAINERRORMESSAGE =
        "Serious error - a library has an invalid namespace. \nThis will prevent properly presenting libraries in version chains. Examine the library namespaces and either fix the or remove from project.";

    private void chainError() {
        if (dialogBox == null)
            dialogBox = DialogBoxContoller.init();
        dialogBox.show( CHAINERRORMESSAGE );
        dialogBox = null;
        showingError = false;
    }

    /**
     * Get all the projects from the project manager. Create libraries for all project items if they have not already be
     * modeled. Start validation and type resolution task.
     */
    public void addProjects() {
        // log.debug( "AddProjects() with " + getTlModel().getAllLibraries().size() + " libraries" );

        // Add projects to project map
        for (Project project : getProjectManager().getAllProjects())
            otmProjectManager.add( project );
        // otmProjectManager.addProject( project.getName(), new OtmProject( project, this ) );
        //
        // TODO - examine and if needed improve JUNIT
        //

        // Get the built in libraries, will do nothing if already added
        addBuiltInLibraries( getTlModel() );

        // Get Libraries - Libraries can belong to multiple projects.
        // Map will de-dup the entries based on baseNS and name.
        for (ProjectItem pi : getProjectManager().getAllProjectItems()) {
            add( pi );
        }

        startValidatingAndResolvingTasks();
        // log.debug( "Model has " + members.size() + " members." );
    }

    /**
     * Start the validation and type resolver tasks. Use this model manager to handle results and its status controller.
     */
    public void startValidatingAndResolvingTasks() {
        // Start a background task to validate the objects
        new ValidateModelManagerItemsTask( this, this, statusController ).go();
        // Start a background task to resolve type relationships
        new TypeResolverTask( this, this, statusController ).go();
    }

    /**
     * Add the built in libraries to the libraries and member maps
     */
    protected void addBuiltInLibraries(TLModel tlModel) {
        for (BuiltInLibrary tlLib : tlModel.getBuiltInLibraries()) {
            // if (libraries.containsKey( tlLib )) {
            // log.warn( "Trying to add builtin library again." );
            // }
            libraries.put( tlLib, new OtmBuiltInLibrary( tlLib, this ) );
            for (LibraryMember tlMember : tlLib.getNamedMembers()) {
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
     * If the project item is new to this model manager:
     * <ul>
     * <li>create OtmLibrary to represent the abstract TL library
     * <li>add the absLibrary:OtmLibrary pair to the libraries map
     * <li>add the libraryNamespace:library in the baseNS map
     * </ul>
     * <p>
     * base namespaces can have multiple libraries.
     * 
     * @param pi
     */
    private void add(ProjectItem pi) {
        if (pi == null)
            return;
        AbstractLibrary absLibrary = pi.getContent();
        if (absLibrary == null)
            return;
        // log.debug( "Adding project item: " + absLibrary.getName() + " in " + absLibrary.getNamespace() );
        if (contains( absLibrary )) {
            // let the library track project as needed to know if the library is editable
            libraries.get( absLibrary ).add( pi );
        } else {
            // Model and Add newly discovered library to the libraries and baseNS maps
            add( pi, getVersionChainFactory() );
        }
        OtmLibrary lib = libraries.get( absLibrary );
        if (lib != null) {
            // Could be a minor version which will require refreshing the chain
            lib.getVersionChain().refresh();
        } else
            log.error( "Failed to find newly added library." );
    }

    /**
     * Clear the model. Clears the model manager's data, the TL Model, and Project Manager.
     */
    public void clear() {
        baseNSManaged.clear();
        baseNSUnmanaged.clear();
        libraries.clear();
        members.clear();
        domains.clear();
        getTlModel().clearModel();
        if (otmProjectManager != null)
            otmProjectManager.clear();

        addBuiltInLibraries( getTlModel() );

        // log.debug( "Cleared model. " + tlModel.getAllLibraries().size() );
    }

    /**
     * Examine all members. Return list of owners that have a of a descendant type user that is assigned to provider.
     * 
     * @param provider
     * @return
     */
    public List<OtmLibraryMember> findUsersOf(OtmTypeProvider provider) {
        // Changed 11/5/2019 - why copy list? The list is not changing.
        List<OtmLibraryMember> values = new ArrayList<>( members.values() );
        List<OtmLibraryMember> users = new ArrayList<>();
        for (OtmLibraryMember m : values) {
            if (m.getUsedTypes().contains( provider ))
                users.add( m );
        }
        // if (!users.isEmpty())
        // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        return users;
    }

    /**
     * Examine all members. Return list of members that use the passed member as a base type.
     * 
     * @param member
     * @return
     */
    public List<OtmLibraryMember> findSubtypesOf(OtmLibraryMember member) {
        // Changed 11/5/2019 - why copy list? The list is not changing.
        List<OtmLibraryMember> values = new ArrayList<>( members.values() );
        List<OtmLibraryMember> subTypes = new ArrayList<>();
        // Contextual facets use base type to define injection point
        for (OtmLibraryMember m : values) {
            if (m.getBaseType() == member && !(m instanceof OtmContextualFacet))
                subTypes.add( m );
        }
        // if (!users.isEmpty())
        // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        return subTypes;
    }

    /**
     * @param TL Abstract Library
     * @return OtmLibrary associated with the abstract library
     */
    public OtmLibrary get(AbstractLibrary absLibrary) {
        return libraries.get( absLibrary );
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
     * @return the abstract library associated with the otm library parameter.
     */
    public AbstractLibrary get(OtmLibrary library) {
        for (Entry<AbstractLibrary,OtmLibrary> set : libraries.entrySet())
            if (set.getValue() == library)
                return set.getKey();
        return null;
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

    public DexActionManager getMinorActionManager(boolean full) {
        return full ? minorActionManager : readOnlyActionManager;
    }

    /**
     * @return New arrayList of strings for both managed and unmanaged base namespaces.
     */
    public Set<String> getBaseNamespaces() {
        Set<String> nsList = new HashSet<>( baseNSManaged.keySet() );
        nsList.addAll( baseNSUnmanaged.keySet() );
        return nsList;
        // return baseNSManaged.keySet();
    }

    /**
     * @return Live list of all domains (baseNamespaces) in the model.
     */
    public List<OtmDomain> getDomains() {
        // List<String> dNames = new ArrayList<>();
        // domains.forEach( d -> dNames.add( d.getDomain() ) );
        return domains;
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
            if (l.getBaseNamespace().equals( baseNamespace ))
                libList.add( l );
        } );
        return libList;
    }


    /**
     * @return just user libraries, not built-in
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
     * @param baseNamespace
     * @return If the namespace is managed, the set contains all the <b>managed</b> libraries in base namespace
     *         (namespace root). If the namespace is unmanged, the set is just the single library with that
     *         namespace+name.
     */
    public Set<OtmLibrary> getLibraryChain(String baseNamespace) {
        Set<OtmLibrary> libs = new LinkedHashSet<>();
        VersionChain<TLLibrary> chain = baseNSManaged.get( baseNamespace );
        if (chain != null) {
            // Null means unmanaged libraries without a chain
            for (TLLibrary tlLib : chain.getVersions())
                if (libraries.get( tlLib ) != null)
                    libs.add( libraries.get( tlLib ) );
            // else
            // log.debug( "OOPS - library in chain is null." );
        } else {
            libs.add( baseNSUnmanaged.get( baseNamespace ) );
        }
        return libs;
    }

    /**
     * Get all libraries in the base namespace with the same major version
     * 
     * @param baseNamespace
     * @return List with libraries in that library's chain
     */
    public List<OtmLibrary> getVersionChain(OtmLibrary library) {
        List<OtmLibrary> versionChain = new ArrayList<>();
        String baseNS = library.getNameWithBasenamespace();
        // Null means unmanaged libraries without a chain
        VersionChain<TLLibrary> chain = baseNSManaged.get( baseNS );
        if (chain != null) {
            OtmLibrary otmLib;
            for (TLLibrary tlLib : chain.getVersions()) {
                otmLib = libraries.get( tlLib );
                try {
                    if (otmLib != null && otmLib.getMajorVersion() == library.getMajorVersion())
                        versionChain.add( otmLib );
                } catch (VersionSchemeException e) {
                    // if version error, ignore the library
                }
            }
        }
        return versionChain;
    }

    /**
     * Get a project that contains this library.
     * <p>
     * Note: OTM-DE used projects to manage write access to libraries. DEX does not.
     * 
     * @param library
     * @return
     */
    public OtmProject getManagingProject(OtmLibrary library) {
        library.getBaseNamespace();
        OtmProject foundProject = null;
        for (OtmProject project : getProjects()) {
            if (project.contains( get( library ) ))
                if (foundProject == null || library.getBaseNamespace().startsWith( project.getTL().getProjectId() ))
                    foundProject = project;
        }
        return foundProject;
    }

    public OtmLibraryMember getMember(TLModelElement tlMember) {
        if (tlMember instanceof LibraryMember)
            return members.get( (tlMember) );
        return null;
    }

    /**
     * Get the member with matching prefix and name
     * 
     * @param nameWithPrefix
     * @return member if found or null
     */
    public OtmLibraryMember getMember(String nameWithPrefix) {
        for (OtmLibraryMember candidate : getMembers())
            if (candidate.getNameWithPrefix().equals( nameWithPrefix ))
                return candidate;
        return null;
    }

    /**
     * @param name
     * @return list of members with matching names
     */
    public List<OtmLibraryMember> getMembers(OtmLibraryMember m) {
        List<OtmLibraryMember> matches = new ArrayList<>();
        for (OtmLibraryMember candidate : getMembers())
            if (m != candidate && candidate.getName().equals( m.getName() ))
                matches.add( candidate );
        return matches;
    }

    /**
     * Return a library member with the same name that is in the latest version of the libraries with the same base
     * namespace
     * 
     * @param member
     * @return
     */
    public OtmLibraryMember getLatestMember(OtmLibraryMember member) {
        for (OtmLibraryMember c : getMembers()) {
            if (c.getLibrary().getBaseNamespace().equals( member.getLibrary().getBaseNamespace() )
                && c.getName().equals( member.getName() ) && c.isLatestVersion())
                return c;
        }
        return null;
    }


    /**
     * @return all the library members being managed in a unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers() {
        return Collections.unmodifiableCollection( members.values() );
    }

    /**
     * @return new list with all the library members in that library
     */
    public List<OtmLibraryMember> getMembers(OtmLibrary library) {
        List<OtmLibraryMember> libraryMembers = new ArrayList<>();
        members.values().forEach( m -> {
            if (m.getLibrary() == library)
                libraryMembers.add( m );
        } );
        return libraryMembers;
    }



    /**
     * 
     * @return user settings or null
     */
    public UserSettings getUserSettings() {
        return userSettings;
    }

    /**
     * Get the TL Model used by this model manager.
     * 
     * @return
     */
    public TLModel getTlModel() {
        return tlModel;
    }

    public void handleEvent(DexChangeEvent e) {
        // WARNING - This runs often so keep this light weight or put into a background task.
        //
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
        if (lib == null || lib.getTL() == null)
            return false;
        // String key = lib.getNameWithBasenamespace();
        VersionChain<TLLibrary> chain = baseNSManaged.get( lib.getNameWithBasenamespace() );
        if (chain != null && lib.getTL() instanceof TLLibrary) {
            // List<TLLibrary> versions = chain.getVersions();
            return (chain.getNextVersion( (TLLibrary) lib.getTL() )) == null;
        }
        return true;
    }

    public void printLibraries() {
        libraries.entrySet().forEach( l -> log.debug( l.getValue().getName() ) );
    }

    public void setStatusController(DexStatusController statusController) {
        this.statusController = statusController;
    }

    /**
     * @return the OTM Project Manager
     */
    public OtmProjectManager getOtmProjectManager() {
        return otmProjectManager;
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
    public List<OtmResource> getResources(boolean sort) {
        List<OtmResource> resources = new ArrayList<>();
        members.values().forEach( m -> {
            if (m instanceof OtmResource)
                resources.add( (OtmResource) m );
        } );
        if (sort)
            resources.sort( (one, other) -> one.getName().compareTo( other.getName() ) );
        return resources;
    }

    // /**
    // * Get all base namespaces that start with the passed base namespace name.
    // *
    // * @param domain is the base namespace that identifies the domain
    // * @return new list
    // */
    // // TODO - test in junit. Test when there are no sub domains.
    // public List<String> getSubDomains(String domain) {
    // List<String> subs = new ArrayList<>();
    // getDomains().forEach( b -> {
    // if (b.startsWith( domain ))
    // subs.add( b );
    // } );
    // subs.remove( domain );
    // return subs;
    // }

    /**
     * @return
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
     * @return true if any library is editable
     */
    public boolean hasEditableLibraries() {
        for (OtmLibrary l : libraries.values())
            if (l.isEditable())
                return true;
        return false;
    }


    /**
     * @param library, can be null
     * @return true if any library except the passed library is editable
     */
    public boolean hasEditableLibraries(OtmLibrary library) {
        for (OtmLibrary l : libraries.values())
            if (l != library && l.isEditable())
                return true;
        return false;
    }

    public static final String XSD_LIBRARY_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String XSD_ID_NAME = "ID";
    public static final String XSD_DECIMAL_NAME = "decimal";
    public static final String XSD_INTEGER_NAME = "integer";
    public static final String XSD_STRING_NAME = "string";

    public OtmLibrary getXsdLibrary() {
        for (OtmLibrary lib : getLibraries()) {
            if (lib.getTL() instanceof BuiltInLibrary && lib.getTL().getNamespace().equals( XSD_LIBRARY_NAMESPACE ))
                return lib;
        }
        return null;
    }

    public OtmXsdSimple getXsdMember(String name) {
        OtmLibrary lib = getXsdLibrary();
        LibraryMember member = null;
        if (lib != null)
            member = lib.getTL().getNamedMember( name );
        OtmObject otm = OtmModelElement.get( (TLModelElement) member );
        return otm instanceof OtmXsdSimple ? (OtmXsdSimple) otm : null;
    }

    /**
     * Try to find the XSD ID type and return it
     * 
     * @return
     */
    public OtmXsdSimple getIdType() {
        return getXsdMember( XSD_ID_NAME );
        // LibraryMember tlId = null;
        // for (OtmLibrary lib : getLibraries()) {
        // if (lib.getTL() instanceof BuiltInLibrary && lib.getTL().getNamespace().equals( XSD_LIBRARY_NAMESPACE ))
        // tlId = lib.getTL().getNamedMember( XSD_ID_NAME );
        // }
        // OtmObject id = OtmModelElement.get( (TLModelElement) tlId );
        // // if (id == null)
        // // log.debug( "Missing ID type to return." );
        // return id instanceof OtmXsdSimple ? (OtmXsdSimple) id : null;
    }


    /**
     * @return the ota 2.0 Empty simple type
     */
    public OtmXsdSimple getEmptyType() {
        LibraryMember tlId = null;
        for (OtmLibrary lib : getLibraries()) {
            if (lib.getTL() instanceof BuiltInLibrary && lib.getTL().getNamespace().equals( OTA_LIBRARY_NAMESPACE ))
                tlId = lib.getTL().getNamedMember( OTA_EMPTY_NAME );
            // log.debug( "Library " + lib + " namespace = " + lib.getTL().getNamespace() );
        }
        OtmObject id = OtmModelElement.get( (TLModelElement) tlId );
        return id instanceof OtmXsdSimple ? (OtmXsdSimple) id : null;
    }

}
