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
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.FileChooserDelegate;
import org.opentravel.application.common.StatusType;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;

import javafx.stage.Stage;

/**
 * @author dmh
 *
 */
public class DexFileHandler extends AbstractMainWindowController {
    private static Log log = LogFactory.getLog( DexFileHandler.class );

    ValidationFindings findings;

    /**
     * Return a file selected by the user. Save the directory in the user settings.
     * 
     */
    public File fileChooser(Stage stage, UserSettings settings) {
        // Get the last directory used from settings
        File initialDirectory = settings.getLastProjectFolder();

        // Let user choose a file
        FileChooserDelegate chooser = newFileChooser( "Import from OTP", initialDirectory, OTP_EXTENSION_FILTER,
            OTR_EXTENSION_FILTER, OTM_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
        File selectedFile = chooser.showOpenDialog( stage );

        // Save the directory used in user settings.
        if (selectedFile != null) {
            settings.setLastProjectFolder( selectedFile.getParentFile() );
            settings.save();
        }

        return selectedFile;
    }

    // public ValidationFindings getFindings() {
    // return findings;
    // }

    /**
     * @return a list of OTM Project files
     */
    public File[] getProjectList(File directory) {
        // List<File> projectFiles = new ArrayList<>();
        if (directory == null) {
            directory = new File( System.getProperty( "user.home" ) );
        }
        File[] projectFiles = {};
        if (directory.isDirectory()) {
            projectFiles = directory.listFiles( f -> f.getName().endsWith( ".otp" ) );
        }
        log.warn( "TODO - get directory from preferences." );
        return projectFiles;
    }

    // /**
    // * Open the passed project with the project manager.
    // * <p>
    // * Open library file using library model loader
    // *
    // * @param selectedFile
    // */
    // public void openFile(File selectedFile) {
    // if (selectedFile == null)
    // return;
    // log.debug("Open selected file: " + selectedFile.getName());
    //
    // if (selectedFile.getName().endsWith(".otp")) {
    // ProjectManager manager = openProject(selectedFile, null);
    // newModel = manager.getModel();
    // } else { // assume OTM library file
    // LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(selectedFile);
    // try {
    // LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>();
    //
    // findings = modelLoader.loadLibraryModel(libraryInput);
    // newModel = modelLoader.getLibraryModel();
    // } catch (LibraryLoaderException e) {
    // log.error("Error loading model: " + e.getLocalizedMessage());
    // // e.printStackTrace();
    // }
    // }
    // }

    public ProjectManager openProject(File selectedProjectFile, TLModel tlModel, OpenProjectProgressMonitor monitor) {
        // Use project manager from TLModel
        ProjectManager manager;
        if (tlModel != null)
            manager = new ProjectManager( tlModel );
        else
            manager = new ProjectManager( false );
        // Findings are created in back ground task - is there any way to use these instead?
        findings = new ValidationFindings();
        try {
            manager.loadProject( selectedProjectFile, findings, monitor );
        } catch (LibraryLoaderException | RepositoryException | NullPointerException e) {
            log.error( "Error opening project: " + e.getLocalizedMessage() );
        }
        return manager;
    }

    @Override
    protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
        // Inherited status message not used.
    }

    @Override
    protected void updateControlStates() {
        // TODO
    }

}
