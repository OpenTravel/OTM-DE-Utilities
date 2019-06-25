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

package org.opentravel.repositoryviewer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.AbstractUserSettings;
import org.opentravel.common.DialogBox;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author dmh
 *
 */
public class RepositoryViewerApp extends AbstractOTMApplication {

    private static Log log = LogFactory.getLog( RepositoryViewerApp.class );

    Stage window;
    private static final String LAYOUT_FILE = "/RepositoryViewer.fxml";
    private static final String APPLICATION_TITLE = "OpenTravel Repository Viewer";

    /**
     * Default constructor.
     */
    public RepositoryViewerApp() {}

    /**
     * Constructor that provides the manager that should be used when accessing remote OTM repositories.
     * 
     * @param repositoryManager the repository manager instance
     */
    public RepositoryViewerApp(RepositoryManager repositoryManager) {
        super( repositoryManager );
    }

    public static void main(String[] args) {
        launch( args ); // start this application in its own window
    }

    @Override
    public void start(Stage primaryStage) {
        super.start( primaryStage );
        ((RepositoryViewerController) getController()).setStage( primaryStage );
        primaryStage.getScene().getStylesheets().add( "DavesViper.css" );
    }

    public void closeProgram(WindowEvent e) {
        e.consume(); // take the event away from windows
        if (DialogBox.display( "Close?", "Do you really want to close?" ))
            window.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.application.common.AbstractOTMApplication#getMainWindowFxmlLocation()
     */
    @Override
    protected String getMainWindowFxmlLocation() {
        return LAYOUT_FILE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.application.common.AbstractOTMApplication#getUserSettings()
     */
    @Override
    protected AbstractUserSettings getUserSettings() {
        return UserSettings.load();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.application.common.AbstractOTMApplication#getMainWindowTitle()
     */
    @Override
    protected String getMainWindowTitle() {
        return APPLICATION_TITLE;
    }

}
