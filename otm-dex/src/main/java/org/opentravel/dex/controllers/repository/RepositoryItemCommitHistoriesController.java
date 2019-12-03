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
import org.opentravel.dex.events.DexRepositoryItemSelectionEvent;
import org.opentravel.dex.events.DexRepositoryNamespaceSelectionEvent;
import org.opentravel.dex.events.DexRepositorySelectionEvent;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for a library history table. Creates table containing library history properties.
 * 
 * @author dmh
 *
 */
public class RepositoryItemCommitHistoriesController extends DexIncludedControllerBase<RepoItemDAO> {
    private static Log log = LogFactory.getLog( RepositoryItemCommitHistoriesController.class );

    private static final EventType[] subscribedEvents = {DexRepositoryItemSelectionEvent.REPOSITORY_ITEM_SELECTED,
        DexRepositoryNamespaceSelectionEvent.REPOSITORY_NS_SELECTED, DexRepositorySelectionEvent.REPOSITORY_SELECTED};

    @FXML
    public TableView<RepoItemCommitDAO> commitHistoriesTable;
    private TableView<RepoItemCommitDAO> historyTable;

    private ObservableList<RepoItemCommitDAO> commitList = FXCollections.observableArrayList();

    public RepositoryItemCommitHistoriesController() {
        super( subscribedEvents );
    }

    /**
     * Create Columns and set cell values
     */
    private void buildColumns(TableView<RepoItemCommitDAO> table) {
        TableColumn<RepoItemCommitDAO,String> numCol = new TableColumn<>( "Number" );
        numCol.setCellValueFactory( new PropertyValueFactory<RepoItemCommitDAO,String>( "number" ) );
        setColumnProps( numCol, true, false, true, 0 );

        TableColumn<RepoItemCommitDAO,String> dateCol = new TableColumn<>( "Date" );
        dateCol.setCellValueFactory( new PropertyValueFactory<RepoItemCommitDAO,String>( "effective" ) );
        setColumnProps( dateCol, true, false, true, 250 );

        TableColumn<RepoItemCommitDAO,String> userCol = new TableColumn<>( "User" );
        userCol.setCellValueFactory( new PropertyValueFactory<RepoItemCommitDAO,String>( "user" ) );
        setColumnProps( userCol, true, false, true, 150 );

        TableColumn<RepoItemCommitDAO,String> remarksCol = new TableColumn<>( "Remarks" );
        remarksCol.setCellValueFactory( new PropertyValueFactory<RepoItemCommitDAO,String>( "remarks" ) );
        setColumnProps( remarksCol, true, false, true, 0 );

        table.getColumns().setAll( remarksCol, userCol, dateCol, numCol );
    }

    @Override
    public void checkNodes() {
        if (!(commitHistoriesTable instanceof TableView))
            throw new IllegalStateException( "Commit histories table not injected by FXML." );
    }

    @Override
    public void clear() {
        historyTable.getItems().clear();
    }

    public void eventHandler(DexRepositoryItemSelectionEvent event) {
        try {
            post( event.getValue() );
        } catch (Exception e) {
            mainController.postError( e, "Error displaying repository item history" );
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent e) {
        if (e instanceof DexRepositoryItemSelectionEvent)
            eventHandler( (DexRepositoryItemSelectionEvent) e );
        else if (e instanceof DexRepositoryNamespaceSelectionEvent)
            clear();
        else if (e instanceof DexRepositorySelectionEvent)
            clear();
    }

    @Override
    public void initialize() {
        log.debug( "Initializing repository library table view." );

        this.historyTable = commitHistoriesTable;
        if (historyTable == null)
            throw new IllegalStateException( "Library History Table view is null." );

        // Initialize and build columns for library tree table
        buildColumns( historyTable );

        // Have table listen to observable list.
        historyTable.setItems( commitList );
    }

    @Override
    public void post(RepoItemDAO repoItem) {
        super.post( repoItem );

        if (repoItem == null)
            throw new IllegalArgumentException( "Missing repo item." );
        RepositoryItemHistory history = repoItem.getHistory();
        if (history == null) {
            mainController.postError( null, "History could not be retrieved." );
            return;
        }
        for (RepositoryItemCommit cItem : history.getCommitHistory()) {
            commitList.add( new RepoItemCommitDAO( cItem ) );
        }
    }

    @Override
    public void refresh() {
        try {
            post( postedData );
        } catch (Exception e) {
            // log.error( "Unhandled error refreshing repository item commit history: " + e.getLocalizedMessage() );
        }
    }
}
