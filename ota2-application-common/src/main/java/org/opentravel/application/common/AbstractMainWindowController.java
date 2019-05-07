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

package org.opentravel.application.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Base controller class for all JavaFX main window controllers.
 */
public abstract class AbstractMainWindowController {

    protected static final ExtensionFilter OTM_EXTENSION_FILTER =
        new ExtensionFilter( "OTM Library Files (*.otm)", "*.otm" );
    protected static final ExtensionFilter OTP_EXTENSION_FILTER =
        new ExtensionFilter( "OTM Project Files (*.otp)", "*.otp" );
    protected static final ExtensionFilter OTR_EXTENSION_FILTER =
        new ExtensionFilter( "OTM Release Files (*.otr)", "*.otr" );
    protected static final ExtensionFilter XML_EXTENSION_FILTER = new ExtensionFilter( "XML Files (*.xml)", "*.xml" );
    protected static final ExtensionFilter JSON_EXTENSION_FILTER =
        new ExtensionFilter( "JSON Files (*.json)", "*.json" );
    protected static final ExtensionFilter HTML_EXTENSION_FILTER =
        new ExtensionFilter( "HTML Files (*.html)", "*.html" );
    protected static final ExtensionFilter ALL_EXTENSION_FILTER = new ExtensionFilter( "All Files (*.*)", "*.*" );

    private static final Logger log = LoggerFactory.getLogger( AbstractMainWindowController.class );

    private Stage primaryStage;

    /**
     * Returns the primary stage for the window associated with this controller.
     * 
     * @return Stage
     */
    protected Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Assigns the primary stage for the window associated with this controller.
     *
     * @param primaryStage the primary stage for this controller
     */
    protected void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Displays a message to the user in the status bar and optionally disables the interactive controls on the display.
     * 
     * @param message the status bar message to display
     * @param statusType the severity of the message being displayed
     * @param disableControls flag indicating whether interactive controls should be disabled
     */
    protected abstract void setStatusMessage(String message, StatusType statusType, boolean disableControls);

    /**
     * Updates the enabled/disables states of the visual controls based on the current state of user selections.
     */
    protected abstract void updateControlStates();

    /**
     * Returns a new file chooser that is configured for the selection of specific types of files.
     * 
     * @param title the title of the new file chooser
     * @param initialDirectory the initial directory location for the chooser
     * @param extensionFilters two-element arrays that specify the file extension and extension description (in that
     *        order)
     * @return FileChooser
     */
    protected FileChooser newFileChooser(String title, File initialDirectory, ExtensionFilter... extensionFilters) {
        FileChooser chooser = new FileChooser();
        File directory = initialDirectory;

        // Make sure the initial directory for the chooser exists
        while ((directory != null) && !directory.exists()) {
            directory = directory.getParentFile();
        }
        if (directory == null) {
            directory = new File( System.getProperty( "user.home" ) );
        }

        chooser.setTitle( title );
        chooser.setInitialDirectory( directory );

        for (ExtensionFilter filter : extensionFilters) {
            chooser.getExtensionFilters().add( filter );
        }
        return chooser;
    }

    /**
     * Returns a new directory chooser instance.
     * 
     * @param title the title of the new directory chooser
     * @param initialDirectory the initial directory location for the chooser
     * @return DirectoryChooser
     */
    protected DirectoryChooser newDirectoryChooser(String title, File initialDirectory) {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = initialDirectory;

        // Make sure the initial directory for the chooser exists
        while ((directory != null) && !directory.exists()) {
            directory = directory.getParentFile();
        }
        if (directory == null) {
            directory = new File( System.getProperty( "user.home" ) );
        }

        chooser.setTitle( title );
        chooser.setInitialDirectory( directory );
        return chooser;
    }

    /**
     * Displays a modal error dialog with the title and message provided.
     * 
     * @param title the title of the error dialog
     * @param message the error message to display
     */
    protected void showErrorDialog(String title, String message) {
        Platform.runLater( () -> {
            Alert errorDialog = new Alert( AlertType.ERROR );

            errorDialog.setTitle( title );
            errorDialog.setContentText( message );
            errorDialog.setHeaderText( null );
            errorDialog.showAndWait();
        } );
    }

    /**
     * Returns a user-readable error message for the given exception.
     * 
     * @param e the exception for which to create a formatted error message
     * @return String
     */
    protected String getErrorMessage(Exception e) {
        Throwable rootCause = e;
        String message = null;

        while ((message == null) && (rootCause != null)) {
            message = rootCause.getMessage();
            rootCause = rootCause.getCause();
        }
        if (message == null) {
            message = (e == null) ? "Unknown Error" : e.getClass().getSimpleName();
        }
        return message;
    }

    /**
     * Abstract class that executes a background task in a non-UI thread.
     */
    protected abstract class BackgroundTask implements Runnable {

        private String statusMessage;
        private StatusType statusType;

        /**
         * Constructor that specifies the status message to display during task execution.
         * 
         * @param statusMessage the status message for the task
         * @param statusType the type of status message to display
         */
        public BackgroundTask(String statusMessage, StatusType statusType) {
            this.statusMessage = statusMessage;
            this.statusType = statusType;
        }

        /**
         * Executes the sub-class specific task functions.
         * 
         * @throws OtmApplicationException thrown if an error occurs during task execution
         */
        protected abstract void execute() throws OtmApplicationException;

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                setStatusMessage( statusMessage, statusType, true );
                execute();

            } catch (Exception e) {
                String errorMessage = (e.getMessage() != null) ? e.getMessage() : "See log output for DETAILS.";

                try {
                    setStatusMessage( "ERROR: " + errorMessage, StatusType.ERROR, false );
                    log.error( errorMessage, e );
                    updateControlStates();
                    Thread.sleep( 5000 );

                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                }

            } finally {
                setStatusMessage( null, null, false );
                updateControlStates();
            }
        }

    }

}
