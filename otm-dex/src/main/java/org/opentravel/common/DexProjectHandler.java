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
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.controllers.popup.SelectProjectDialogController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<OtmProject> projects = library.getModelManager().getProjects();

        // Remove projects the library is already in
        library.getProjects().forEach( projects::remove );
        // library.getProjects().forEach( p -> projects.remove( p ) );

        OtmProject project = selectOneProject( projects );

        // Add the library to the selected project
        if (project != null)
            try {
                project.add( library );
            } catch (Exception e) {
                log.warn( "Could not add library to project: " + e.getLocalizedMessage() );
                DialogBoxContoller dbc = DialogBoxContoller.init();
                if (dbc != null)
                    dbc.show( "Add Library Error", e.getLocalizedMessage() );
            }
    }

    /**
     * Get a list of projects from the model manager and let the user select one.
     * 
     * @param modelManager
     * @return user selected project or null
     */
    public OtmProject selectProject(OtmModelManager modelManager) {
        List<OtmProject> projects = modelManager.getProjects();
        SelectProjectDialogController spdc = SelectProjectDialogController.init();
        spdc.setProjectList( projects );
        spdc.showAndWait( "" );
        return spdc.getSelection();
    }

    /**
     * Remove the libraries in the list from the user selected project. If libraries are only in one project, remove
     * from that project.
     * 
     * @param libraries
     */
    public void remove(List<OtmLibrary> libraries) {
        // Select one project from all that any library is in
        OtmProject project = null;
        Set<OtmProject> ps = new HashSet<>();
        libraries.forEach( l -> ps.addAll( l.getProjects() ) );
        List<OtmProject> projects = new ArrayList<>( ps );

        if (projects.size() == 1)
            project = projects.get( 0 );
        else if (projects.size() > 1)
            project = selectOneProject( projects );

        if (project != null)
            project.remove( libraries );
    }

    /**
     * Remove the library from the project the user selects.
     * 
     * @param library
     */
    public void removeLibrary(OtmLibrary library) {
        // Select one project
        List<OtmProject> projects = library.getProjects();
        OtmProject project = null;
        project = selectOneProject( projects );
        // Remove the library from selected project
        if (project != null)
            project.remove( library );
    }

    /**
     * User selection of project. If there is more than one project in the list, prompt the user to select one.
     * 
     * @param list of projects to select from
     * @return user selected project, the only project in the list or null
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
