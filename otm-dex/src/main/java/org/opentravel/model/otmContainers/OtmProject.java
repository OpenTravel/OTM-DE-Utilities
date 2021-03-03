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
import org.opentravel.model.OtmModelManager;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
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
    private String id;
    private OtmModelManager modelManager;

    public OtmProject(Project project, OtmModelManager modelManager) {
        this.modelManager = modelManager;
        this.tlProject = project;
        id = getTL().getProjectId();
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

    public ProjectItem getProjectItem(OtmLibrary lib) {
        for (ProjectItem pi : lib.getProjectItems())
            if (getTL().getProjectItems().contains( pi ))
                return pi;
        return null;
    }

    public void add(OtmLibrary library) throws RepositoryException {
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

    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
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

    public RepositoryPermission getPermission() {
        try {
            return getRepositoryManager().getUserAuthorization( getTL().getProjectId() );
        } catch (RepositoryException e) {
            log.error( "Could not get permission for " + this + " project: " + e.getLocalizedMessage() );
        }
        return null;
    }

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
