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
import org.opentravel.model.OtmModelNamespaceManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
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

    // ValidationFindings findings = null;

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
     * Throw exception if the file can not be written to for any reason.
     * 
     * @param file
     * @throws DexFileException
     */
    private static void checkCanRead(File file) throws DexFileException {
        String path = "";
        try {
            path = file.getCanonicalPath();
        } catch (SecurityException se) {
            throw new DexFileException( "Access denied to file: " + file + " because " + se.getLocalizedMessage() );
        } catch (IOException e1) {
            throw new DexFileException( "Error checking file: " + file + " because " + e1.getLocalizedMessage() );
        }
        try {
            if (!file.canRead()) {
                throw new DexFileException( "File can not be written to. " + path );
            }
        } catch (SecurityException e) {
            throw new DexFileException( "Access denied to write to " + file + " because " + e.getLocalizedMessage() );
        }
    }

    /**
     * Throw exception if the file can not be written to for any reason.
     * 
     * @param file
     * @throws DexFileException
     */
    private static void checkCanWrite(File file) throws DexFileException {
        String path = "";
        try {
            path = file.getCanonicalPath();
        } catch (SecurityException se) {
            throw new DexFileException( "Access denied to file: " + file + " because " + se.getLocalizedMessage() );
        } catch (IOException e1) {
            throw new DexFileException( "Error checking file: " + file + " because " + e1.getLocalizedMessage() );
        }
        try {
            if (!file.canWrite()) {
                throw new DexFileException( "File can not be written to. " + path );
            }
        } catch (SecurityException e) {
            throw new DexFileException( "Access denied to write to " + file + " because " + e.getLocalizedMessage() );
        }

    }

    // // FUTURE?
    // public void cleanUp(Path path) {
    // Files.delete( path );
    public void cleanUp(File file) throws DexFileException {
        try {
            Files.delete( file.toPath() );
        } catch (IOException e) {
            throw new DexFileException( "Could not delete " + file.toPath() + " because " + e.getLocalizedMessage() );
        }
    }

    /**
     * Check directory to assure it is valid and write-able
     * 
     * @param dirName used to create directory file
     * @param fullPath only used to create exception message if file IO exception
     * @throws DexFileException if directory is not writable
     */
    public static void checkDir(String dirName, String fullPath) throws DexFileException {
        if (dirName == null || dirName.isEmpty())
            throw new DexFileException( "Missing directory name." );

        File tmpDir = new File( dirName );
        String cPath = "";

        try {
            cPath = tmpDir.getCanonicalPath();
        } catch (IOException e) {
            throw new DexFileException( "Error accessing directory: " + fullPath + "\n" + e.getLocalizedMessage() );
        }
        if (!tmpDir.isDirectory())
            throw new DexFileException( "Invalid directory: " + cPath );
        if (!tmpDir.canWrite())
            throw new DexFileException( "Can't create file in the directory: " + cPath );
    }

    /**
     * Create write-able file in file system.
     * 
     * @param pathName
     * @return the file created
     * @throws DexFileException
     */
    protected static File createFile(String pathName) throws DexFileException {
        File libraryFile = new File( pathName );
        try {
            if (!libraryFile.createNewFile()) {
                throw new DexFileException(
                    "Could not create new library file: " + libraryFile.getPath() + " already exists." );
            }
        } catch (SecurityException se) {
            throw new DexFileException(
                "Access denied while creating library file: " + pathName + " because " + se.getLocalizedMessage() );
        } catch (IOException e1) {
            throw new DexFileException(
                "Error creating library file: " + pathName + " because " + e1.getLocalizedMessage() );
        }
        checkCanWrite( libraryFile ); // Throws exception
        return libraryFile;
    }

    /**
     * Create a TLLibrary whose URL is the passed File.
     * 
     * @param libraryFile must be created and writable
     * @return
     */
    protected static TLLibrary createLibrary(File libraryFile) throws DexFileException {
        if (libraryFile == null)
            throw new DexFileException( "Missing library file." );
        checkCanWrite( libraryFile ); // Throws exception

        final URL fileURL;
        try {
            fileURL = URLUtils.toURL( libraryFile );
        } catch (Exception e) {
            throw new DexFileException( "Invalid URL: " + e.getLocalizedMessage() );
        }
        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus( TLLibraryStatus.DRAFT );
        tlLib.setLibraryUrl( fileURL );
        return tlLib;
    }

    /**
     * Create an .OTM file, a TLLibrary and OTMLibrary. Add the OTMLibrary to the model manager. Add the TLLibrary to
     * the TLModel.
     * 
     * @param filename to create. Must be write-able and not exist. ".otm" will be added if missing.
     * @param nsValue namespace for the library. Version suffix will be created if necessary.
     * @param nameValue name of the library
     * @param modelMgr add new unmanaged library and provide access to namespace prefixes and TLModel
     * @return newly created OTMLibrary
     * @throws DexFileException
     */
    public static OtmLocalLibrary createLibrary(String filename, String nsValue, String nameValue,
        OtmModelManager modelMgr) throws DexFileException {
        if (modelMgr == null)
            throw new DexFileException( "Internal Error: model manager is null." );

        OtmLibrary otmLibrary = null; // return value
        String namespace = OtmModelNamespaceManager.fixNamespaceVersion( nsValue );
        String prefix = new OtmModelNamespaceManager( modelMgr ).getPrefix( namespace );

        // Create the file
        File libraryFile = null;
        if (!filename.endsWith( ".otm" ))
            filename += ".otm";
        libraryFile = createFile( filename ); // throws exception

        // Create the TL Library
        AbstractLibrary absLib = createLibrary( libraryFile ); // throws exception
        if (!(absLib instanceof TLLibrary))
            throw new DexFileException( "Create library did not return a TLLibrary." );
        TLLibrary tlLib = (TLLibrary) absLib;
        tlLib.setOwningModel( modelMgr.getTlModel() );
        tlLib.setName( nameValue );
        tlLib.setNamespace( namespace );
        tlLib.setPrefix( prefix );
        tlLib.setComments( "" );

        // Create an OTM library and add to model manager
        otmLibrary = modelMgr.addLibrary( tlLib );
        if (otmLibrary instanceof OtmLocalLibrary)
            return (OtmLocalLibrary) otmLibrary;
        else {
            log.warn( "Invalid library type created." );
        }
        return null;
    }

    public static String getDefaultProjectFolder(UserSettings settings) {
        String folder = "";
        if (settings != null && settings.getLastProjectFolder() != null)
            folder = settings.getLastProjectFolder().getPath();
        if (folder == null || folder.isEmpty())
            folder = getUserHome();
        return folder;
    }

    /**
     * @return a list of OTM Project files from a directory
     */
    public static File[] getProjectList(File directory) {
        if (directory == null) {
            directory = new File( getUserHome() );
            log.warn( "Used user home directory. Should have used directory from preferences." );
        }
        File[] projectFiles = {};
        try {
            if (directory.isDirectory()) {
                projectFiles = directory.listFiles( f -> f.getName().endsWith( PROJECT_FILE_EXTENSION ) );
            }
        } catch (SecurityException e) {
            // NO-OP -- return empty list
        }
        return projectFiles;
    }

    private static String getUserHome() {
        return System.getProperty( "user.home" );
    }

    /**
     * Open the library. Add to model manager.
     * <p>
     * Caller may want to resolve types and validate again. <br>
     * <code>    modelMgr.startValidatingAndResolvingTasks(); </code>
     * 
     * @param selectedFile library to open
     * @param modelManager
     * @throws DexFileException
     */
    public static void openLibrary(File selectedFile, OtmModelManager modelManager) throws DexFileException {
        // Pre-checks
        if (modelManager == null || modelManager.getTlModel() == null)
            throw new DexFileException( "Missing model parameter." );

        openLibraryFile( selectedFile, modelManager.getTlModel() ); // Throws exception

        // Add all the libraries -- model manager will ignore those already added
        modelManager.addLibraries();
    }

    /**
     * Open the library. Does <b>Not</b> add to model manager.
     * 
     * @param selectedFile library to open
     * @param tlModel
     * @throws DexFileException
     */

    public static void openLibraryFile(File selectedFile, TLModel tlModel) throws DexFileException {
        // Pre-checks
        if (tlModel == null)
            throw new DexFileException( "Missing model parameter." );
        if (selectedFile == null)
            throw new DexFileException( "Missing file parameter." );
        checkCanRead( selectedFile ); // throws exceptions
        if (!selectedFile.getName().endsWith( ".otm" ))
            throw new DexFileException( "Incorrect file extension on " + selectedFile.getName() );

        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( selectedFile );
        try {
            LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>( tlModel );
            modelLoader.loadLibraryModel( libraryInput ); // Ignore returned findings
        } catch (LibraryLoaderException e) {
            throw new DexFileException( "Error loading model: " + e.getLocalizedMessage() );
        }
        // Could return findings if needed
    }

    /**
     * Open the passed file using the project manager associated with the model manager. If successful, load projects
     * into the model manager.
     * <p>
     * 
     * @param selectedProjectFile name must end with the PROJECT_FILE_EXTENSION
     * @param mgr
     * @param monitor
     * @return
     * @throws DexFileException
     */
    public static OtmProject openProject(File selectedProjectFile, OtmModelManager mgr,
        OpenProjectProgressMonitor monitor) throws DexFileException {
        if (selectedProjectFile == null)
            throw new DexFileException( "Missing project file parameter." );
        if (!selectedProjectFile.getName().endsWith( PROJECT_FILE_EXTENSION ))
            throw new DexFileException( "Invalid project file name: " + selectedProjectFile.getName() );
        checkCanRead( selectedProjectFile );
        if (mgr == null || mgr.getProjectManager() == null)
            throw new DexFileException( "Missing model or project manager." );

        ValidationFindings findings = null;
        Project newProject = null;
        try {
            newProject = mgr.getProjectManager().loadProject( selectedProjectFile, findings, monitor );
        } catch (Exception e) {
            mgr.getProjectManager().closeAll();
            throw new DexFileException( "Error Opening Project: " + e.getLocalizedMessage() );
        }

        mgr.addProjects();

        // Find the new project to return
        return mgr.getOtmProjectManager().get( newProject );
    }

    /**
     * Save all editable libraries from the list.
     * 
     * @param libraries
     * @return message describing success or exception
     */
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
                    // log.debug( "Saving library: " + libraryName + " " + libraryUrl );
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

    /**
     * Let user choose a directory from using {@link DirectoryChooserDelegate#showDialog(javafx.stage.Window)}
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
     * Return a directory selected by the user using {@link DirectoryChooserDelegate#showDialog(javafx.stage.Window)}.
     * Save the directory using {@link UserSettings#setLastProjectFolder(File)}.
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
     * Return a file selected by the user using {@link FileChooserDelegate#showOpenDialog(javafx.stage.Window)}. Save
     * the directory using {@link UserSettings#setLastProjectFolder(File)}.
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



    @Override
    protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
        // Inherited status message not used.
    }

    @Override
    protected void updateControlStates() {
        // NO-OP
    }

}
