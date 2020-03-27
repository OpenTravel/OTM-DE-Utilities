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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.popup.CompileDialogController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.controllers.popup.NewLibraryDialogController;
import org.opentravel.dex.controllers.popup.NewProjectDialogController;
import org.opentravel.dex.controllers.popup.SaveAndExitDialogController;
import org.opentravel.dex.controllers.popup.WebViewDialogController;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexEventDispatcher;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexRepositorySelectionEvent;
import org.opentravel.dex.events.OtmObjectReplacedEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.OpenLibraryFileTask;
import org.opentravel.dex.tasks.repository.OpenProjectFileTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

import java.io.File;
import java.util.ArrayList;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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
    // FIXME - need to fix the size of the region used for undo
    //

    // FXML injected objects
    @FXML
    private ComboBox<String> projectCombo;
    @FXML
    private Menu doCompileMenu;
    @FXML
    private Menu viewsMenu;
    @FXML
    private Menu displaySizeMenu;
    @FXML
    private Label projectLabel;
    @FXML
    public MenuItem doCloseItem;
    @FXML
    public MenuItem doSaveAllItem;
    @FXML
    public MenuItem doNewLibraryItem;
    @FXML
    public MenuItem doNewProjectItem;
    @FXML
    public MenuItem fileOpenItem;
    @FXML
    private Label actionCount;
    @FXML
    private Label lastAction;
    @FXML
    private Button undoActionButton;
    @FXML
    private Button navForwardButton;
    @FXML
    private Button navBackButton;
    @FXML
    private ToolBar menuToolBar;

    private Stage stage;
    private OtmModelManager modelMgr;
    private DialogBoxContoller dialogBox;
    private UserSettings userSettings;
    private Repository selectedRepository = null;
    private DexEventDispatcher eventDispatcher;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexRepositorySelectionEvent.REPOSITORY_SELECTED, DexMemberSelectionEvent.MEMBER_SELECTED};
    private static final EventType[] publishedEvents =
        {DexMemberDeleteEvent.MEMBER_DELETED, OtmObjectReplacedEvent.OBJECT_REPLACED, DexModelChangeEvent.MODEL_CHANGED,
            DexMemberSelectionEvent.MEMBER_SELECTED};

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
        publishedEventTypes = new ArrayList<>();
        publishedEventTypes.add( DexModelChangeEvent.MODEL_CHANGED );
        publishedEventTypes.add( DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED );
        publishedEventTypes.add( DexMemberSelectionEvent.TYPE_USER_SELECTED );
        publishedEventTypes.add( DexRepositorySelectionEvent.REPOSITORY_SELECTED );

        for (EventType<? extends DexEvent> et : publishedEvents) {
            publishedEventTypes.add( et );
        }
        // TODO - some actions create events (assignTypeAction) that are not in the actions enumeration.
        // For now, they are added to publishedEvents array. Find a better way to register them.
        for (DexActions action : DexActions.values()) {
            DexChangeEvent event = null;
            try {
                event = DexActions.getEvent( action );
                if (event != null && !publishedEventTypes.contains( event.getEventType() ))
                    publishedEventTypes.add( event.getEventType() );
            } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            }
        }
        return publishedEventTypes;
    }

    @FXML
    public void goBack() {
        eventDispatcher.goBack( this );
        updateNavigationButtons();
    }

    @FXML
    public void goForward() {
        eventDispatcher.goForward( this );
        updateNavigationButtons();
    }

    public void updateNavigationButtons() {
        if (eventDispatcher != null) {
            navBackButton.setDisable( !eventDispatcher.canGoBack() );
            navForwardButton.setDisable( !eventDispatcher.canGoForward() );
        } else {
            navBackButton.setDisable( true );
            navForwardButton.setDisable( true );
        }
    }

    @FXML
    public void aboutApplication(ActionEvent event) {
        AboutDialogController.createAboutDialog( stage ).showAndWait();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        updateNavigationButtons();
        // log.debug( event.getEventType() + " event received." );
        if (event instanceof DexRepositorySelectionEvent)
            selectedRepository = ((DexRepositorySelectionEvent) event).getRepository();
    }

    /**
     * Prompt user to save work and exit, or cancel.
     */
    @FXML
    public void appExit(Event e) {
        // Use save and exit controller with will prompt user to save if it finds there are changes in the queue
        SaveAndExitDialogController controller = SaveAndExitDialogController.init();
        controller.setModelManager( modelMgr );
        controller.showAndWait( "" );
        e.consume(); // take the event away from windows in case they answer no.

        if (controller.okResult())
            stage.close();
    }

    @Override
    public void checkNodes() {
        if (menuToolBar == null || projectCombo == null || projectLabel == null)
            throw new IllegalStateException( "Menu bar is missing FXML injected fields." );
        // log.debug( "FXML Nodes checked OK." );
    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        eventPublisherNode = menuToolBar;
        modelMgr = mainController.getModelManager();
        userSettings = mainController.getUserSettings();

        stage = mainController.getStage();

        // For debugging, intercept and log DexEvents
        eventDispatcher = new DexEventDispatcher( stage.getEventDispatcher() );
        stage.setEventDispatcher( eventDispatcher );

        // Set up to handle opening and closing files
        setFileOpenHandler( this::handleOpenMenu );
        setdoCloseHandler( this::handleCloseMenu );
        setdoSaveAllHandler( this::handleSaveAllMenu );
        setUndoAction( e -> undoAction() );

        updateNavigationButtons();
        setDisplaySizeMenu();
    }

    private DialogBoxContoller getDialogBox(UserSettings settings) {
        dialogBox = DialogBoxContoller.init();
        dialogBox.setUserSettings( settings );
        return dialogBox;
    }


    @FXML
    public void undoAction(ActionEvent e) {
        // log.debug( "Close menu item selected." );
        dialogBox.show( "Undo", "Not Implemented" );
    }

    @FXML
    public void setDisplaySize(ActionEvent e) {
        if (e.getSource() instanceof MenuItem) {
            MenuItem item = (MenuItem) e.getSource();
            getMainController().setStyleSheet( item.getText() );
            setDisplaySizeMenu();
        }
    }

    private void setDisplaySizeMenu() {
        // TODO - use labels to create menu entries
        // disable currently selected size
        String selector = getMainController().getStyleSheet();
        displaySizeMenu.getItems().forEach( i -> i.setDisable( i.getText().equals( selector ) ) );
    }

    public void setUndoAction(EventHandler<ActionEvent> handler) {
        undoActionButton.setOnAction( handler );
    }

    public void updateActionManagerDisplay(DexActionManager actionManger) {
        if (actionManger != null) {
            actionCount.setText( Integer.toString( actionManger.getQueueSize() ) );
            undoActionButton.setDisable( actionManger.getQueueSize() <= 0 );
            String last = "";
            if (actionManger.getLastAction() != null)
                last = (actionManger.getLastAction().toString());
            if (last != null && last.length() > 40)
                last = last.substring( 0, 40 ); // don't overflow the menu bar space
            lastAction.setText( last );
        }
    }

    // // DONE -- it should get its own size
    // public void updateActionQueueSize(int size) {
    // actionCount.setText( Integer.toString( size ) );
    // undoActionButton.setDisable( size <= 0 );
    // String last = "";
    // if (modelMgr != null && modelMgr.getActionManager( true ) != null
    // && modelMgr.getActionManager( true ).getLastAction() != null)
    // last = modelMgr.getActionManager( true ).getLastAction().toString();
    // if (last != null && last.length() > 40)
    // last = last.substring( 0, 40 ); // don't overflow the menu bar space
    // lastAction.setText( last );
    // }

    public void undoAction() {
        modelMgr.getActionManager( true ).undo();
        // mainController.refresh();
    }

    @FXML
    public void doCompile(ActionEvent e) {
        // DONE - if not saved, save now
        CompileDialogController cdc = CompileDialogController.init();
        cdc.configure( modelMgr, userSettings, mainController.getStatusController() );
        cdc.show( "" );
    }

    @FXML
    public void doClose(ActionEvent e) {
        // This is only run if the handler is not set.
        // log.debug( "Close menu item selected." );
        getDialogBox( null ).show( "Close", "Not Implemented" );
    }

    @FXML
    public void doSaveAll(ActionEvent e) {
        // This is only run if the handler is not set.
        getDialogBox( null ).show( "Save All", "Not Implemented" );
    }

    @FXML
    void doNewLibrary(ActionEvent e) {
        NewLibraryDialogController nldc = NewLibraryDialogController.init();
        nldc.configure( modelMgr, userSettings );
        Results results = nldc.showAndWait( "" );
        if (results == Results.OK) {
            fireEvent( new DexModelChangeEvent( modelMgr ) );
        }
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
        // log.debug( "File Open selected." );
        getDialogBox( null ).show( "Open", "Not implemented" );
    }

    public void handleOpenMenu(ActionEvent event) {
        // log.debug( "Handle file open action event." );
        DexFileHandler fileHandler = new DexFileHandler();
        if (event.getTarget() instanceof MenuItem) {
            File selectedFile = fileHandler.fileChooser( stage, userSettings );
            // Update Combo in menu bar
            if (selectedFile != null)
                configureProjectCombo( selectedFile.getParentFile() );
            // Run the task
            openFile( selectedFile );
        }
    }

    public void openFile(File selectedFile) {
        if (selectedFile != null) {
            if (selectedFile.getName().endsWith( ".otm" )) {
                if (!userSettings.getHideOpenProjectDialog())
                    getDialogBox( userSettings ).show( "Opening Library", "Please wait." );
                new OpenLibraryFileTask( selectedFile, modelMgr, this::handleTaskComplete,
                    mainController.getStatusController() ).go();
            } else {
                if (!userSettings.getHideOpenProjectDialog())
                    getDialogBox( userSettings ).show( "Opening Project", "Please wait." );
                new OpenProjectFileTask( selectedFile, modelMgr, this::handleTaskComplete,
                    mainController.getStatusController() ).go();
            }
        }
    }

    private HashMap<String,File> projectMap = new HashMap<>();

    public void configureProjectCombo() {
        File initialDirectory = userSettings.getLastProjectFolder();
        configureProjectCombo( initialDirectory );
    }

    public void configureProjectCombo(File initialDirectory) {
        DexFileHandler fileHandler = new DexFileHandler();
        if (initialDirectory != null) {
            for (File file : fileHandler.getProjectList( initialDirectory )) {
                projectMap.put( file.getName(), file );
            }
            ObservableList<String> projectList = FXCollections.observableArrayList( projectMap.keySet() );
            configureProjectComboBox( projectList, this::projectComboSelectionListener );
        }
    }

    /**
     * Configure the combo box with a list and listener.
     * <p>
     * Usage: menuBarWithProjectController.configureProjectMenuButton(projectList, this::projectComboSelectionListener);
     * 
     * @param projectList
     * @param listener
     */
    private void configureProjectComboBox(ObservableList<String> projectList, EventHandler<ActionEvent> listener) {
        log.debug( "Setting combo." );
        projectList.sort( null );
        projectCombo.setItems( projectList );
        projectCombo.setOnAction( listener );
    }

    // FUTURE
    // public void addViewItems(List<DexIncludedController<?>> controllers) {
    // for (DexIncludedController<?> c : controllers) {
    // if (c instanceof DexTabController)
    // addViewItem( (DexTabController) c );
    // }
    // }

    public void addViewItem(DexTabController tc) {
        if (tc.getDialogTitle() != null) {
            // log.debug( "Add controller" );
            MenuItem item = new MenuItem( tc.getDialogTitle() );
            item.setOnAction( tc::launchWindow );
            viewsMenu.getItems().add( item );
        }
    }



    public void projectComboSelectionListener(Event e) {
        // log.debug( "project selection event" );
        if (e.getTarget() instanceof ComboBox)
            openFile( projectMap.get( ((ComboBox<?>) e.getTarget()).getValue() ) );
    }

    public void handleSaveAllMenu(ActionEvent event) {
        // log.debug( "Handle save all action event." );
        String results = DexFileHandler.saveLibraries( modelMgr.getEditableLibraries() );
        DialogBoxContoller dialog = getDialogBox( null );
        dialog.show( "Save Results", results );
    }

    public void handleCloseMenu(ActionEvent event) {
        // log.debug( "Handle close action event." );
        // Close all menu item
        if (event.getTarget() instanceof MenuItem) {
            clear();
            if (modelMgr != null) {
                // Clear the model
                modelMgr.clear();

                // Clear action queue
                DexActionManager actionMgr = modelMgr.getActionManager( true );
                if (actionMgr != null) {
                    actionMgr.clearQueue();
                    updateActionManagerDisplay( actionMgr );
                }
                // clear status line
                mainController.getStatusController().postStatus( "" );

                // clear the project combo
                projectCombo.getSelectionModel().clearSelection();

                // Let everyone know
                fireEvent( new DexModelChangeEvent( modelMgr ) );
            }
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
    private MenuItem launchWebRepoWindow;

    @FXML
    private void launchWebRepoWindow() {
        String repoUrl = null;
        if (selectedRepository instanceof RemoteRepositoryClient)
            repoUrl = ((RemoteRepositoryClient) selectedRepository).getEndpointUrl();
        WebViewDialogController wvdc = WebViewDialogController.init();
        wvdc.show( repoUrl );
    }

    public void setComboLabel(String text) {
        projectLabel.setText( text );
    }

    public void setdoCloseHandler(EventHandler<ActionEvent> handler) {
        doCloseItem.setOnAction( handler );
    }

    public void setdoSaveAllHandler(EventHandler<ActionEvent> handler) {
        doSaveAllItem.setOnAction( handler );
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
