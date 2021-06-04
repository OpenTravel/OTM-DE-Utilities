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
import org.opentravel.common.DexLibraryException;
import org.opentravel.common.DexProjectException;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.repository.NamespaceLibrariesRowFactory;
import org.opentravel.dex.tasks.repository.VersionLibraryTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

import java.util.List;

/**
 * OTM Object Node for business objects. Project does NOT extend model element
 * 
 * @author Dave Hollander
 * 
 */
public class OtmProject {
    private static Log log = LogFactory.getLog( OtmProject.class );

    Project tlProject;
    // private String id;
    private OtmModelManager modelManager;

    public OtmProject(Project project, OtmModelManager modelManager) {
        this.modelManager = modelManager;
        this.tlProject = project;
        // id = getTL().getProjectId();
    }

    public String toString() {
        return getName();
    }

    public String getDescription() {
        return getTL().getDescription();
    }

    public String getProjectId() {
        return getTL().getProjectId();
    }

    // /**
    // * Used by ManageLibraryTask
    // *
    // * @param library
    // * @param repository
    // * @throws DexProjectException
    // */
    // @Deprecated
    // public OtmMajorLibrary publish(OtmLibrary library, Repository repository) throws DexProjectException {
    // return getModelManager().getOtmProjectManager().publish( library, repository );
    // // if (getTL() == null || getTL().getProjectManager() == null)
    // // throw new DexProjectException( "Missing Project information." );
    // // if (library == null || library.getTL() == null || library.getModelManager() == null)
    // // throw new DexProjectException( "Missing library information." );
    // // if (repository == null)
    // // throw new DexProjectException( "Missing repository information." );
    // // if (!(library instanceof OtmLocalLibrary))
    // // throw new DexProjectException( "Library must be an unmanged, local library." );
    // //
    // // TLLibrary tlLib = (TLLibrary) library.getTL();
    // // // Repository repo = library.getProjectItem().getRepository();
    // // ProjectItem item = getProjectItem( tlLib );
    // // if (item == null)
    // // throw new DexProjectException( "Missing project item information." );
    // //
    // // // ?? Is this the same as library.getProjectItem() ??
    // // // FIXME - Shouldn't that be tested elsewhere?
    // // ProjectItem i2 = library.getProjectItem();
    // // if (item != i2)
    // // log.debug( "Project items are different. " );
    // //
    // // if (item.getState() != RepositoryItemState.UNMANAGED)
    // // throw new DexProjectException( "Library item is already managed in repository." );
    // //
    // // ProjectManager pm = getTL().getProjectManager();
    // // try {
    // // pm.publish( item, repository );
    // // } catch (RepositoryException | PublishWithLocalDependenciesException e) {
    // // throw new DexProjectException( "Project Publish Exception: " + e.getLocalizedMessage() );
    // // }
    // //
    // // if (item.getState() == RepositoryItemState.UNMANAGED)
    // // throw new DexProjectException( "Library was not managed in repository." );
    // //
    // // // Create new library for the item and inform the model manager
    // // OtmMajorLibrary newLib = OtmLibraryFactory.newLibrary( (OtmLocalLibrary) library );
    // // return newLib;
    // }


    public void remove(List<OtmLibrary> libraries) {
        libraries.forEach( l -> remove( l, false ) );
        save();
    }

    public void remove(OtmLibrary library) {
        remove( library, true );
    }

    public void remove(OtmLibrary library, boolean save) {
        // Remove this project from the library's list
        ProjectItem pi = getProjectItem( library );
        if (pi == null)
            log.warn( "Missing PI for this library " + library );

        getTL().remove( library.getTL() );
        // log.debug( "Removed " + library.getName() + " from " + getName() + " Library now is in "
        // + library.getProjects().size() + " projects." );

        // Shouldn't be needed, but is required to change file as of 7/16/2019
        if (save)
            save();

        library.remove( pi );
        // Note - no check to see if there are any projects that own this library.

        // // Test only safety check
        // for (ProjectItem pi : getTL().getProjectItems())
        // assert pi.getContent() != library.getTL();
    }

    public void save() {
        try {
            getTL().getProjectManager().saveProject( getTL() );
        } catch (LibrarySaveException e) {
            log.error( "Error saving project: " + e.getLocalizedMessage() );
        }
    }

    /**
     * From the project items maintained in the library, return the first one the TLProject contains.
     * 
     * @param lib
     * @return
     */
    public ProjectItem getProjectItem(OtmLibrary lib) {
        for (ProjectItem pi : lib.getProjectItems())
            if (getTL().getProjectItems().contains( pi ))
                return pi;
        return null;
    }

    /**
     * @deprecated use add(OtmLocalLibrary)
     * 
     * @param library must be an unmanaged library
     * @throws DexProjectException
     */
    @Deprecated
    public ProjectItem add(OtmLibrary library) throws DexProjectException {
        ProjectItem pi = null;
        // try {
        if (library instanceof OtmLocalLibrary)
            pi = add( (OtmLocalLibrary) library );
        else if (library.getProjectItem() instanceof RepositoryItem) {
            // OtmManagedLibrary newLib = addManaged( library.getProjectItem() );
            // return newLib.getProjectItem();
            try {
                pi = modelManager.getProjectManager().addUnmanagedProjectItem( library.getTL(), getTL() );
            } catch (RepositoryException e) {
                throw new DexProjectException( "Project manager exception: " + e.getLocalizedMessage() );
            }
            library.add( pi ); // let the library know it is now part of this project
        }
        // } catch (DexProjectException e) {
        // log.warn( "Error adding library to project. " + e.getLocalizedMessage() );
        // }
        return pi;
    }

    @Deprecated
    public void addOLD(OtmLibrary library) throws RepositoryException {
        if (library != null) {
            ProjectItem pi = null;
            // if (library.isUnmanaged()) {
            // use modelManager's projectManager
            pi = modelManager.getProjectManager().addUnmanagedProjectItem( library.getTL(), getTL() );
            // } else {
            // FIXME - find out how to the the repoItem! This does not work as is.
            // String itemUri = library.getProjectItem().toURI().toString();
            // RepositoryItem repoItem = library.getProjectItem().getRepository().getRepositoryItem( itemUri );
            // pi = getTL().getProjectManager().addManagedProjectItem( repoItem, getTL() );
            // // List<RepositoryItem> repoItems = null;
            // // library.getProjectItem();
            // // List<ProjectItem> pis = modelManager.getProjectManager().addManagedProjectItems( repoItems, getTL()
            // // );
            // }
            library.add( pi ); // let the library know it is now part of this project
            log.debug( "Added " + library.getName() + " to " + getName() );
        }
    }

    /**
     * Add the managedPI to the TL Project using the TLProjectManager.
     * <p>
     * Intended for use in {@link NamespaceLibrariesRowFactory} and {@link VersionLibraryTask#doIT()}
     * 
     * @param managedPI must be a RepositoryItem
     * @return the new library with new managed PI
     * @throws DexProjectException
     */
    // // TODO - change signature to require RepositoryItem ?
    public OtmManagedLibrary addManaged(RepositoryItem managedPI) throws DexProjectException {
        // // return addManaged( (ProjectItem) managedPI );
        // // }
        //
        // public OtmManagedLibrary addManaged(ProjectItem managedPI) throws DexProjectException {
        ProjectManager pm = getModelManager().getProjectManager();
        if (pm == null)
            throw new DexProjectException( "Missing TL Project manager." );
        if (managedPI.getState() == RepositoryItemState.UNMANAGED)
            throw new DexProjectException( "Project item must be managed in repository." );
        if (!(managedPI instanceof RepositoryItem))
            throw new DexProjectException( "Project item must be managed in repository." );

        // Creates a new PI with new TLLib created by LibraryLoader
        ProjectItem newPI = null;
        try {
            newPI = pm.addManagedProjectItem( managedPI, getTL() );
        } catch (LibraryLoaderException | RepositoryException e) {
            throw new DexProjectException( "Exception managing project item: " + e.getLocalizedMessage() );
        }

        // Create new OTM Managed library
        OtmManagedLibrary mLib = null; // Return value
        OtmLibrary lib = null;
        try {
            lib = OtmLibraryFactory.newLibrary( newPI, getModelManager() );
            if (lib instanceof OtmManagedLibrary) {
                mLib = (OtmManagedLibrary) lib;
                getModelManager().addToMaps( mLib );
            }
        } catch (DexLibraryException e) {
            throw new DexProjectException( "Exception building OTM library: " + e.getLocalizedMessage() );
        }
        return mLib;
    }

    /**
     * TL: This method assumes that the library is an uncontrolled artifact(not currently under repository control), and
     * has already been incorporated into the existing model that is maintained by this ProjectManager.
     * 
     * @param library must be an unmanaged, local library
     * @throws DexProjectException
     */
    public ProjectItem add(OtmLocalLibrary library) throws DexProjectException {
        // Pre-checks
        if (library == null)
            throw new DexProjectException( "Missing library to add to project." );
        TLModel tlModel = modelManager.getProjectManager().getModel();
        if (tlModel == null || !tlModel.getUserDefinedLibraries().contains( library.getTL() ))
            throw new DexProjectException( "TLModel does not contain the library." );

        ProjectItem pi = null;
        try {
            pi = modelManager.getProjectManager().addUnmanagedProjectItem( library.getTL(), getTL() );
        } catch (RepositoryException e) {
            throw new DexProjectException( "Project manager exception: " + e.getLocalizedMessage() );
        }
        // } else {
        // FIXME - expose this for OtmManagedLibrary
        // ProjectItem newPI = pm.addManagedProjectItem( item, proj.getTL() );

        // You can't! - find out how to the the repoItem! This does not work as is.
        // String itemUri = library.getProjectItem().toURI().toString();
        // RepositoryItem repoItem = library.getProjectItem().getRepository().getRepositoryItem( itemUri );
        // pi = getTL().getProjectManager().addManagedProjectItem( repoItem, getTL() );
        // // List<RepositoryItem> repoItems = null;
        // // library.getProjectItem();
        // // List<ProjectItem> pis = modelManager.getProjectManager().addManagedProjectItems( repoItems, getTL()
        // // );
        // }
        library.add( pi ); // let the library know it is now part of this project

        // Debugging post checks - see TestProject
        if (getTL().getProjectManager().getProjectItem( library.getTL() ) == null)
            throw new DexProjectException( "Library was not added to TL project manager." );
        if (getProjectItem( library.getTL() ) == null)
            throw new DexProjectException( "Library was not added to TL project." );
        if (getProjectItem( library ) == null)
            throw new DexProjectException( "Library was not added to project manager." );
        // if (modelManager.getManagingProject( library ) == null)
        // throw new DexProjectException( "Model manager can't find managing project even though it was just added." );

        log.debug( "Added " + library.getName() + " to " + getName() );
        return pi;
    }

    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
    }

    /**
     * Simply return the model managed used to create this project.
     * 
     * @return
     */
    public OtmModelManager getModelManager() {
        return modelManager;
    }

    public String getName() {
        return this.getTL().getName();
    }

    public Project getTL() {
        return tlProject;
    }

    public RepositoryManager getRepositoryManager() {
        return tlProject.getProjectManager().getRepositoryManager();
    }

    /**
     * Get the permission from this project's repository manager.
     * 
     * @return
     */
    public RepositoryPermission getPermission() {
        try {
            return getRepositoryManager().getUserAuthorization( getTL().getProjectId() );
        } catch (RepositoryException e) {
            log.error( "Could not get permission for " + this + " project: " + e.getLocalizedMessage() );
        }
        return null;
    }

    /**
     * Get all ProjectItems from this TL Project. Return the first one whose content is the library.
     * 
     * @param tlLib
     * @return
     */
    public ProjectItem getProjectItem(AbstractLibrary tlLib) {
        for (ProjectItem pi : getTL().getProjectItems())
            if (pi.getContent() == tlLib)
                return pi;
        return null;
    }

    /**
     * Close the project in the TL Project manager and OTM model manager.
     */
    public void close() {
        getTL().getProjectManager().closeProject( getTL() );
        modelManager.getOtmProjectManager().close( this );
        // modelManager.close( this );
    }

    /**
     * Does this project contain the library?
     * 
     * @param library
     * @return
     */
    // OTM-DE used projects to manage write access to libraries. DEX does not.
    public boolean contains(AbstractLibrary tlLib) {
        for (ProjectItem pi : getTL().getProjectItems()) {
            if (pi.getContent().equals( tlLib ))
                return true;
        }
        return false;
    }

    public void setDefaultContextId(String defaultContextId) {
        getTL().setDefaultContextId( emptyIfNull( defaultContextId ) );
    }

    public void setDescription(String description) {
        getTL().setDescription( emptyIfNull( description ) );
    }

    public void setName(String name) {
        getTL().setName( emptyIfNull( name ) );
    }

    private String emptyIfNull(String param) {
        return param == null ? "" : param;
    }


}
