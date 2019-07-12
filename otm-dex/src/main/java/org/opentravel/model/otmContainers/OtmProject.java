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
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

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

    public OtmProject(Project project) {
        this.tlProject = project;
        id = getTL().getProjectId();
    }

    public String toString() {
        return getName();
    }

    public void remove(OtmLibrary library) {
        getTL().remove( library.getTL() );
    }

    public void add(OtmLibrary library) {
        if (library != null) {
            try {
                ProjectItem pi = getTL().getProjectManager().addUnmanagedProjectItem( library.getTL(), getTL() );
                library.add( pi ); // let the library know it is now part of this project
            } catch (RepositoryException e) {
                log.warn( "Could not add library to project: " + e.getLocalizedMessage() );
            }
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
}
