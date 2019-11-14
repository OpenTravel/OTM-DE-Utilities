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
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.SearchRepositoryTask;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.util.Map;
import java.util.Map.Entry;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

/**
 * Manage the repository search panel
 * 
 * {@link RemoteRepository#getEntityWhereUsed(org.opentravel.schemacompiler.model.NamedEntity, boolean)}
 *
 * @author dmh
 *
 */
// TODO - remove its FXML file
@Deprecated
public class RepositorySearchController extends DexIncludedControllerBase<RepositoryManager>
    implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( RepositorySearchController.class );

    @FXML
    private ChoiceBox<String> statusChoice;
    @FXML
    private TextField searchTerm;
    @FXML
    private Button doSearch;
    @FXML
    private Button clearSearch;
    @FXML
    private RadioButton lastestOnlyRadio;
    @FXML
    private RadioButton lockedRadio;

    private Repository currentRepository;

    private Map<String,RepositoryItem> currentFilterMap;

    @Override
    public void checkNodes() {
        if (searchTerm == null)
            throw new IllegalStateException( "Null search term in repository search controller." );
        if (doSearch == null)
            throw new IllegalStateException( "Null search button in repository search controller." );
        if (clearSearch == null)
            throw new IllegalStateException( "Null clear search button in repository search controller." );

        log.debug( "FXML Nodes checked OK." );
    }

    public RepositorySearchController() {
        log.debug( "Starting constructor." );
    }

    @Override
    @FXML
    public void initialize() {
        log.debug( "Repository Selection Controller initialized." );
    }

    /**
     * @param repository
     * 
     */
    public void setRepository(Repository repository) {
        currentRepository = repository;
    }

    /**
     */
    public void configure() {
        checkNodes(); // Verify FXML loaded correctly

        doSearch.setOnAction( this::runSearch );
        lockedRadio.setOnAction( this::setLockedFilter );
        clearSearch.setOnAction( this::clearFilter );
        log.debug( "Repository Search Stage set." );
    }

    private void clearFilter(ActionEvent event) {
        currentFilterMap = null;
        searchTerm.setText( "" );
        refreshParent();
    }

    private void runSearch(ActionEvent event) {
        RepositorySearchCriteria criteria = new RepositorySearchCriteria( currentRepository, searchTerm.getText() );
        mainController.postStatus( "Searching for: " + criteria.getQuery() );
        // if (currentRepository != null)
        // todo new SearchRepositoryTask( criteria, this::handleTaskComplete, null, null ).go();
    }

    private void setLockedFilter(ActionEvent event) {

    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        log.debug( "Search returned." );
        if (event.getTarget() instanceof SearchRepositoryTask) {
            SearchRepositoryTask task = ((SearchRepositoryTask) event.getTarget());
            // currentFilterMap = task.getFilterMap();
        }

        refreshParent();

        printFilter();
        // parentController.clear();
        // try {
        // parentController.getRepositoryNamespacesController().post(currentRepository);
        // } catch (Exception e) {
        // log.error("Error posting repository namespaces: " + e.getLocalizedMessage());
        // }
    }

    private void refreshParent() {
        // FIXME - proper command/control structure/delegation
        mainController.postStatus( "Done." );
        mainController.clear();
        // try {
        // parentController.getRepositoryNamespacesController().post(currentRepository);
        // } catch (Exception e) {
        // log.error("Error posting repository namespaces: " + e.getLocalizedMessage());
        // }

    }

    public Map<String,RepositoryItem> getFilter() {
        return currentFilterMap;
    }

    /**
     * 
     * @param namespace
     * @return true if the map contains the passed namespace as a key
     */
    public boolean isSelected(String namespace) {
        if (currentFilterMap != null)
            log.debug( "Is " + namespace + "\t contained in map?\t" + currentFilterMap.containsKey( namespace ) );
        // return currentFilterMap != null ? currentFilterMap.containsKey(namespace) : true;
        return currentFilterMap == null || currentFilterMap.containsKey( namespace );
    }

    public boolean isSelected(RepositoryItem item) {
        return currentFilterMap == null || currentFilterMap.containsValue( item );
    }

    private void printFilter() {
        log.debug( "\nCurrent Filter Entries" );
        if (currentFilterMap != null)
            for (Entry<String,RepositoryItem> entry : currentFilterMap.entrySet())
                log.debug( entry.getKey() + " \t " + entry.getValue().getLibraryName() );
    }

    /**
     * Remove all items from the tables
     */
    @Override
    public void clear() {
        // TODO
    }

    @Override
    public void post(RepositoryManager repositoryManager) throws Exception {
        // does nothing
    }

    // @Override
    // public ReadOnlyObjectProperty<String> getSelectable() {
    // return null;
    // }

}
