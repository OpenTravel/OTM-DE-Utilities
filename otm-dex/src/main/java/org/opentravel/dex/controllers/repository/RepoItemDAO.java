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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.GetRepositoryItemHistoryTask;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.image.ImageView;

/**
 * Controller for a libraries in a namespace tree table view. Creates table containing repository item properties.
 * <p>
 * This class is designed to be injected into a parent controller by FXML loader. It has a VBOX containing the label
 * header and a tree table view.
 * 
 * @author dmh
 *
 */
public class RepoItemDAO implements DexDAO<RepositoryItem>, TaskResultHandlerI {
    private static Logger log = LogManager.getLogger( RepoItemDAO.class );

    protected RepositoryItem repoItem;
    SimpleStringProperty lastHistory = new SimpleStringProperty( ":> working..." );
    Double historyProgess = 1.0;

    RepositoryItemHistory history = null;

    public RepoItemDAO(RepositoryItem item, DexStatusController dexStatusController) {
        this.repoItem = item;

        // Start a task to retrieve history
        new GetRepositoryItemHistoryTask( repoItem, this::handleTaskComplete, dexStatusController ).go();
    }

    // Handle setting history with results from task
    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        if (event.getTarget() instanceof GetRepositoryItemHistoryTask) {
            // log.debug("Handling get history task results");
            setHistory( ((GetRepositoryItemHistoryTask) event.getTarget()).getHistory() );
        }
    }

    public StringProperty libraryNameProperty() {
        return new SimpleStringProperty( repoItem.getLibraryName() );
    }

    public StringProperty versionProperty() {
        return new SimpleStringProperty( repoItem.getVersion() );
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty( repoItem.getStatus().toString() );
    }

    public StringProperty lockedProperty() {
        // log.debug(
        // repoItem.getLibraryName() + " is locked by " + repoItem.getLockedByUser() + " " + repoItem.hashCode());
        return new SimpleStringProperty( repoItem.getLockedByUser() );
    }

    public boolean isLocked() {
        return repoItem.getLockedByUser() != null;
    }

    public StringProperty historyProperty() {
        return lastHistory;
    }

    public void setHistory(RepositoryItemHistory history) {
        this.history = history;
        setHistory();
    }

    public void setHistory() {
        if (history == null)
            return;
        StringBuilder remark = new StringBuilder( history.getCommitHistory().get( 0 ).getUser() );
        remark.append( " - " );
        remark.append( history.getCommitHistory().get( 0 ).getRemarks() );
        lastHistory.set( remark.toString() );
        // log.debug("History set: " + remark.toString());
    }

    /**
     * Background thread ready getter for the history of this repository item.
     * 
     * @param repoItem
     * @param value
     * @return the history item if already retrieved or starts a background task to retrieve it.
     */
    public RepositoryItemHistory getHistory() {
        if (history != null)
            return history;
        // log.debug("Finding history item for " + repoItem.getFilename());
        try {
            history = repoItem.getRepository().getHistory( repoItem );
        } catch (RepositoryException e) {
        }
        return null;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return null;
    }

    @Override
    public RepositoryItem getValue() {
        return repoItem;
    }

    public void setValue(RepositoryItem item) {
        repoItem = item;
    }

    public String getRepositoryURL() {
        // get the actual repository client
        Repository r = repoItem.getRepository();
        RemoteRepositoryClient repoClient = null;
        if (r instanceof RemoteRepositoryClient)
            repoClient = (RemoteRepositoryClient) r;

        if (repoClient == null)
            return "";

        // Get the full URL
        // http://opentravelmodel.net/console/libraryDictionary.html?baseNamespace=http%3a%2f%2fwww.opentravel.org%2fSandbox%2fdemo&filename=SteveTest_0_0_0.otm&version=0.0.0
        String rn = ((RemoteRepositoryClient) r).getEndpointUrl();
        String path = "/console/libraryDictionary.html?";
        String bns = repoItem.getBaseNamespace();
        String fn = repoItem.getFilename();
        String vn = repoItem.getVersion();
        String url = rn + path + "baseNamespace=" + bns + "&filename=" + fn + "&version=" + vn;


        final String password = repoClient.getEncryptedPassword();
        final String username = repoClient.getUserId();

        // Create authenticator
        URI baseUri = URI.create( rn );
        // log.debug( "Creating authenticator for " + baseUri.toString() );
        Authenticator.setDefault( new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                // only return our credentials for our URI
                if (baseUri.getHost().equals( getRequestingHost() )) {
                    return new PasswordAuthentication( username, password.toCharArray() );
                }
                return null;
            }
        } );

        return url;
    }
}
