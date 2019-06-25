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
import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.dex.actions.DexReadOnlyActionManager;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.model.TypeResolverTask;
import org.opentravel.dex.tasks.model.ValidateModelManagerItemsTask;
import org.opentravel.model.otmContainers.OtmBuiltInLibrary;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberFactory;
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
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.concurrent.WorkerStateEvent;

/**
 * Manage access to all objects in scope.
 * 
 * @author dmh
 *
 */
public class OtmModelManager implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( OtmModelManager.class );

    // Open projects - projectName and otmProject
    private Map<String,OtmProject> projects = new HashMap<>();

    // Map of base namespaces with all libraries in that namespace
    private Map<String,VersionChain<TLLibrary>> baseNSMap = new HashMap<>();

    // Open libraries - Abstract Libraries are built-in and user
    private Map<AbstractLibrary,OtmLibrary> libraries = new HashMap<>();

    // All members - Library Members are TLLibraryMembers and contextual facets
    private Map<LibraryMember,OtmLibraryMember> members = new HashMap<>();

    private DexActionManager actionMgr;
    private DexStatusController statusController;

    private TLModel tlModel = null;

    /**
     * Create a model manager.
     * 
     * @param controller
     * @param actionManager action manager to assign to all members
     */
    public OtmModelManager(DexActionManager actionManager) {
        if (actionManager == null)
            this.actionMgr = new DexReadOnlyActionManager();
        this.actionMgr = actionManager;
        // actionManager.setModelManager(this);
        // this.statusController = statusController;

        // Create a TL Model
        // FIXME - this is not the model being used when adding projects
        try {
            tlModel = new TLModel();
        } catch (Exception e) {
            log.info( "Exception creating new model: " + e.getLocalizedMessage() );
        }
        // Tell model to track changes to maintain its type integrity
        tlModel.addListener( new ModelIntegrityChecker() );
        log.debug( "TL Model created and integrity checker added." );

        // // Render the built-in libraries
        // addBuiltInLibraries();
    }

    /**
     * @param member
     */
    public void add(OtmLibraryMember member) {
        if (member != null && member.getTL() instanceof LibraryMember)
            members.put( member.getTlLM(), member );
    }

    /**
     * Add all the libraries in this project to the model manager. Model all libraries and contents. Run validation and
     * type resolver in the background.
     * 
     * @param pm
     */
    public void add(ProjectManager pm) {
        log.debug( "New project with " + pm.getModel().getAllLibraries().size() + " libraries" );

        // Add projects to project map
        for (Project project : pm.getAllProjects())
            projects.put( project.getName(), new OtmProject( project ) );

        // TODO - model should be managed outside of add(Project) - this should only check to see if one exists.
        if (pm.getModel() != tlModel)
            log.debug( "Models are different" );
        tlModel = pm.getModel();
        // TODO - document how tlModel is managed.

        // Tell model to track changes to maintain its type integrity
        pm.getModel().addListener( new ModelIntegrityChecker() );

        // Get the built in libraries
        addBuiltInLibraries( pm.getModel() );
        // TODO - check to see if built-ins are already loaded.

        // Get Libraries
        //
        // Libraries can belong to multiple projects. Map will de-dup the entries based on baseNS and name.
        VersionChainFactory versionChainFactory = new VersionChainFactory( tlModel );
        for (ProjectItem pi : pm.getAllProjectItems()) {
            addLibrary( pi, versionChainFactory );
        }

        // Get Members
        for (AbstractLibrary tlLib : tlModel.getAllLibraries()) {
            for (LibraryMember tlMember : tlLib.getNamedMembers()) {
                OtmLibraryMemberFactory.memberFactory( tlMember, this ); // creates and adds
            }
        }

        // Start a background task to validate the objects
        new ValidateModelManagerItemsTask( this, this, statusController ).go();
        // Start a background task to resolve type relationships
        new TypeResolverTask( this, this, statusController ).go();

        log.debug( "Model has " + members.size() + " members." );
    }

    /**
     * Add the built in libraries to the libraries and member maps
     */
    private void addBuiltInLibraries(TLModel tlModel) {
        for (BuiltInLibrary tlLib : tlModel.getBuiltInLibraries()) {
            if (libraries.containsKey( tlLib )) {
                log.warn( "Trying to add builtin library again." );
            }
            libraries.put( tlLib, new OtmBuiltInLibrary( tlLib, this ) );
            for (LibraryMember tlMember : tlLib.getNamedMembers()) {
                OtmLibraryMemberFactory.memberFactory( tlMember, this ); // creates and adds
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
     * @param VersionChainFactory versionChainFactory = new VersionChainFactory(pi.getProjectManager().getModel());
     */
    private void addLibrary(ProjectItem pi, VersionChainFactory versionChainFactory) {
        if (pi == null)
            return;
        AbstractLibrary absLibrary = pi.getContent();
        if (absLibrary == null)
            return;
        for (AbstractLibrary l : libraries.keySet())
            if (l.getNamespace().equals( absLibrary.getNamespace() ) && l.getName().equals( absLibrary.getName() )) {
                log.debug( "Same URL found." );
                // libraries.get(l).add(pi);
                // return;
            }
        if (libraries.containsKey( absLibrary )) {
            // let the library track project as needed to know if the library is editable
            libraries.get( absLibrary ).add( pi );
        } else {
            OtmLibrary lib = new OtmLibrary( pi, this );
            libraries.put( absLibrary, lib );
            if (absLibrary instanceof TLLibrary)
                baseNSMap.put( lib.getNameWithBasenamespace(),
                    versionChainFactory.getVersionChain( (TLLibrary) absLibrary ) );
        }
    }

    /**
     * 
     */
    public void clear() {
        projects.clear();
        libraries.clear();
        members.clear();
        baseNSMap.clear();
    }

    public List<OtmLibraryMember> findUsersOf(OtmTypeProvider p) {
        List<OtmLibraryMember> values = new ArrayList<>( members.values() );
        List<OtmLibraryMember> users = new ArrayList<>();
        for (OtmLibraryMember m : values) {
            if (m.getUsedTypes().contains( p ))
                users.add( m );
        }
        // if (!users.isEmpty())
        // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        return users;
    }

    /**
     * @param TL Abstract Library
     * @return OtmLibrary associated with the abstract library
     */
    public OtmLibrary get(AbstractLibrary absLibrary) {
        if (!libraries.containsKey( absLibrary )) {
            // abstract library may be in the library pi list
            if (absLibrary != null)
                log.warn( "Missing library associated with: " + absLibrary.getName() );
            else
                log.warn( "Missing library." );
            printLibraries();
        }
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

    public AbstractLibrary get(OtmLibrary library) {
        for (Entry<AbstractLibrary,OtmLibrary> set : libraries.entrySet())
            if (set.getValue() == library)
                return set.getKey();
        return null;
    }

    public DexActionManager getActionManager() {
        return actionMgr;
    }

    public Set<String> getBaseNamespaces() {
        return baseNSMap.keySet();
    }

    public Collection<OtmLibrary> getLibraries() {
        return Collections.unmodifiableCollection( libraries.values() );
    }

    /**
     * Get all the libraries in a given base namespace (namespace root)
     * 
     * @param baseNamespace
     * @return
     */
    public Set<OtmLibrary> getLibraryChain(String baseNamespace) {
        Set<OtmLibrary> libs = new LinkedHashSet<>();
        VersionChain<TLLibrary> chain = baseNSMap.get( baseNamespace );
        for (TLLibrary tlLib : chain.getVersions())
            if (libraries.get( tlLib ) != null)
                libs.add( libraries.get( tlLib ) );
            else
                log.debug( "OOPS - library in chain is null." );
        return libs;
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
        for (OtmProject project : projects.values()) {
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
     * @return
     */
    public Collection<OtmLibraryMember> getMembers() {
        return Collections.unmodifiableCollection( members.values() );
    }

    /**
     * Get the TL Model used by this model manager.
     * 
     * @return
     */
    public TLModel getTlModel() {
        return tlModel;
    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        // NO-OP
    }

    /**
     * Look into the chain and return true if this is the latest version (next version = null)
     * 
     * @param lib
     * @return
     */
    public boolean isLatest(OtmLibrary lib) {
        if (lib == null || lib.getTL() == null)
            return false;
        VersionChain<TLLibrary> chain = baseNSMap.get( lib.getNameWithBasenamespace() );
        if (chain != null && lib.getTL() instanceof TLLibrary)
            return (chain.getNextVersion( (TLLibrary) lib.getTL() )) == null;
        return true;
    }

    private void printLibraries() {
        libraries.entrySet().forEach( l -> log.debug( l.getValue().getName() ) );
    }

    public void setStatusController(DexStatusController statusController) {
        this.statusController = statusController;
    }
}
