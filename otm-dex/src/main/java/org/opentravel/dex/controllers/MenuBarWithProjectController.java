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
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.DexFileHandler;
import org.opentravel.common.DialogBox;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.controllers.popup.NewProjectDialogController;
import org.opentravel.dex.controllers.resources.ResourcesWindowController;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexEventDispatcher;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.OpenLibraryFileTask;
import org.opentravel.dex.tasks.repository.OpenProjectFileTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;

/**
 * Manage the menu bar.
 * 
 * @author dmh
 *
 */
public class MenuBarWithProjectController extends DexIncludedControllerBase<String> implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( MenuBarWithProjectController.class );

    //
    // FIXME - need to add ability to unset user settings
    //

    // FXML injected objects
    @FXML
    private ComboBox<String> projectCombo;
    @FXML
    private Label projectLabel;
    @FXML
    public MenuItem doCloseItem;
    @FXML
    public MenuItem fileOpenItem;
    @FXML
    private Label actionCount;
    @FXML
    private Button undoActionButton;
    @FXML
    private ToolBar menuToolBar;

    private Stage stage;
    private OtmModelManager modelMgr;

    private DialogBoxContoller dialogBox;
    private ResourcesWindowController rwc;

    private UserSettings userSettings;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {};
    private static final EventType[] publishedEvents = {DexModelChangeEvent.MODEL_CHANGED};

    public MenuBarWithProjectController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override to get all the event types published by actions since this eventPublisherNode will be used by main
     * controller for action initiated events.
     */
    @Override
    public List<EventType<? extends AbstractOtmEvent>> getPublishedEventTypes() {
        // publishedEventTypes = Collections.unmodifiableList( Arrays.asList( publishedEvents ) );
        publishedEventTypes = new ArrayList<>( Arrays.asList( publishedEvents ) );
        for (DexActions action : DexActions.values()) {
            DexChangeEvent event = null;
            try {
                event = DexActions.getHandler( action );
                if (event != null && !publishedEventTypes.contains( event.getEventType() ))
                    publishedEventTypes.add( event.getEventType() );
            } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            }
        }
        return publishedEventTypes;
    }

    @FXML
    public void aboutApplication(ActionEvent event) {
        AboutDialogController.createAboutDialog( stage ).showAndWait();
    }

    @FXML
    public void appExit(Event e) {
        log.debug( "exit" );
        e.consume(); // take the event away from windows in case they answer no.
        if (DialogBox.display( "Exit", "Do you really want to exit?" ))
            stage.close();
    }

    @Override
    public void checkNodes() {
        if (menuToolBar == null || projectCombo == null || projectLabel == null)
            throw new IllegalStateException( "Menu bar is missing FXML injected fields." );
        log.debug( "FXML Nodes checked OK." );
    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        eventPublisherNode = menuToolBar;
        modelMgr = mainController.getModelManager();
        userSettings = mainController.getUserSettings();

        stage = mainController.getStage();

        // For debugging, intercept and log DexEvents
        stage.setEventDispatcher( new DexEventDispatcher( stage.getEventDispatcher() ) );

        // Set up to handle opening and closing files
        setFileOpenHandler( this::handleOpenMenu );
        setdoCloseHandler( this::handleCloseMenu );
        setUndoAction( e -> undoAction() );

        rwc = ResourcesWindowController.init();
        rwc.setup( "" );
        rwc.configure( mainController );

    }

    private DialogBoxContoller getDialogBox() {
        dialogBox = DialogBoxContoller.init();
        dialogBox.setUserSettings( userSettings );
        return dialogBox;
    }

    /**
     * Configure the combo box with a list and listener.
     * <p>
     * Usage: menuBarWithProjectController.configureProjectMenuButton(projectList, this::projectComboSelectionListener);
     * 
     * @param projectList
     * @param listener
     */
    public void configureComboBox(ObservableList<String> projectList, EventHandler<ActionEvent> listener) {
        // log.debug("Setting combo.");
        projectList.sort( null );
        projectCombo.setItems( projectList );
        projectCombo.setOnAction( listener );
    }

    /** *********************************************************** **/

    @FXML
    public void undoAction(ActionEvent e) {
        log.debug( "Close menu item selected." );
        dialogBox.show( "Undo", "Not Implemented" );
    }

    public void setUndoAction(EventHandler<ActionEvent> handler) {
        undoActionButton.setOnAction( handler );
    }

    public void updateActionQueueSize(int size) {
        actionCount.setText( Integer.toString( size ) );
        undoActionButton.setDisable( size <= 0 );
    }

    public void undoAction() {
        modelMgr.getActionManager().undo();
        // mainController.refresh();
    }

    @FXML
    public void doClose(ActionEvent e) {
        // This is only run if the handler is not set.
        log.debug( "Close menu item selected." );
        getDialogBox().show( "Close", "Not Implemented" );
    }

    @FXML
    void doNewProject(ActionEvent e) {
        NewProjectDialogController npdc = NewProjectDialogController.init();
        npdc.configure( modelMgr, userSettings );
        npdc.showAndWait( "" );
    }

    @FXML
    public void fileOpen(ActionEvent e) {
        // This is only run if the handler is not set.
        log.debug( "File Open selected." );
        getDialogBox().show( "Open", "Not implemented" );
    }

    /**
     * Menu action event handler for opening a file.
     * <p>
     * <ol>
     * <li>Run file chooser
     * <li>Display dialog
     * <li>Get default directory from settings
     * <li>clear current model and all controllers
     * <li>Create open project task and run it
     * <li>Handle on complete
     * </ol>
     * 
     * @param event
     */

    public void handleOpenMenu(ActionEvent event) {
        log.debug( "Handle file open action event." );
        DexFileHandler fileHandler = new DexFileHandler();
        if (event.getTarget() instanceof MenuItem) {
            File selectedFile = fileHandler.fileChooser( stage, userSettings );
            openFile( selectedFile );
        }
    }

    public void openFile(File selectedFile) {
        if (selectedFile != null) {
            if (selectedFile.getName().endsWith( ".otm" )) {
                if (!userSettings.getHideOpenProjectDialog())
                    getDialogBox().show( "Opening Library", "Please wait." );
                new OpenLibraryFileTask( selectedFile, modelMgr, this::handleTaskComplete,
                    mainController.getStatusController() ).go();
            } else {
                if (!userSettings.getHideOpenProjectDialog())
                    getDialogBox().show( "Opening Project", "Please wait." );
                new OpenProjectFileTask( selectedFile, modelMgr, this::handleTaskComplete,
                    mainController.getStatusController() ).go();
            }
        }
    }

    private HashMap<String,File> projectMap = new HashMap<>();

    public void configureProjectCombo() {
        DexFileHandler fileHandler = new DexFileHandler();
        File initialDirectory = userSettings.getLastProjectFolder();
        if (initialDirectory != null) {
            for (File file : fileHandler.getProjectList( initialDirectory )) {
                projectMap.put( file.getName(), file );
            }
            ObservableList<String> projectList = FXCollections.observableArrayList( projectMap.keySet() );
            configureComboBox( projectList, this::projectComboSelectionListener );
        }
    }

    public void projectComboSelectionListener(Event e) {
        log.debug( "project selection event" );
        if (e.getTarget() instanceof ComboBox)
            openFile( projectMap.get( ((ComboBox<?>) e.getTarget()).getValue() ) );
    }

    public void handleCloseMenu(ActionEvent event) {
        log.debug( "Handle close action event." );

        if (event.getTarget() instanceof MenuItem) {
            clear();
            if (modelMgr != null) {
                modelMgr.clear();
                // Let everyone know
                fireEvent( new DexModelChangeEvent( modelMgr ) );
                projectCombo.getSelectionModel().clearSelection();
            }
            // FIXME - clear actionQueue
        }
    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        if (event.getTarget() instanceof OpenProjectFileTask || event.getTarget() instanceof OpenLibraryFileTask) {
            if (dialogBox != null)
                dialogBox.close();
            fireEvent( new DexModelChangeEvent( modelMgr ) );
            mainController.refresh();
        }
    }

    @FXML
    private ToggleButton launchResourceWindow;

    @FXML
    private void launchResourceWindow() {
        if (launchResourceWindow.isSelected())
            rwc.show( "" );
        else
            rwc.hide();
    }

    public void setComboLabel(String text) {
        projectLabel.setText( text );
    }

    public void setdoCloseHandler(EventHandler<ActionEvent> handler) {
        doCloseItem.setOnAction( handler );
    }

    public void setFileOpenHandler(EventHandler<ActionEvent> handler) {
        fileOpenItem.setOnAction( handler );
    }

    /**
     * Show or hide the combo box and its label.
     * 
     * @param visable true to show, false to hide
     */
    public void showCombo(boolean visable) {
        projectCombo.setVisible( visable );
        projectLabel.setVisible( visable );
        // setup to handle project combo
        if (visable)
            configureProjectCombo();
    }

}
