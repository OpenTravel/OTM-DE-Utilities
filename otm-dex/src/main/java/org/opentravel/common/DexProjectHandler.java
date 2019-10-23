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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.popup.SelectProjectDialogController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;

import java.util.List;

/**
 * Handle requests for project related services.
 * 
 * @author dmh
 *
 */
public class DexProjectHandler {
    private static Log log = LogFactory.getLog( DexProjectHandler.class );

    // public DexProjectHandler() {}


    /**
     * Add the library to the project selected by the user.
     * 
     * @param library
     */
    public void addToProject(OtmLibrary library) {
        List<OtmProject> projects = library.getModelManager().getUserProjects();

        // Remove projects the library is already in
        library.getProjects().forEach( p -> projects.remove( p ) );

        OtmProject project = selectOneProject( projects );

        // Add the library to the selected project
        if (project != null)
            project.add( library );
    }

    /**
     * Get a list of projects from the model manager and let the user select one.
     * 
     * @param modelManager
     * @return user selected project or null
     */
    public OtmProject selectProject(OtmModelManager modelManager) {
        List<OtmProject> projects = modelManager.getUserProjects();
        SelectProjectDialogController spdc = SelectProjectDialogController.init();
        spdc.setProjectList( projects );
        spdc.showAndWait( "" );
        return spdc.getSelection();
    }

    /**
     * Remove the library from the project the user selects.
     * 
     * @param library
     */
    // FIXME - doen't seem to do anything
    public void removeLibrary(OtmLibrary library) {
        // Select one project
        List<OtmProject> projects = library.getProjects();
        OtmProject project = null;
        // if (projects.isEmpty())
        // return;
        project = selectOneProject( projects );
        // if (projects.size() <= 1)
        // project = projects.get( 0 );
        // else {
        // // post selection dialog
        // SelectProjectDialogController spdc = SelectProjectDialogController.init();
        // spdc.setProjectList( projects );
        // spdc.showAndWait( "" );
        // project = spdc.getSelection();
        //
        // }
        // Remove the library from selected project
        if (project != null)
            project.remove( library );
    }

    /**
     * @param list of projects to select from
     * @return
     */
    public OtmProject selectOneProject(List<OtmProject> projects) {
        // Select one project
        OtmProject project = null;
        if (projects == null || projects.isEmpty())
            return null;

        if (projects.size() <= 1)
            for (OtmProject p : projects)
                project = p;
        else {
            // post a dialog to select the project
            SelectProjectDialogController spdc = SelectProjectDialogController.init();
            spdc.setProjectList( projects );
            spdc.showAndWait( "" );
            if (spdc.getSelection() != null)
                project = spdc.getSelection();
        }
        return project;
    }
}
