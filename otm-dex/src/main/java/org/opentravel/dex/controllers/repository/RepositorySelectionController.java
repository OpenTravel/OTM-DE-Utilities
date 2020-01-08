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

package org.opentravel.dex.controllers.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.RepositoryLoginDialogContoller;
import org.opentravel.dex.events.DexRepositorySelectionEvent;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

/**
 * Manage the repository selection choice, user, password bar.
 * 
 * @author dmh
 *
 */
public class RepositorySelectionController extends DexIncludedControllerBase<RepositoryManager> {
    private static Log log = LogFactory.getLog( RepositorySelectionController.class );

    private static final String LOCAL_REPO = "Local";

    @FXML
    private ChoiceBox<String> repositoryChoice;
    @FXML
    private Label repositoryUser;
    @FXML
    private Button addRepository;

    private RepositoryManager repositoryManager;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexRepositorySelectionEvent.REPOSITORY_SELECTED};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexRepositorySelectionEvent.REPOSITORY_SELECTED};

    public RepositorySelectionController() {
        super( subscribedEvents, publishedEvents );
        // log.debug( "Starting constructor." );
    }

    private void addRepository() {
        // initialize login Dialog Box using a new dynamic loader
        RepositoryLoginDialogContoller loginDialogController = RepositoryLoginDialogContoller.init( repositoryManager );

        if (loginDialogController.showAndWait( "" ) == Results.OK) {
            repositorySelectionChanged(); // update user field
            // repositoryChoice.getSelectionModel().select(loginDialogController.getLoginRepoID());
        }
    }

    @Override
    public void checkNodes() {
        if (repositoryChoice == null)
            throw new IllegalStateException( "Null repository choice node in repository controller." );
        if (repositoryUser == null)
            throw new IllegalArgumentException( "repositoryUser is null." );
        // log.debug("FXML Nodes checked OK.");
    }

    /**
     * Remove all items from the tables
     */
    @Override
    public void clear() {
        // No-op
    }

    private void configureRepositoryChoice() {
        // log.debug( "Configuring repository choice box." );

        ObservableList<String> repositoryIds = FXCollections.observableArrayList();
        repositoryIds.add( LOCAL_REPO );
        repositoryManager.listRemoteRepositories().forEach( r -> repositoryIds.add( r.getId() ) );
        repositoryChoice.setItems( repositoryIds );
        repositoryChoice.getSelectionModel().select( 0 );

        // Configure listener for choice box
        repositoryChoice.valueProperty()
            .addListener( (observable, oldValue, newValue) -> repositorySelectionChanged() );
        // log.debug( "Repository choice has " + repositoryIds.size() + " items." );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexRepositorySelectionEvent) {
            String rid = "";
            Repository repo = ((DexRepositorySelectionEvent) event).getRepository();
            if (repo != null)
                rid = repo.getId();
            if (!rid.isEmpty())
                repositoryChoice.getSelectionModel().select( rid );
        }
    }


    /**
     * Get the selected repository or default repository if none selected.
     * 
     * @throws RepositoryException
     */
    public Repository getSelectedRepository() throws RepositoryException {
        Repository repository = RepositoryManager.getDefault();
        String rid = repositoryChoice.getSelectionModel().getSelectedItem();
        if (rid != null)
            if (rid.equals( LOCAL_REPO ))
                repository = RepositoryManager.getDefault();
            else
                // Use selected repository
                repository = repositoryManager.getRepository( rid );
        return repository;
    }

    @Override
    @FXML
    public void initialize() {
        // log.debug( "Repository Selection Controller initialized." );
    }

    @Override
    public void post(RepositoryManager repositoryManager) {
        super.post( repositoryManager );
        if (repositoryManager != null)
            this.repositoryManager = repositoryManager;
        // does not really do anything -- the local repository acts as default manager.
    }

    /**
     * Get the user from the repository and post in the repository user field.
     * 
     * @param repository
     */
    private void postUser(Repository repository) {
        String user = "--local--";
        if (repository instanceof RemoteRepositoryClient)
            user = ((RemoteRepositoryClient) repository).getUserId();
        repositoryUser.setText( user );
    }

    /**
     * Called when the user modifies the selection of the 'repositoryChoice' control.
     * 
     * @throws RepositoryException
     */
    private void repositorySelectionChanged() {
        // log.debug( "Selected new repository" );
        try {
            postUser( getSelectedRepository() );
            if (getMainController().getUserSettings() != null)
                getMainController().getUserSettings().setLastRepositoryId( getSelectedRepository().getId() );
            repositoryChoice.fireEvent( new DexRepositorySelectionEvent( getSelectedRepository() ) );
        } catch (Exception e) {
            log.error( "Error posting repository: " + e.getLocalizedMessage() );
        }
    }

    /**
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = repositoryChoice;

        repositoryManager = parent.getRepositoryManager();
        configureRepositoryChoice();

        // // initialize login Dialog Box using a new dynamic loader
        // loginDialogController = RepositoryLoginDialogContoller.init( repositoryManager );
        addRepository.setOnAction( e -> addRepository() );

        // log.debug( "Repository Selection configured." );
    }

}
