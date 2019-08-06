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

package org.opentravel.dex.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.OtmEventUser;
import org.opentravel.application.common.StatusType;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DexFullActionManager;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Abstract base controller for main controllers.
 * 
 * @author dmh
 *
 */
public abstract class DexMainControllerBase extends AbstractMainWindowController implements DexMainController {
    private static Log log = LogFactory.getLog( DexMainControllerBase.class );

    protected DexMainController mainController;
    protected ImageManager imageMgr = null;
    protected OtmModelManager modelMgr = null;

    // preferences
    protected UserSettings userSettings;

    protected List<DexIncludedController<?>> includedControllers = new ArrayList<>();

    protected DexStatusController statusController;
    protected MenuBarWithProjectController menuBarController;
    protected DialogBoxContoller dialogBoxController;

    protected OtmEventSubscriptionManager eventManager = new OtmEventSubscriptionManager();

    protected Stage stage;

    public DexMainControllerBase() {
        log.debug( "Constructing controller." );
    }

    @Override
    public void addIncludedController(DexIncludedController<?> controller, OtmEventSubscriptionManager eventManager) {
        if (controller == null)
            throw new IllegalStateException( "Tried to add null Included controller" );

        controller.checkNodes();
        includedControllers.add( controller );
        controller.configure( this );

        // // Register any published event types
        if (controller instanceof OtmEventUser)
            eventManager.register( (OtmEventUser) controller );
        this.eventManager = eventManager;
    }

    @Override
    public void clear() {
        includedControllers.forEach( DexIncludedController::clear );
    }

    /**
     */
    @Override
    public OtmEventSubscriptionManager getEventSubscriptionManager() {
        return eventManager;
    }

    public DialogBoxContoller getDialogBoxController() {
        if (dialogBoxController == null)
            dialogBoxController = DialogBoxContoller.init();
        return dialogBoxController;
    }

    @Override
    public OtmModelManager getModelManager() {
        if (modelMgr != null)
            return modelMgr;
        return mainController != null ? mainController.getModelManager() : null;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public DexStatusController getStatusController() {
        if (statusController != null)
            return statusController;
        return mainController != null ? mainController.getStatusController() : null;
    }

    @Override
    public UserSettings getUserSettings() {
        return userSettings;
    }

    @FXML
    @Override
    public void initialize() {
        log.debug( "Initializing controller: " + this.getClass().getSimpleName() );
    }

    @Override
    public void postError(Exception e, String title) {
        if (getDialogBoxController() != null)
            if (e == null)
                getDialogBoxController().show( "", title );
            else {
                log.debug( title + e.getLocalizedMessage() );
                if (e.getCause() == null)
                    getDialogBoxController().show( title, e.getLocalizedMessage() );
                else
                    getDialogBoxController().show( title,
                        e.getLocalizedMessage() + " \n\n(" + e.getCause().toString() + ")" );
            }
        else
            log.debug( "Missing dialog box to show: " + title );
    }

    @Override
    public void postProgress(double percentDone) {
        if (getStatusController() != null)
            getStatusController().postProgress( percentDone );
    }

    @Override
    public void postStatus(String string) {
        if (getStatusController() != null)
            getStatusController().postStatus( string );
    }

    @Override
    public void refresh() {
        includedControllers.forEach( DexIncludedController::refresh );
    }

    /**
     * Set the stage for a top level main controller. Called by the application on startup. Initialize action, model and
     * image managers. checkNodes().
     * 
     * @param primaryStage
     */
    public void setStage(Stage primaryStage) {
        // These may be needed by sub-controllers
        this.stage = primaryStage;
        this.mainController = null;

        // Initialize managers
        // TODO - use user settings to select which action manager to use
        modelMgr = new OtmModelManager( new DexFullActionManager( this ), getRepositoryManager() );
        imageMgr = new ImageManager( primaryStage );

        checkNodes();
    }

    /**
     * Set the main controller field. Set model manager's status controller.
     * 
     * @param controller
     */
    protected void setMainController(DexMainController controller) {
        mainController = controller;
        if (modelMgr != null)
            modelMgr.setStatusController( getStatusController() );
    }

    /**
     * Create a main controller that has a main controller parent.
     * 
     * @param parent
     */
    public void setParent(DexMainController parent) {
        this.stage = parent.getStage();
        this.mainController = parent;
    }

    // Required by AbstractApp...
    @Override
    protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
        if (getStatusController() != null)
            getStatusController().postStatus( message );
    }

    @Override
    public void updateActionQueueSize(int size) {
        if (menuBarController != null)
            menuBarController.updateActionQueueSize( size );
    }

    @Override
    protected void updateControlStates() {
        // Platform.runLater(() -> {
        // // boolean exDisplayDisabled = (originalDocument == null);
        // // boolean exControlsDisabled = (model == null) || (originalDocument == null);
        // //
        // // libraryText.setText( (modelFile == null) ? "" : modelFile.getName() );
        // // libraryTooltip.setText( (modelFile == null) ? "" : modelFile.getAbsolutePath() );
        // // exampleText.setText( (exampleFile == null) ? "" : exampleFile.getName() );
        // // exampleTooltip.setText( (exampleFile == null) ? "" : exampleFile.getAbsolutePath() );
    }

}
