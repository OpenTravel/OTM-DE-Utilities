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
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.controllers.repository.RepositorySelectionController;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.model.ValidateModelManagerItemsTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Abstract base controller for main controllers.
 * 
 * @author dmh
 *
 */
public abstract class DexMainControllerBase extends AbstractMainWindowController
    implements DexMainController, TaskResultHandlerI {
    private static Log log = LogFactory.getLog( DexMainControllerBase.class );

    protected DexMainController mainController;
    protected ImageManager imageMgr = null;
    protected OtmModelManager modelMgr = null;
    protected Node eventPublisherNode = null; // Node to use for change events

    // preferences
    protected UserSettings userSettings;

    protected List<DexIncludedController<?>> includedControllers = new ArrayList<>();

    protected DexStatusController statusController;
    protected MenuBarWithProjectController menuBarController;
    protected DialogBoxContoller dialogBoxController;

    protected OtmEventSubscriptionManager eventManager = new OtmEventSubscriptionManager();

    // protected Stage stage;
    protected static Stage stage;

    private static void setStageStatic(Stage stage) {
        DexMainControllerBase.stage = stage;
    }

    public DexMainControllerBase() {
        log.debug( "Constructing controller." );
    }

    @Override
    public void addIncludedController(DexIncludedController<?> controller) {
        addIncludedController( controller, getEventSubscriptionManager() );
    }

    @Override
    public void addIncludedController(DexIncludedController<?> controller, OtmEventSubscriptionManager eventManager) {
        if (controller == null)
            throw new IllegalStateException( "Tried to add null Included controller" );
        if (eventManager == null)
            eventManager = getEventSubscriptionManager();

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
    public Repository getSelectedRepository() {
        Repository selected = null;
        RepositorySelectionController rController = null;
        for (DexIncludedController<?> c : includedControllers)
            if (c instanceof RepositorySelectionController)
                rController = (RepositorySelectionController) c;
        if (rController != null) {
            try {
                selected = rController.getSelectedRepository();
            } catch (RepositoryException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        }
        return selected;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    public static Stage getStageStatic() {
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
        if (userSettings == null)
            userSettings = UserSettings.load();
        return userSettings;
    }

    @Override
    public void publishEvent(DexChangeEvent event) {
        // Use the menu bar as the event publisher for change events.
        if (eventPublisherNode == null && menuBarController != null)
            eventPublisherNode = menuBarController.getEventPublisherNode();

        if (eventPublisherNode != null)
            eventPublisherNode.fireEvent( event );
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
        setStageStatic( primaryStage );
        this.mainController = null;

        // Initialize managers
        modelMgr = new OtmModelManager( new DexFullActionManager( this ), getRepositoryManager(), getUserSettings() );
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

    // /**
    // * Create a main controller that has a main controller parent.
    // *
    // * @param parent
    // */
    // public void setParent(DexMainController parent) {
    // this.stage = parent.getStage();
    // this.mainController = parent;
    // }

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

    public void updateValidation() {
        new ValidateModelManagerItemsTask( getModelManager(), this, statusController ).go();
    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        // NO-OP
        log.debug( "Task complete" );
        menuBarController.fireEvent( new DexModelChangeEvent() );
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
