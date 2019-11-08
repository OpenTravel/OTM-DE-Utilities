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
import org.opentravel.application.common.DirectoryChooserDelegate;
import org.opentravel.application.common.FileChooserDelegate;
import org.opentravel.application.common.StatusType;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javafx.stage.Stage;

/**
 * @author dmh
 *
 */
public class DexFileHandler extends AbstractMainWindowController {
    private static Log log = LogFactory.getLog( DexFileHandler.class );

    public static final String PROJECT_FILE_EXTENSION = ".otp";
    public static final String LIBRARY_FILE_EXTENSION = ".otm";
    public static final String FILE_SEPARATOR = "/";

    ValidationFindings findings = null;

    /**
     * Return a file selected by the user. Save the directory in the user settings.
     * 
     */
    public File fileChooser(Stage stage, UserSettings settings) {
        // Get the last directory used from settings
        File initialDirectory = settings.getLastProjectFolder();

        // Let user choose a file
        FileChooserDelegate chooser = newFileChooser( "Open", initialDirectory, OTP_EXTENSION_FILTER,
            OTR_EXTENSION_FILTER, OTM_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
        File selectedFile = chooser.showOpenDialog( stage );

        // Save the directory used in user settings.
        if (selectedFile != null) {
            settings.setLastProjectFolder( selectedFile.getParentFile() );
            settings.save();
        }

        return selectedFile;
    }

    /**
     * Return a directory selected by the user. Save the directory in the user settings.
     * 
     */
    public File directoryChooser(Stage stage, String title, UserSettings settings) {
        // Get the last directory used from settings
        File initialDirectory = settings.getLastProjectFolder();

        // Let user choose
        DirectoryChooserDelegate chooser = newDirectoryChooser( title, initialDirectory );
        File selectedFile = chooser.showDialog( stage );

        // Save the directory used in user settings.
        if (selectedFile != null) {
            settings.setLastProjectFolder( selectedFile.getParentFile() );
            settings.save();
        }

        return selectedFile;
    }

    /**
     * Let user choose a directory
     * 
     * @param stage
     * @param title
     * @param initialDirectory
     * @return
     */
    public File directoryChooser(Stage stage, String title, String initialDirectory) {
        File initialFile = new File( initialDirectory );
        DirectoryChooserDelegate chooser = newDirectoryChooser( title, initialFile );
        return chooser.showDialog( stage );
    }

    /**
     * @return a list of OTM Project files
     */
    public File[] getProjectList(File directory) {
        if (directory == null) {
            directory = new File( getUserHome() );
            log.warn( "Used user home directory. Should have used directory from preferences." );
        }
        File[] projectFiles = {};
        if (directory.isDirectory()) {
            projectFiles = directory.listFiles( f -> f.getName().endsWith( PROJECT_FILE_EXTENSION ) );
        }
        return projectFiles;
    }

    public static String getUserHome() {
        return System.getProperty( "user.home" );
    }

    /**
     * Open library or project file using library model loader. Results in an updated model.
     * <p>
     * To open a project and receive the project manager, use
     * {@link #openProject(File, TLModel, OpenProjectProgressMonitor)}
     *
     * @param selectedFile
     */
    public ValidationFindings openFile(File selectedFile, OtmModelManager modelManager,
        OpenProjectProgressMonitor monitor) {
        findings = null;
        if (selectedFile != null && selectedFile.canRead()) {
            if (selectedFile.getName().endsWith( PROJECT_FILE_EXTENSION ))
                openProject( selectedFile, modelManager, monitor );
            else if (selectedFile.getName().endsWith( LIBRARY_FILE_EXTENSION ))
                findings = openLibrary( selectedFile, modelManager.getTlModel(), monitor );
        }
        return findings;
    }

    // TODO - should be private or protected
    public ValidationFindings openLibrary(File selectedFile, TLModel libraryModel, OpenProjectProgressMonitor monitor) {
        if (selectedFile == null)
            return null;
        if (!selectedFile.canRead()) {
            log.debug( "Can't read file: " + selectedFile.getAbsolutePath() );
            return null;
        }
        log.debug( "Open selected file: " + selectedFile.getName() );
        findings = null;

        // Assure OTM library file
        if (selectedFile.getName().endsWith( ".otm" )) {
            LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( selectedFile );
            try {
                LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>( libraryModel );
                findings = modelLoader.loadLibraryModel( libraryInput );
            } catch (LibraryLoaderException e) {
                log.error( "Error loading model: " + e.getLocalizedMessage() );
            }
        } else {
            log.debug( "Invalid file extension: " + selectedFile.getName() );
        }

        return findings;
    }

    public boolean openProject(File selectedProjectFile, OtmModelManager mgr, OpenProjectProgressMonitor monitor) {
        if (selectedProjectFile.getName().endsWith( PROJECT_FILE_EXTENSION )) {
            // Use project manager from TLModel
            ProjectManager manager = mgr.getProjectManager();
            try {
                manager.loadProject( selectedProjectFile, findings, monitor );
            } catch (Exception e) {
                log.error( "Error Opening Project: " + e.getLocalizedMessage() );
                manager.closeAll();
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public ProjectManager openProject(File selectedProjectFile, TLModel tlModel, OpenProjectProgressMonitor monitor) {
        // Use project manager from TLModel
        ProjectManager manager = null;
        if (selectedProjectFile.getName().endsWith( ".otp" )) {
            if (tlModel != null)
                manager = new ProjectManager( tlModel );
            else
                manager = new ProjectManager( false );
            // Findings are created in back ground task - is there any way to use these instead?
            findings = new ValidationFindings();
            try {
                manager.loadProject( selectedProjectFile, findings, monitor );
                // } catch (LibraryLoaderException | RepositoryException | NullPointerException e) {
                // log.error( "Error opening project: " + e.getLocalizedMessage() );
            } catch (Exception e) {
                log.error( "Error Opening Project: " + e.getLocalizedMessage() );
            }
        }
        return manager;
    }

    public static String saveLibraries(List<OtmLibrary> libraries) {
        if (libraries == null || libraries.isEmpty())
            return "No libraries to save.";

        final LibraryModelSaver lms = new LibraryModelSaver();
        final StringBuilder successfulSaves = new StringBuilder();
        final StringBuilder errorSaves = new StringBuilder();
        final ValidationFindings findings = new ValidationFindings();
        for (final OtmLibrary library : libraries) {
            if (library.isEditable() && library.getTL() instanceof TLLibrary) {
                final TLLibrary tlLib = (TLLibrary) library.getTL();
                final String libraryName = library.getName();
                final URL libraryUrl = tlLib.getLibraryUrl();

                try {
                    log.debug( "Saving library: " + libraryName + " " + libraryUrl );
                    findings.addAll( lms.saveLibrary( tlLib ) );
                    successfulSaves.append( "\n" ).append( libraryName ).append( " (" ).append( libraryUrl )
                        .append( ")" );
                } catch (final LibrarySaveException e) {
                    final Throwable t = e.getCause();
                    errorSaves.append( "\n" ).append( libraryName ).append( " (" ).append( libraryUrl ).append( ")" )
                        .append( " - " ).append( e.getMessage() );
                    if (t != null && t.getMessage() != null) {
                        errorSaves.append( " (" ).append( t.getMessage() ).append( ")" );
                    }
                }
            }
        }
        return buildSaveResults( successfulSaves, errorSaves );
    }

    private static String buildSaveResults(StringBuilder successfulSaves, StringBuilder errorSaves) {
        // Return the results
        final StringBuilder userMessage = new StringBuilder();
        if (successfulSaves.length() > 0)
            userMessage.append( "Successfully saved:" ).append( successfulSaves ).append( "\n\n" );

        if (errorSaves.length() > 0)
            userMessage.append( "Failed to save:" ).append( errorSaves ).append( "\n\n" )
                .append( "You may need to use the .bak file to restore your work" );

        return userMessage.toString();
    }

    /**
     * 
     * @param libraryFile must be created and writable
     * @return
     */
    public static TLLibrary createLibrary(File libraryFile) {
        if (libraryFile == null || !libraryFile.canWrite())
            return null;

        final URL fileURL = URLUtils.toURL( libraryFile );
        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus( TLLibraryStatus.DRAFT );
        tlLib.setLibraryUrl( fileURL );
        return tlLib;
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
