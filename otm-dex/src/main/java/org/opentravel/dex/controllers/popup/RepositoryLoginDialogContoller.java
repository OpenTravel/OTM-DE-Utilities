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

package org.opentravel.dex.controllers.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryUtils;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for unlock library dialog box pop-up menu.
 * <p>
 * This MUST be constructed by passing an FXMLLoader instance which needs access to default constructor.
 * 
 * @author dmh
 *
 */
public class RepositoryLoginDialogContoller extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( RepositoryLoginDialogContoller.class );

    public static final String LAYOUT_FILE = "/RepositoryLoginDialog.fxml";

    // Stage create by FXML loader
    protected static Stage dialogStage;

    private static String helpText = "Login to repository using provided credentials.";
    private static String dialogTitle = "Login Dialog";

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @param repositoryManager
     * @return dialog box controller or null
     */
    public static RepositoryLoginDialogContoller init(RepositoryManager repositoryManager) {
        FXMLLoader loader = new FXMLLoader( RepositoryLoginDialogContoller.class.getResource( LAYOUT_FILE ) );
        RepositoryLoginDialogContoller controller = null;

        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof RepositoryLoginDialogContoller))
                throw new IllegalStateException( "Error creating login dialog controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        controller.setRepositoryManager( repositoryManager );
        positionStage( dialogStage );
        // log.debug( "Repo Login Dialog controller initialized. " + repositoryManager );
        return controller;
    }

    // private Results result = Results.OK;
    @FXML
    Button dialogButtonOK;
    @FXML
    RadioButton dialogButtonAnonymous;
    @FXML
    Button dialogButtonCancel;
    @FXML
    TextField loginRepoID;
    @FXML
    CheckBox repoOKCheckbox;
    @FXML
    ComboBox<String> loginURLCombo;
    @FXML
    TextField loginUser;
    @FXML
    TextField loginPassword;
    @FXML
    Button dialogButtonTest;
    @FXML
    ProgressIndicator dialogProgress;
    @FXML
    TextArea testResults;

    RepositoryManager repositoryManager;

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
        log.warn( "Set repository manager to " + repositoryManager );
    }

    RemoteRepository selectedRemoteRepository = null; // Selected repository

    private void anonymousSelectionChanged() {
        // If selected, grey out the user name and password
        testResults.setText( "" );
        loginPassword.setDisable( dialogButtonAnonymous.isSelected() );
        loginUser.setDisable( dialogButtonAnonymous.isSelected() );
    }

    @Override
    public void clear() {
        loginRepoID.setText( "" );
        repoOKCheckbox.setSelected( false );
        loginUser.setText( "" );
        loginPassword.setText( "" );
        testResults.setText( "" );
    }

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
    }

    private void configureRepositoryCombo() {
        log.debug( "Configuring repository combo box." );

        ObservableList<String> repositoryIds = FXCollections.observableArrayList();
        repositoryManager.listRemoteRepositories().forEach( r -> repositoryIds.add( r.getEndpointUrl() ) );
        loginURLCombo.setItems( repositoryIds );
        loginURLCombo.getSelectionModel().select( 0 );

        // Configure listener for choice box
        loginURLCombo.valueProperty().addListener( (observable, oldValue, newValue) -> repositorySelectionChanged() );
        log.debug( "Repository choice has " + repositoryIds.size() + " items." );

        repositorySelectionChanged(); // initialize values
    }

    @Override
    public void doCancel() {
        super.doCancel();
        testResults.setText( "" );
    }

    public void doTest() {
        log.debug( "Test: " + loginUser.getText() + " : " + loginPassword.getText() );
        testResults.setText( "Testing" );

        String user = loginUser.getText();
        String pwd = loginPassword.getText();
        dialogProgress.progressProperty().set( -1.0 );

        StringBuilder results = new StringBuilder( "Test Results\n\t" );
        if (selectedRemoteRepository != null) {
            if (!dialogButtonAnonymous.isSelected())
                try {
                    repositoryManager.setCredentials( selectedRemoteRepository, user, pwd );
                } catch (RepositoryException e) {
                    postException( e );
                }
            // Check to see what rights these credentials have
            try {
                List<RepositoryItem> items = selectedRemoteRepository.getLockedItems();
                results.append( "User has " + items.size() + " locked items.\n\t" );
            } catch (Exception e) {
                results.append( "User can not access locked items.\n\t" );
            }
            try {
                RepositoryPermission auth =
                    selectedRemoteRepository.getUserAuthorization( selectedRemoteRepository.getEndpointUrl() );
                results.append(
                    "Authorization on " + selectedRemoteRepository.getEndpointUrl() + " = " + auth.toString() + "\n" );
            } catch (Exception e) {
                results.append(
                    "User can not access authorization for " + selectedRemoteRepository.getEndpointUrl() + "\n" );
            }
        } else {
            results.append( "No valid repository selected." );
        }
        testResults.setText( results.toString() );
        dialogProgress.progressProperty().set( 1.0 );
    }

    /**
     * Get the repository at the URL. If not found, try to add the URL to the repository managers list of known
     * repositories.
     * 
     * @param rMgr
     * @param url
     * @return a remote repository or null if unsuccessful.
     */
    private RemoteRepository getRemoteRepository(RepositoryManager rMgr, String url) {
        RemoteRepository rr = null;

        // Repository utils will resolve IP addresses and DNS names
        RemoteRepositoryUtils remoteUtils = new RemoteRepositoryUtils();
        RepositoryInfoType remoteRepositoryMetadata = null;
        try {
            remoteRepositoryMetadata = remoteUtils.getRepositoryMetadata( url );
        } catch (Exception e1) {
            postException( e1 );
            return null;
        }
        String newRepositoryID = remoteRepositoryMetadata.getID();

        if (rMgr.getRepository( newRepositoryID ) instanceof RemoteRepository)
            rr = (RemoteRepository) rMgr.getRepository( newRepositoryID );

        // Repo with URL not found, try to add the URL to the repository manager
        if (rr == null)
            try {
                rr = rMgr.addRemoteRepository( url ); // throws error if can not be added
            } catch (RepositoryException e) {
                postException( e );
                return null;
            }
        return rr;
    }

    private RepositoryManager getRepositoryManager() {
        if (repositoryManager == null)
            log.warn( "Repository manger is null. Using default repository manager." );
        try {
            repositoryManager = RepositoryManager.getDefault();
        } catch (RepositoryException e) {
            postException( e );
        }
        return repositoryManager;
    }

    private void postException(Exception e) {
        postException( e, null );
    }

    /**
     * Post in testResults field the exception message and cause if any.
     * 
     * @param e
     * @param operation text added to message before exception messaage
     */
    private void postException(Exception e, String operation) {
        log.error( "Error." );
        StringBuilder errMsg = new StringBuilder( "Error: " );
        if (operation != null)
            errMsg.append( operation );
        errMsg.append( e.getLocalizedMessage() );
        if (e.getCause() != null)
            errMsg.append( "\n" + e.getCause().toString() );

        log.error( errMsg.toString() );
        testResults.setWrapText( true );
        testResults.setText( errMsg.toString() );

    }

    private void repositorySelectionChanged() {
        clear();
        // Try connecting
        String url = loginURLCombo.getValue();
        if (url != null && getRepositoryManager() != null)
            selectedRemoteRepository = getRemoteRepository( repositoryManager, url );

        if (selectedRemoteRepository != null)
            loginRepoID.setText( selectedRemoteRepository.getDisplayName() );
        else {
            loginRepoID.setText( "No valid repository selected." );
            log.warn( "Selected Remote Repository is null." );
        }

        // Set check box to show success or failure
        repoOKCheckbox.setSelected( selectedRemoteRepository != null );
    }

    public String getLoginRepoID() {
        return loginRepoID.getText();
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        dialogButtonTest.setOnAction( e -> doTest() );

        try {
            repositoryManager = RepositoryManager.getDefault();
        } catch (RepositoryException e1) {
            postException( e1 );
            return;
        }
        // FIXME - get the starting credentials so there can be a cancel
        // Requires repository change
        // startingCredentials = rMgr.get
        configureRepositoryCombo();

        anonymousSelectionChanged();
        dialogButtonAnonymous.setOnAction( e -> anonymousSelectionChanged() );
    }
}
