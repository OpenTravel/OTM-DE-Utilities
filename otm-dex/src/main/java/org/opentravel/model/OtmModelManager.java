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
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
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
import org.opentravel.model.otmLibraryMembers.TestInheritance;
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
import org.opentravel.schemacompiler.version.VersionChainFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
// Refactoring Idea - should i have a ModelDomainManager instead of a namespace manager?
// The Domain structure is great.
// If i add a VersionChain field, the mapping is 1 : 1
//
public class OtmModelManager implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( OtmModelManager.class );


    // Internal Managers
    private OtmProjectManager otmProjectManager;
    private OtmModelMapsManager otmMapManager;
    private OtmModelChainsManager chainsManager = null;
    private OtmModelNamespaceManager nsManager = null;
    private OtmModelMembersManager membersManager = null;

    // // Map of base namespaces with all managed libraries in that namespace
    // @Deprecated
    // private Map<String,VersionChain<TLLibrary>> baseNSManaged = new HashMap<>();
    // @Deprecated
    // private Map<String,OtmLibrary> baseNSUnmanaged = new HashMap<>();


    // private List<String> baseNSList = new ArrayList<>();

    // // Map Otm Managed libraries to the version chain. The same chain is used for all members of the chain.
    // private Map<OtmManagedLibrary,OtmVersionChain> chainMap = new HashMap<>();

    // Open libraries - Abstract Libraries are built-in and user
    private Map<AbstractLibrary,OtmLibrary> libraries = new HashMap<>();

    // // All members - Library Members are TLLibraryMembers and contextual facets
    // public static final int MEMBERCOUNT = 2666; // 2000 / .075 +1;
    // private Map<LibraryMember,OtmLibraryMember> members = new HashMap<>( MEMBERCOUNT );
    // private Map<LibraryMember,OtmLibraryMember> syncedMembers = Collections.synchronizedMap( members );

    // Domains - one entry per unique base namespace
    private List<OtmDomain> domains = new ArrayList<>();

    private DexActionManager readOnlyActionManager = new DexReadOnlyActionManager();
    private DexActionManager minorActionManager;
    private DexActionManager fullActionManager;

    private DexStatusController statusController;
    // private RepositoryManager repositoryManager;
    private TLModel tlModel = null;

    public static final String OTA_EMPTY_NAME = "Empty";
    public static final String XSD_ID_NAME = "ID";
    public static final String XSD_DECIMAL_NAME = "decimal";
    public static final String XSD_INTEGER_NAME = "integer";
    public static final String XSD_STRING_NAME = "string";

    private DialogBoxContoller dialogBox = null;
    private boolean showingError = false;

    private UserSettings userSettings;

    private int backgroundTaskCount = 0;



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
        // // Bring in the built-in libraries
        // addLibraries_BuiltIn( tlModel );
        //
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

        // Initialize internal managers
        chainsManager = new OtmModelChainsManager();
        otmMapManager = new OtmModelMapsManager( this );
        nsManager = new OtmModelNamespaceManager( this );
        membersManager = new OtmModelMembersManager( this );

        // Bring in the built-in libraries. Do last - relies on managers
        addLibraries_BuiltIn( tlModel );

        log.debug( "Model Manager constructor complete." );
    }

    public OtmModelMapsManager getMapManager() {
        return otmMapManager;
    }

    /**
     * Simply add the member to the members maps if it is not already in the map.
     * <p>
     * See {@link OtmLibrary#add(OtmLibraryMember)} to add to both TL library and manager.
     * 
     * <p>
     * Facade for {@linkplain OtmModelMembersManager#add(OtmLibraryMember)}
     * 
     * @param member
     */
    public void add(OtmLibraryMember member) {
        membersManager.add( member );
        // if (member != null && member.getTL() instanceof LibraryMember && !contains( member.getTlLM() ))
        // members.put( member.getTlLM(), member );
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
        // if (member != null && member.getTL() instanceof LibraryMember && contains( member.getTlLM() ))
        // members.remove( member.getTlLM(), member );
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
        // Add will prevent duplicate entries
        // getTlModel().getUserDefinedLibraries().forEach( tlLib -> addOLD( tlLib ) );
        // TESTME
        getTlModel().getUserDefinedLibraries().forEach( this::addLibrary );

        startValidatingAndResolvingTasks();
        // log.debug( "Added Libraries. Model has " + members.size() + " members." );
    }

    // /**
    // * Add the TL library to the model if it is not already in the model. Adds all the members.
    // * <p>
    // * Does <b>not</b> resolve types. Does <b>not</b> validate the objects.
    // * <p>
    // * Create OtmLibrary facade.
    // *
    // * @see #startValidatingAndResolvingTasks()
    // *
    // * @param library to add
    // * @return newly created OtmLibrary
    // */
    // @Deprecated
    // public OtmLibrary addOLD(AbstractLibrary absLibrary) {
    // ProjectItem pi = getProjectManager().getProjectItem( absLibrary );
    // if (getProjectManager().getProjectItem( absLibrary ) != null)
    // log.debug( "Has PI" ); // If this is true, the OtmLibraryFactory should do the test.
    // return addLibrary( absLibrary );
    // // return add( absLibrary, getVersionChainFactory() );
    // // FIXME - return add( absLibrary );
    // }

    /**
     * Facade for TL {@link ProjectManager#getProjectItem(AbstractLibrary)}
     * 
     * @return PI if library is TLLibrary (not built-in or XSD) and project manager defined
     */
    public ProjectItem getProjectItem(AbstractLibrary absLib) {
        return absLib instanceof TLLibrary && getProjectManager() != null ? getProjectManager().getProjectItem( absLib )
            : null;
    }

    // // Fixme - should be ok. fixed OtmLibrary to return baseNamespace without PI.
    // // Todo - refactor add(*) to simplify
    // // Not used on open project task
    // /**
    // * Model the TL library, add to OTM maps, then model the members.
    // *
    // * @param absLibrary
    // * @param versionChainFactory - used to retrieve the chain for
    // * @return
    // */
    // @Deprecated
    // protected OtmLibrary add(AbstractLibrary absLibrary, VersionChainFactory versionChainFactory) {
    // if (absLibrary == null)
    // return null;
    // if (contains( absLibrary ))
    // return libraries.get( absLibrary );
    //
    // // Model the library - create the OTM Library facade
    // OtmLibrary otmLibrary;
    // if (OtmLibraryFactory.isUnmanaged( absLibrary, tlModel ))
    // log.debug( "Do unmanaged" );
    // try {
    // otmLibrary = OtmLibraryFactory.newLibrary( absLibrary, this );
    // } catch (DexLibraryException e) {
    // return null;
    // }
    // // OtmLibrary otmLibrary = new OtmLibrary( absLibrary, this );
    //
    // // Add library to map
    // addToMaps( otmLibrary );
    // // libraries.put( absLibrary, otmLibrary );
    // // addDomain( absLibrary );
    // //
    // // // Put in correct base namespace Map
    // // // + Managed libraries map has NS : chain
    // // // + Unmanaged libraries map has NS : OtmLibrary
    // // String baseNS = otmLibrary.getNameWithBasenamespace();
    // // VersionChain<TLLibrary> chain = null;
    // // if (absLibrary instanceof TLLibrary && versionChainFactory != null)
    // // chain = versionChainFactory.getVersionChain( (TLLibrary) absLibrary );
    // // if (chain != null) {
    // // baseNSManaged.put( baseNS, chain );
    // // // log.debug( "Added " + baseNS + otmLibrary.getVersion() + " to base NS managed." );
    // // } else {
    // // baseNSUnmanaged.put( baseNS, otmLibrary );
    // // // log.debug( "Added " + baseNS + otmLibrary.getVersion() + " to base NS UN-managed." );
    // // }
    // // }
    // // if (absLibrary instanceof TLLibrary) {
    // // String baseNS = otmLibrary.getNameWithBasenamespace();
    // // VersionChain<TLLibrary> chain = versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
    // // if (versionChainFactory != null) {
    // // baseNSManaged.put( baseNS, versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
    // // // log.debug( "Added " + baseNS + otmLibrary.getVersion() + " to base NS managed." );
    // // } else {
    // // baseNSUnmanaged.put( baseNS, otmLibrary );
    // // // log.debug( "Added " + baseNS + otmLibrary.getVersion() + " to base NS UN-managed." );
    // // }
    // // }
    //
    // // For each named member use the factory to create and add OtmObject
    // absLibrary.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, this ) );
    //
    // return otmLibrary;
    // }

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

    // @Deprecated
    // public OtmLibrary addUnmanagedOLD(AbstractLibrary absLibrary) {
    // return add( absLibrary, null );
    // }

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
            // Return the existing library.
            // if (libraries.get( absLibrary ) instanceof OtmManagedLibrary) {
            // mLib = (OtmManagedLibrary) libraries.get( absLibrary );
            // // Add PI to the library as needed to know if the library is editable
            // mLib.add( pi );
            newlib = get( absLibrary );
        } else {
            // // Assure the PI content is in the TL Model.
            // // Version chain and Library factories depend on library being in the TL Model.
            // if (!getTlModel().getUserDefinedLibraries().contains( absLibrary ))
            // getTlModel().addLibrary( absLibrary );

            // Create new library
            try {
                newlib = OtmLibraryFactory.newLibrary( pi, this );
                addToMaps( newlib );
                OtmLibraryFactory.modelMembers( pi.getContent(), this );
            } catch (DexLibraryException e) {
                log.warn( "Library Factory exception: " + e.getLocalizedMessage() );
                return null;
            }
            // if (!(newlib instanceof OtmManagedLibrary)) {
            // log.warn( "Library Factory returned an unmanaged library that should have been managed." );
            // return null;
            // }
            // mLib = (OtmManagedLibrary) newlib;

            // // Move to factory?
            // mLib.add( pi ); // Needed to know base namespace
            // // Add new library to the maps
            // libraries.put( absLibrary, mLib );
            // addDomain( absLibrary );
            //
            // // Check the chain, if it existed, replace with new one.
            // // TODO - create chainMap manager
            // chainMap.put( mLib, new OtmVersionChain( mLib ) ); // Move to factory? Yes, it knows major/minor
            //
            // // The next section is deprecated
            // //
            // // Map of base namespaces with all libraries in that namespace
            // String baseNS = newlib.getNameWithBasenamespace();
            // if (getVersionChainFactory() != null) {
            // baseNSManaged.put( baseNS, getVersionChainFactory().getVersionChain( (TLLibrary) absLibrary ) );
            // // log.debug( "Added chain for managed base namespace: " + baseNS );
            // } else {
            // baseNSUnmanaged.put( baseNS, newlib );
            // // log.debug( "Added unmanaged base namespace: " + baseNS );
            // }
            // // End deprecation

            // // TODO - move to factory
            // // Model Members - use the factory to create and add OtmObjects
            // absLibrary.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, this ) );

            // // Post-check
            // OtmLibrary lib = libraries.get( absLibrary );
            // if (lib == null)
            // log.error( "Failed to find newly added library." );
            // }
        }
        // log.debug( "Adding project item: " + absLibrary.getName() + " in " + absLibrary.getNamespace() );
        return newlib;
    }

    /**
     * Add this library to the maps. Should only be called by the OtmLibraryFactory.
     * <P>
     * OtmProject also uses it.
     * 
     * @param lib
     */
    public void addToMaps(OtmLibrary lib) {
        AbstractLibrary absLibrary = lib.getTL();
        putLibrary( absLibrary, lib );
        addDomain( absLibrary );
        nsManager.add( lib );
        chainsManager.add( lib );

        // // The next section is deprecated
        // //
        // // Map of base namespaces with all libraries in that namespace
        // String baseNS = lib.getNameWithBasenamespace();
        // VersionChain<TLLibrary> vc = OtmLibraryFactory.getTLVersionChain( absLibrary, getTlModel() );
        // if (lib instanceof OtmManagedLibrary) {
        // // if (getVersionChainFactory() != null) {
        // baseNSManaged.put( baseNS, vc );
        // log.debug( "Added chain for managed base namespace: " + baseNS );
        // } else {
        // baseNSUnmanaged.put( baseNS, lib );
        // log.debug( "Added unmanaged base namespace: " + baseNS );
        // }
        // // End deprecation
        //
    }

    // Exposed for testing
    protected void removeFromMaps(OtmLibrary lib) {
        AbstractLibrary absLibrary = lib.getTL();
        libraries.remove( absLibrary );
        chainsManager.remove( lib );
        nsManager.remove( lib );
        // TODO - what to do here??? addDomain( absLibrary );

        // // The next section is deprecated
        // String baseNS = lib.getNameWithBasenamespace();
        // baseNSManaged.remove( baseNS );
        // baseNSUnmanaged.remove( baseNS, lib );
    }

    // FIXME 11/26/2020 - the baseNamespace maps are "funky"
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

    // @Deprecated
    // protected OtmLibrary add(ProjectItem pi, VersionChainFactory versionChainFactory) {
    // if (pi == null)
    // return null;
    //
    // AbstractLibrary absLibrary = pi.getContent();
    // if (absLibrary == null)
    // return null;
    // if (contains( absLibrary ))
    // return libraries.get( absLibrary );
    //
    // // Create new library
    // // OtmLibrary otmLibrary = new OtmLibrary( absLibrary, this );
    // OtmLibrary otmLibrary;
    // try {
    // otmLibrary = OtmLibraryFactory.newLibrary( absLibrary, this );
    // } catch (DexLibraryException e) {
    // return null;
    // }
    // otmLibrary.add( pi ); // Needed to know base namespace
    //
    // // Add new library to the maps
    // libraries.put( absLibrary, otmLibrary );
    // addDomain( absLibrary );
    // // Map of base namespaces with all libraries in that namespace
    // String baseNS = otmLibrary.getNameWithBasenamespace();
    // if (absLibrary instanceof TLLibrary)
    // if (versionChainFactory != null) {
    // baseNSManaged.put( baseNS, versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
    // // log.debug( "Added chain for managed base namespace: " + baseNS );
    // } else {
    // baseNSUnmanaged.put( baseNS, otmLibrary );
    // // log.debug( "Added unmanaged base namespace: " + baseNS );
    // }
    //
    // // For each named member use the factory to create and add OtmObject
    // absLibrary.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, this ) );
    //
    // return otmLibrary;
    // }

    /**
     * NOTE - this is slow compared with {@link #contains(tlMember)}
     * 
     * @return true if the member exists as a value in the members map.
     */
    public boolean contains(OtmLibraryMember member) {
        return membersManager.contains( member );
        // return member != null && members.containsValue( member );
        // return member != null && member.getTL() instanceof LibraryMember && members.containsKey( member.getTlLM() );
    }

    /**
     * @return true if the TL Library Member exists as a key in the members map.
     */
    public boolean contains(LibraryMember tlMember) {
        return membersManager.contains( tlMember );
        // return members.containsKey( tlMember );
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
     * Use the TL Model to attempt to get a version chain factory.
     * 
     * @return the factory or null if factory throws exception
     */
    @Deprecated
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

        // TEST
        // TODO - remove(lib)

        // // Marshal parameters
        // String baseNS = lib.getNameWithBasenamespace();
        // OtmLibrary found = baseNSUnmanaged.get( baseNS );
        // VersionChainFactory factory = getVersionChainFactory();
        // VersionChain<TLLibrary> vc = null;
        // if (factory != null && lib.getTL() instanceof TLLibrary)
        // vc = factory.getVersionChain( (TLLibrary) lib.getTL() );
        //
        // // Pre-check
        // if (found == null) {
        // log.debug( "Error: library to change a library was not in unmanaged table." );
        // // if (vc == null)
        // // log.debug( "Error: tried to change a library that does not have a version chain." );
        // return;
        // }
        //
        // // Collection<OtmLibrary> libsOld = getUserLibraries();
        //
        // // Remove from unmanaged and add to managed
        // baseNSUnmanaged.remove( baseNS );
        // libraries.remove( lib.getTL() );
        //
        // baseNSManaged.put( baseNS, vc );
        //
        // libraries.put( newLib.getTL(), newLib );
        // putLibrary( newLib.getTL(), newLib );
        // // FIXME - need to update other maps
        //
        // // add( newLib.getTL() ); // FIXME - this will duplicate members
        //
        // // Collection<OtmLibrary> libsNew = getUserLibraries();
        // // log.debug( "Changed unmanaged to managed: " + newLib );
    }

    private static final String CHAINERRORMESSAGE =
        "Serious error - a library has an invalid namespace. \nThis will prevent properly presenting libraries in version chains. Examine the library namespaces and either fix the or remove from project.";

    private void chainError() {
        if (dialogBox == null)
            dialogBox = DialogBoxContoller.init();
        dialogBox.show( CHAINERRORMESSAGE );
        dialogBox = null;
        showingError = false;
    }

    // /**
    // * Get all the projects from the project manager. Create libraries for all project items if they have not already
    // be
    // * modeled. Start validation and type resolution task.
    // * <p>
    // * Used by
    // * {@link DexFileHandler#openProject(java.io.File, OtmModelManager,
    // org.opentravel.common.OpenProjectProgressMonitor)}
    // */
    // @Deprecated
    // public void addProjectsOLD() {
    // // log.debug( "AddProjects() with " + getTlModel().getAllLibraries().size() + " libraries" );
    //
    // // Add projects to project map
    // for (Project project : getProjectManager().getAllProjects())
    // otmProjectManager.add( project );
    // // otmProjectManager.addProject( project.getName(), new OtmProject( project, this ) );
    // //
    // // TODO - examine and if needed improve JUNIT
    // //
    //
    // // Get the built in libraries, will do nothing if already added
    // addBuiltInLibraries( getTlModel() );
    //
    // // Get Libraries - Libraries can belong to multiple projects.
    // // Map will de-dup the entries based on baseNS and name.
    // for (ProjectItem pi : getProjectManager().getAllProjectItems()) {
    // addOLD( pi );
    // }
    //
    // startValidatingAndResolvingTasks();
    // // log.debug( "Model has " + members.size() + " members." );
    // }

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
     * Start the validation and type resolver tasks. Use this model manager to handle results and its status controller.
     */
    public void startValidatingAndResolvingTasks() {
        // Start a background task to validate the objects
        new ValidateModelManagerItemsTask( this, this, statusController ).go();
        // Start a background task to resolve type relationships
        new TypeResolverTask( this, this, statusController ).go();
        backgroundTaskCount = 2;
    }
    // Attempt to publish event when resolver complete. Too complicated to pass through project open chain.
    // /**
    // * Start the validation and type resolver tasks.
    // * Use the passed handler for type resolver results and model manager to handle validation results.
    // * Use model manager's status controller.
    // */
    // public void startValidatingAndResolvingTasks(TaskResultHandlerI resultHandler) {
    // if (resultHandler == null) resultHandler = this;
    // // Start a background task to validate the objects
    // new ValidateModelManagerItemsTask( this, this, statusController ).go();
    // // Start a background task to resolve type relationships
    // new TypeResolverTask( this, resultHandler, statusController ).go();
    // backgroundTaskCount = 2;
    // }

    /**
     * Simply put the pair into the libraries map.
     */
    private void putLibrary(AbstractLibrary alib, OtmLibrary otmLib) {
        if (alib != null && otmLib != null)
            libraries.put( alib, otmLib );
    }

    /**
     * Add the built in libraries to the libraries and member maps
     */
    protected void addLibraries_BuiltIn(TLModel tlModel) {
        OtmLibrary otmLib;
        for (BuiltInLibrary builtInLib : tlModel.getBuiltInLibraries()) {
            // if (libraries.containsKey( builtInLib )) {
            // log.warn( "Trying to add builtin library again." );
            // }
            // libraries.put( builtInLib, new OtmBuiltInLibrary( builtInLib, this ) );
            try {
                otmLib = OtmLibraryFactory.newLibrary( builtInLib, this );
                putLibrary( builtInLib, otmLib );
                // libraries.put( builtInLib, otmLib );
            } catch (DexLibraryException e) {
                // TODO Auto-generated catch block
            }

            // TODO - move this code to OtmBuiltInLibrary constructor or method
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

    // /**
    // * If the project item is new to this model manager:
    // * <ul>
    // * <li>create OtmLibrary to represent the abstract TL library
    // * <li>add the absLibrary:OtmLibrary pair to the libraries map
    // * <li>add the libraryNamespace:library in the baseNS map
    // * </ul>
    // * <p>
    // * base namespaces can have multiple libraries.
    // *
    // * @param pi
    // */
    // @Deprecated
    // private void addOLD(ProjectItem pi) {
    // if (pi == null)
    // return;
    // AbstractLibrary absLibrary = pi.getContent();
    // if (absLibrary == null)
    // return;
    // // log.debug( "Adding project item: " + absLibrary.getName() + " in " + absLibrary.getNamespace() );
    // if (contains( absLibrary )) {
    // // let the library track project as needed to know if the library is editable
    // libraries.get( absLibrary ).add( pi );
    // } else {
    // // Model and Add newly discovered library to the libraries and baseNS maps
    // // For each named member use the factory to create and add OtmLibraryMember
    // add( pi, getVersionChainFactory() );
    // }
    // OtmLibrary lib = libraries.get( absLibrary );
    // if (lib != null && lib.getVersionChain() != null) {
    // // Could be a minor version which will require refreshing the chain
    // lib.getVersionChain().refresh();
    // } else
    // log.error( "Failed to find newly added library." );
    // }

    /**
     * Clear the model. Clears the model manager's data, the TL Model, and Project Manager.
     */
    public void clear() {
        // baseNSManaged.clear();
        // baseNSUnmanaged.clear();
        nsManager.clear();
        chainsManager.clear();
        membersManager.clear();

        libraries.clear();
        // members.clear();
        domains.clear();
        getTlModel().clearModel();
        if (otmProjectManager != null)
            otmProjectManager.clear();

        addLibraries_BuiltIn( getTlModel() );

        // log.debug( "Cleared model. " + tlModel.getAllLibraries().size() );
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
        // // Changed 11/5/2019 - why copy list? The list is not changing.
        // // List<OtmLibraryMember> values = new ArrayList<>( getMembers() );
        // List<OtmLibraryMember> users = new ArrayList<>();
        // for (OtmLibraryMember m : getMembers()) {
        // if (m.getUsedTypes().contains( provider ))
        // users.add( m );
        // }
        // // if (!users.isEmpty())
        // // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        // return users;
    }

    /**
     * Examine all members. Return list of members that use the passed member as a base type.
     * 
     * @param member
     * @return
     */
    public List<OtmLibraryMember> findSubtypesOf(OtmLibraryMember member) {
        return membersManager.findSubtypesOf( member );
        // // Changed 11/5/2019 - why copy list? The list is not changing.
        // List<OtmLibraryMember> values = new ArrayList<>( getMembers() );
        // List<OtmLibraryMember> subTypes = new ArrayList<>();
        // // Contextual facets use base type to define injection point
        // for (OtmLibraryMember m : values) {
        // if (m.getBaseType() == member && !(m instanceof OtmContextualFacet))
        // subTypes.add( m );
        // }
        // // if (!users.isEmpty())
        // // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        // return subTypes;
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

    public DexActionManager getMinorActionManager(boolean minor) {
        return minor ? minorActionManager : readOnlyActionManager;
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

    /**
     * @return unmodifiableList of strings for both managed and unmanaged base namespaces.
     */
    public List<String> getBaseNamespaces() {
        // FIXME
        // Find test
        return nsManager.getBaseNamespaces();

        // TESTME
        // Set<String> nsList = new HashSet<>( baseNSManaged.keySet() );
        // nsList.addAll( baseNSUnmanaged.keySet() );
        // return nsList;
        // // return baseNSManaged.keySet();
    }

    public List<OtmLibrary> getBaseNSLibraries(String baseNS) {
        return nsManager.getBaseNsLibraries( baseNS );
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
            if (l.getBaseNS().equals( baseNamespace ))
                libList.add( l );
        } );
        return libList;
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

    // Exposed for testing
    protected OtmModelChainsManager getChainsManager() {
        return chainsManager;
    }

    // /**
    // * Get all libraries in the version chain. For repository managed libraries, it uses
    // * {@link VersionChain#getVersions()}. If unmanaged, the model manager's unmanaged table is used.
    // *
    // * @param chainName
    // * @return If the namespace is managed, the set contains all the <b>managed</b> libraries in base namespace
    // * (namespace root). If the namespace is unmanaged, the set is just the single library with that
    // * namespace+name.
    // */
    /**
     * Get list of libraries in the chain. Facade for {@linkplain OtmModelChainsManager#getChainLibraries(String)}
     * 
     * @param chainName
     * @return
     */
    public List<OtmLibrary> getChainLibraries(String chainName) {
        return chainsManager.getChainLibraries( chainName );
    }

    public List<OtmLibrary> getChainLibraries(OtmLibrary lib) {
        return getChainLibraries( lib.getNameWithBasenamespace() );

        // // TESTME
        // Set<OtmLibrary> libs = new LinkedHashSet<>();
        // VersionChain<TLLibrary> chain = baseNSManaged.get( baseNamespace );
        // if (chain != null) {
        // // Null means unmanaged libraries without a chain
        // for (TLLibrary tlLib : chain.getVersions())
        // if (libraries.get( tlLib ) != null)
        // libs.add( libraries.get( tlLib ) );
        // // else
        // // log.debug( "OOPS - library in chain is null." );
        // } else {
        // libs.add( baseNSUnmanaged.get( baseNamespace ) );
        // }
        // return libs;
    }

    /**
     * Simply get the version chain from the chains manager.
     * 
     * @param lib
     * @return version chain if any or null
     */
    public OtmVersionChain getVersionChain(OtmLibrary lib) {
        return chainsManager.get( lib );
        // return chainMap.get( managedLibrary );
    }

    public OtmVersionChain getVersionChain(String chainName) {
        return chainsManager.get( chainName );
    }

    // /**
    // * @deprecated Only used in tests. Use getChainLibraries().
    // * <p>
    // * use the chain or fix this
    // * <p>
    // * Get all namespace managed libraries in the base namespace with the same major version
    // *
    // * @param baseNamespace
    // * @return List with libraries in that library's chain
    // */
    // @Deprecated
    // public List<OtmLibrary> getVersionChainLibraries(OtmLibrary library) {
    // List<OtmLibrary> versionChain = new ArrayList<>();
    // String baseNS = library.getNameWithBasenamespace();
    // // Null means unmanaged libraries without a chain
    // VersionChain<TLLibrary> chain = baseNSManaged.get( baseNS );
    // if (chain != null) {
    // OtmLibrary otmLib;
    // for (TLLibrary tlLib : chain.getVersions()) {
    // otmLib = libraries.get( tlLib );
    // // try {
    // if (otmLib != null && otmLib.getMajorVersion() == library.getMajorVersion())
    // versionChain.add( otmLib );
    // // } catch (VersionSchemeException e) {
    // // // if version error, ignore the library
    // // }
    // }
    // }
    // return versionChain;
    // }

    /**
     * @deprecated {@link #getProjectManager()} From the projects in the map, get one that contains the passed library.
     *             <p>
     *             If multiple projects are found, return the one whose ProjectID is at the beginning of the library's
     *             base namespace.
     *             <p>
     *             Note: This was more important for OTM-DE which used projects to manage write access to libraries. DEX
     *             does not.
     * 
     * @param library
     * @return project found or null
     */
    // FIXME - this whole idea is flawed. Fix it or remove it if it is not really needed.
    // Most users only need the TL ProjectManager from the project.
    // Change those to use getProjectManager() from model manager, or pass in ProjectManager.
    // VersionLibraryTask uses it to put the new libraries into that project.
    @Deprecated
    public OtmProject getManagingProject(OtmLibrary library) {
        // library.getBaseNamespace();
        OtmProject foundProject = null;
        for (OtmProject project : getProjects()) {
            if (project.contains( get( library ) ))
                if (foundProject == null || library.getBaseNS().startsWith( project.getTL().getProjectId() ))
                    foundProject = project;
        }
        return foundProject;
    }

    public OtmLibraryMember getMember(TLModelElement tlMember) {
        return membersManager.getMember( tlMember );
        // if (tlMember instanceof LibraryMember)
        // return members.get( (tlMember) );
        // return null;
    }

    /**
     * Get the member with matching prefix and name
     * 
     * @param nameWithPrefix formatted as prefix + ":" + name
     * @return member if found or null
     */
    public OtmLibraryMember getMember(String nameWithPrefix) {
        return membersManager.getMember( nameWithPrefix );
        // for (OtmLibraryMember candidate : getMembers())
        // if (candidate.getNameWithPrefix().equals( nameWithPrefix ))
        // return candidate;
        // return null;
    }

    /**
     * @param name
     * @return list of members with matching names
     */
    public List<OtmLibraryMember> getMembers(OtmLibraryMember m) {
        return membersManager.getMembers( m );
        // List<OtmLibraryMember> matches = new ArrayList<>();
        // for (OtmLibraryMember candidate : getMembers())
        // if (m != candidate && candidate.getName().equals( m.getName() ))
        // matches.add( candidate );
        // return matches;
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
        // for (OtmLibraryMember c : getMembers()) {
        // if (c.getLibrary().getBaseNS().equals( member.getLibrary().getBaseNS() )
        // && c.getName().equals( member.getName() ) && c.isLatestVersion())
        // return c;
        // }
        // return null;
    }


    /**
     * Synchronized access to members.values()
     * 
     * @return all the library members being managed in a unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers() {
        return membersManager.getMembers();
        // // return Collections.unmodifiableCollection( members.values() );
        // return Collections.unmodifiableCollection( syncedMembers.values() );
    }

    /**
     * Notes: using the commented out sync'ed code causes {@link TestInheritance#testInheritedCustomFacets()} to time
     * out. getMembers() uses the synchronized member list.
     * <p>
     * 
     * @param filter DexFilter to use to select members. If null, all members are selected.
     * @return all the filter selected library members in an unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers(DexFilter<OtmLibraryMember> filter) {
        return membersManager.getMembers( filter );
        // // log.debug( "Starting to get filtered members." );
        // if (filter == null)
        // return getMembers();
        // // List<OtmLibraryMember> selected = Collections.synchronizedList( new ArrayList<>() );
        // // synchronized (selected) {
        // // getMembers().forEach( m -> {
        // // if (filter.isSelected( m ))
        // // selected.add( m );
        // // } );
        // // }
        // // 5/26/2021
        // List<OtmLibraryMember> selected = new ArrayList<>();
        // getMembers().forEach( m -> {
        // if (filter.isSelected( m ))
        // selected.add( m );
        // } );
        // log.debug( "Got " + selected.size() + " filtered members." );
        // return Collections.unmodifiableCollection( selected );
    }

    /**
     * 
     * @return new collection of all contextual facets in the model.
     */
    public Collection<OtmLibraryMember> getMembersContextualFacets() {
        return membersManager.getMembersContextualFacets();
    }

    /**
     * @return new list with all the library members in that library
     */
    public List<OtmLibraryMember> getMembers(OtmLibrary library) {
        return membersManager.getMembers( library );
        // List<OtmLibraryMember> libraryMembers = new ArrayList<>();
        // getMembers().forEach( m -> {
        // if (m.getLibrary() == library)
        // libraryMembers.add( m );
        // } );
        // return libraryMembers;
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

    public int getBackgroundTaskCount() {
        return backgroundTaskCount;
    }

    // public boolean isPublishedAsUnmanaged(OtmLibrary lib) {
    // return baseNSManaged.get( lib.getNameWithBasenamespace() ) == null;
    // }
    //
    // public boolean isPublishedAsManaged(OtmLibrary lib) {
    // return baseNSUnmanaged.get( lib.getNameWithBasenamespace() ) == null;
    // }

    /**
     * Look into the chain and return true if this is the latest version (next version = null)
     * <p>
     * True if not in a chain.
     * 
     * @param lib
     * @return
     */
    public boolean isLatest(OtmLibrary lib) {
        // if (lib == null || lib.getTL() == null)
        // return false;
        if (chainsManager.get( lib ) != null) {
            return chainsManager.get( lib ).isLatest( lib );
        }
        log.warn( "Chain manager did not find a chain for " + lib );
        return true;
        // TESTME
        // String key = lib.getNameWithBasenamespace();
        // VersionChain<TLLibrary> chain = baseNSManaged.get( lib.getNameWithBasenamespace() );
        // if (chain != null && lib.getTL() instanceof TLLibrary) {
        // // List<TLLibrary> versions = chain.getVersions();
        // return (chain.getNextVersion( (TLLibrary) lib.getTL() )) == null;
        // }
        // return true;
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
        getMembers().forEach( m -> {
            if (m instanceof OtmResource)
                resources.add( (OtmResource) m );
        } );
        if (sort)
            resources.sort( (one, other) -> one.getName().compareTo( other.getName() ) );
        return resources;
    }

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


    public OtmLibrary getXsdLibrary() {
        for (OtmLibrary lib : getLibraries()) {
            if (lib.getTL() instanceof BuiltInLibrary
                && lib.getTL().getNamespace().equals( OtmModelNamespaceManager.XSD_LIBRARY_NAMESPACE ))
                return lib;
        }
        return null;
    }

    public OtmBuiltInLibrary getBuiltInLibrary() {
        OtmBuiltInLibrary biLib = null;
        for (OtmLibrary lib : getLibraries())
            if (lib.getTL() instanceof BuiltInLibrary
                && lib.getTL().getNamespace().equals( OtmModelNamespaceManager.OTA_LIBRARY_NAMESPACE ))
                return (OtmBuiltInLibrary) lib;
        return biLib;
    }

    public OtmXsdSimple getXsdMember(String name) {
        return membersManager.getXsdMember( name, getXsdLibrary() );
        // OtmLibrary lib = getXsdLibrary();
        // LibraryMember member = null;
        // if (lib != null)
        // member = lib.getTL().getNamedMember( name );
        // OtmObject otm = OtmModelElement.get( (TLModelElement) member );
        // return otm instanceof OtmXsdSimple ? (OtmXsdSimple) otm : null;
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
     * Try to find the XSD String type and return it
     * 
     * @return
     */
    public OtmXsdSimple getStringType() {
        return getXsdMember( XSD_STRING_NAME );
    }


    /**
     * @return the ota 2.0 Empty simple type
     */
    public OtmXsdSimple getEmptyType() {
        return membersManager.getXsdMember( OTA_EMPTY_NAME, getBuiltInLibrary() );
        // LibraryMember tlId = null;
        // for (OtmLibrary lib : getLibraries()) {
        // if (lib.getTL() instanceof BuiltInLibrary
        // && lib.getTL().getNamespace().equals( OtmModelNamespaceManager.OTA_LIBRARY_NAMESPACE ))
        // tlId = lib.getTL().getNamedMember( OTA_EMPTY_NAME );
        // // log.debug( "Library " + lib + " namespace = " + lib.getTL().getNamespace() );
        // }
        // OtmObject id = OtmModelElement.get( (TLModelElement) tlId );
        // return id instanceof OtmXsdSimple ? (OtmXsdSimple) id : null;
    }

    /**
     * Used only in testing when the default repository location is mocked after the model manager and its project
     * manager are created.
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

    /**
     * @return
     */
    public OtmModelMembersManager getOtmMembersManager() {
        return membersManager;
    }
}
