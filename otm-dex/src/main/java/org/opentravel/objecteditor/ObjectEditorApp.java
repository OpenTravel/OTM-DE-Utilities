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

package org.opentravel.objecteditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.AbstractUserSettings;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import javafx.stage.Stage;

/**
 * DEx - OpenTravel development environment object editor in JavaFX. This is the main application that launches the
 * window.
 * 
 * @author dmh
 *
 */
public class ObjectEditorApp extends AbstractOTMApplication {

    private static Log log = LogFactory.getLog( ObjectEditorApp.class );

    private static final String LAYOUT_FILE = "/OtmObjectEditorLayout.fxml";
    private static final String APPLICATION_TITLE = "DEx - OpenTravel Development Environment Object Editor";

    /**
     * Default constructor.
     */
    public ObjectEditorApp() {}

    /**
     * Constructor that provides the manager that should be used when accessing remote OTM repositories.
     * 
     * @param repositoryManager the repository manager instance
     */
    public ObjectEditorApp(RepositoryManager repositoryManager) {
        super( repositoryManager );
    }

    public static void main(String[] args) {
        launch( args ); // start this application in its own window
    }

    @Override
    public void start(Stage primaryStage) {
        super.start( primaryStage );
        ((ObjectEditorController) getController()).setStage( primaryStage );

        // String size = ((UserSettings) getUserSettings()).getDisplaySize();
        // DexStyleSheetHandler.setStyleSheet( primaryStage, size );

        if (getController().getRepositoryManager() == null)
            throw new IllegalStateException( "Repository manager not available." );
    }


    @Override
    protected String getMainWindowFxmlLocation() {
        return LAYOUT_FILE;
    }

    @Override
    protected String getMainWindowTitle() {
        return APPLICATION_TITLE;
    }

    @Override
    protected AbstractUserSettings getUserSettings() {
        return UserSettings.load();
    }

}
