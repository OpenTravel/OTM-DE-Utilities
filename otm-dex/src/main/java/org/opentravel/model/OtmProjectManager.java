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
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLibraryFactory;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.concurrent.WorkerStateEvent;

/**
 * Manage collection of open OtmProjects and user settings related to projects.
 * <p>
 * This is a sub-manager of OtmModelManager.
 * 
 * @author dmh
 *
 */
public class OtmProjectManager implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( OtmProjectManager.class );

    // TODO - why have a duplicate project store? Consider just using the TLProjMrg's project list.
    // Open projects - projectName and otmProject
    private Map<String,OtmProject> projects = new HashMap<>();
    private OtmModelManager modelManager = null;

    private HashMap<String,File> projectFileMap = new HashMap<>();

    private ProjectManager tlProjectManager;
    private UserSettings userSettings;



    /**
     * Create an OTM Project manager.
     * 
     * @param fullActionManager edit-enabled action manager to assign to all members. If null, read-only action manager
     *        will be used.
     * @param controller
     * @param userSettings used by object builders default values
     */
    public OtmProjectManager(OtmModelManager modelManager, ProjectManager projectManager) {
        this.modelManager = modelManager;
        this.userSettings = modelManager.getUserSettings();
        this.tlProjectManager = projectManager;
    }


    /**
     * Using the TLProject's name, add project to project map and to recent projects in user settings.
     */
    private void add(OtmProject project) {
        if (project.getTL() != null && project.getTL().getName() != null) {
            projects.putIfAbsent( project.getTL().getName(), project );
            if (hasSettings()) {
                userSettings.setRecentProject( project );
                userSettings.save();
            }
        }
    }

    /**
     * If the TL-Project'name is not in the map, create a new OtmProject facade and {@link #add(OtmProject)}.
     * 
     * @param tlProject
     */
    public OtmProject add(Project tlProject) {
        OtmProject otp = null;
        if (tlProject != null && tlProject.getName() != null && !tlProject.getName().isEmpty()
            && !projects.containsKey( tlProject.getName() )) {
            otp = new OtmProject( tlProject, modelManager );
            add( otp );
        }
        return otp;
    }

    /**
     * @return true if the project exists as a value in the project map.
     */
    public boolean contains(OtmProject member) {
        return member != null && projects.containsValue( member );
    }

    public OtmProject getProject(String projectName) {
        return projects.get( projectName );
    }

    /**
     * Clear the model. Clears the model manager's data, the TL Model, and Project Manager.
     */
    public void clear() {
        projects.clear();
        if (tlProjectManager != null)
            tlProjectManager.closeAll();
        // log.debug( "Cleared projects. ");
    }

    // ??
    public void close(OtmProject oProject) {
        // Done in project - projectManager.closeProject( project );
        if (oProject != null && oProject.getTL() != null)
            projects.remove( oProject.getTL().getName() );
    }

    public OtmProject get(String name) {
        return projects.get( name );
    }

    /**
     * @param repository project
     * @return OtmLibrary associated with the repository (TL) Project
     */
    public OtmProject get(Project project) {
        for (OtmProject oProject : projects.values())
            if (oProject.getTL() == project)
                return oProject;
        return null;
    }

    public String getNamespace(OtmProject project) {
        return project.getTL().getProjectId();
    }

    /**
     * Get a map of recently used project file names and files from user settings. Sorted by most recently used first.
     * 
     * @return
     */
    public Map<String,File> getRecentProjects() {
        Map<String,File> fileMap = new LinkedHashMap<>();
        for (File file : getRecentlyUsedProjectFiles())
            if (file.canRead())
                fileMap.put( file.getName(), file );
        return fileMap;
    }

    /**
     * Get the list of the recently used project files from user settings.
     * 
     * @return
     */
    public List<File> getRecentlyUsedProjectFiles() {
        return hasSettings() ? userSettings.getRecentProjects() : Collections.emptyList();
    }

    /**
     * Get the map of the open used project file names and their projects.
     * 
     * @return
     */
    public Map<String,OtmProject> getOpenFileMap() {
        Map<String,OtmProject> projectMap = new HashMap<>();
        modelManager.getProjects().forEach( p -> {
            if (p != null && p.getTL() != null && p.getTL().getProjectFile() != null)
                projectMap.put( p.getTL().getProjectFile().getName(), p );
        } );
        return projectMap;
    }

    /**
     * Get the list of the recently used project file names from user settings.
     * 
     * @return
     */
    public List<String> getRecentlyUsedProjectFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (File file : getRecentlyUsedProjectFiles())
            fileNames.add( file.getName() );
        return fileNames;
    }

    /**
     * Get the last project folder from user settings.
     * 
     * @return last accessed file or null
     */
    public File getProjectDirectory() {
        File initialDirectory = null;
        if (userSettings != null)
            initialDirectory = userSettings.getLastProjectFolder();
        return initialDirectory;
    }

    /**
     * Get the list of the projects from directory.
     * 
     * @return project file map which may be empty
     */
    public Map<String,File> getProjects(File initialDirectory) {
        if (initialDirectory != null) {
            for (File file : DexFileHandler.getProjectList( initialDirectory )) {
                projectFileMap.put( file.getName(), file );
            }
        }
        return projectFileMap;
    }

    /**
     * Get a project that contains this library.
     * <p>
     * Note: OTM-DE used projects to manage write access to libraries. DEX does not.
     * 
     * @param library
     * @return
     */
    // FIXME - this whole idea is flawed. Fix it or remove it if it is not really needed.
    // This not even used!!!
    @Deprecated
    public OtmProject getManagingProject(OtmLibrary library) {
        library.getBaseNS();
        OtmProject foundProject = null;
        for (OtmProject project : projects.values()) {
            if (project.contains( library.getTL() ))
                if (foundProject == null || library.getBaseNS().startsWith( project.getTL().getProjectId() ))
                    foundProject = project;
        }
        return foundProject;
    }

    public boolean hasProjects() {
        return !projects.isEmpty();
    }

    public Collection<OtmProject> getProjects() {
        return Collections.unmodifiableCollection( projects.values() );
    }

    /**
     * 
     * @param library
     * @return new list of projects that contain the library or empty list.
     */
    public List<OtmProject> getProjects(AbstractLibrary library) {
        List<OtmProject> list = new ArrayList<>();
        getProjects().forEach( p -> {
            if (p.contains( library ))
                list.add( p );
        } );
        return list;
    }

    /**
     * Model manager may not have settings when constructing the project manager.
     * 
     * @return userSettings or null
     */
    private Boolean hasSettings() {
        if (userSettings == null && modelManager != null)
            userSettings = modelManager.getUserSettings();
        return userSettings != null;
    }

    /**
     * @return just user projects, not built-in
     */
    public List<OtmProject> getUserProjects() {
        List<OtmProject> projectList = new ArrayList<>();
        projects.values().forEach( p -> {
            if (!(p.getTL() instanceof BuiltInProject))
                projectList.add( p );
        } );
        return projectList;
    }


    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        // NO-OP
        // log.debug( "Task complete" );
    }


    /**
     * Create a new project.
     * 
     * @param projectFile
     * @param required name the user-displayable name of the project
     * @param optional defaultContextId Assigns the default context ID to use for EXAMPLE generation in this project.
     * @param projectId the ID of the project, typically namespace being worked on. ProjectId must not already be in
     *        use.
     * @param description
     * @return
     * @throws LibrarySaveException
     */
    public OtmProject newProject(File projectFile, String name, String defaultContextId, String projectId,
        String description) throws LibrarySaveException {
        if (projectFile == null || projectId == null || projectId.isEmpty())
            throw new IllegalArgumentException( "Missing required argument(s) to create new project." );
        // Verify the file exists and is writable
        if (!projectFile.canWrite())
            throw new IllegalArgumentException( "Project file is not writable." );

        OtmProject op = null;

        if (tlProjectManager == null)
            throw new IllegalArgumentException( "Missing required project manager." );

        // log.debug( "Creating new project in file: " + projectFile.getAbsolutePath() );

        // The only way to add a project to the tlProjectManager is via newProject()
        Project p = null;
        try {
            p = tlProjectManager.newProject( projectFile, projectId, name, description );
            // p = new Project( tlProjectManager );
        } catch (LibrarySaveException e) {
            log.debug( "NewProject exception: " + e.getLocalizedMessage() );
        }
        if (p != null) {
            try {
                op = new OtmProject( p, modelManager );
                op.setDefaultContextId( defaultContextId );
                // These values are stored in the TLProject
                // op.setDescription( description );
                // op.setName( name );

                // Saving project causes file write of contents
                tlProjectManager.saveProject( p );

                // register project in projects map
                if (projects.get( name ) != null)
                    log.warn( "Already had project with same name." );
                projects.put( name, op );
            } catch (IllegalArgumentException e) {
                log.warn( "Exception creating project: " + e.getLocalizedMessage() );
                throw new IllegalArgumentException( "Could not create valid project: " + e.getLocalizedMessage() );
            }
        }
        return op;
    }


    /**
     * See {@link ProjectManager}
     * 
     * @return compiler/repository/tlModel's project manager
     */
    public ProjectManager getTLProjectManager() {
        return tlProjectManager;
    }

    /**
     * Publish library into passed repository. Creates new library when successful.
     * <p>
     * Used by ManageLibraryTask
     * 
     * @param library
     * @param repository
     * @throws DexProjectException
     */
    public OtmMajorLibrary publish(OtmLocalLibrary library, Repository repository) throws DexProjectException {
        // Pre-checks
        if (library == null || library.getTL() == null || library.getModelManager() == null)
            throw new DexProjectException( "Missing library information." );
        if (!(library instanceof OtmLocalLibrary))
            throw new DexProjectException( "Library must be an unmanged, local library." );
        if (repository == null)
            throw new DexProjectException( "Missing repository to publish into." );
        if (tlProjectManager == null)
            throw new DexProjectException( "Missing TL Project Manager." );

        // Get and check the project item
        ProjectItem item = getProjectItem( library.getTL() ); // Throws exception

        // Publish the item
        try {
            tlProjectManager.publish( item, repository );
        } catch (RepositoryException | PublishWithLocalDependenciesException e) {
            throw new DexProjectException( "Project Publish Exception: " + e.getLocalizedMessage() );
        }

        // Post-check
        if (item.getState() == RepositoryItemState.UNMANAGED)
            throw new DexProjectException( "Library was not managed in repository." );

        // Create new library for the item and inform the model manager
        // OtmMajorLibrary newLib = OtmLibraryFactory.newLibrary( library );
        // return newLib;
        return OtmLibraryFactory.newLibrary( library );
    }

    /**
     * Get the project item for the passed library from the TL Project Manager.
     * 
     * @param tlLib
     * @return item
     * @throws DexProjectException if not found or state is not UNMANAGED
     */
    public ProjectItem getProjectItem(AbstractLibrary tlLib) throws DexProjectException {
        // Get and check the project item
        ProjectItem item = tlProjectManager.getProjectItem( tlLib );
        if (item == null)
            throw new DexProjectException( "Missing project item information." );
        if (item.getState() != RepositoryItemState.UNMANAGED)
            throw new DexProjectException( "Library item is already managed in repository." );
        return item;
    }
}
