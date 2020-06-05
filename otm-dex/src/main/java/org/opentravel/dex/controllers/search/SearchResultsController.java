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

package org.opentravel.dex.controllers.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexSearchResultsEvent;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

/**
 * Manage the search results display
 * 
 * @author dmh
 */
public class SearchResultsController extends DexIncludedControllerBase<SearchResultsDAO> {
    private static Log log = LogFactory.getLog( SearchResultsController.class );

    @FXML
    private TreeTableView<SearchResultItemDAO> resultsTreeView;

    protected TreeItem<SearchResultItemDAO> root;
    protected TreeTableColumn<SearchResultItemDAO,String> entityCol;
    protected TreeTableColumn<SearchResultItemDAO,String> typeCol;
    protected TreeTableColumn<SearchResultItemDAO,String> baseNsCol;
    protected TreeTableColumn<SearchResultItemDAO,String> libraryCol;
    protected TreeTableColumn<SearchResultItemDAO,String> versionCol;

    private static final EventType[] subscribedEvents = {DexSearchResultsEvent.SEARCH_RESULTS};
    private static final EventType[] publishedEvents = {};

    @Override
    public void checkNodes() {
        if (resultsTreeView == null)
            throw new IllegalStateException( "Null results tree view in search controller." );
    }

    public SearchResultsController() {
        super( subscribedEvents );
    }

    /**
     * Create Columns and set cell values
     */
    @SuppressWarnings("unchecked")
    private void buildColumns(TreeTableView<SearchResultItemDAO> table) {

        // Entity Column
        entityCol = new TreeTableColumn<>( "Entity" );
        setColumnProps( entityCol, true, true, true, 150, "entity" );
        // Type Column
        typeCol = new TreeTableColumn<>( "Type" );
        setColumnProps( typeCol, true, true, true, 150, "type" );
        // BaseNs Column
        baseNsCol = new TreeTableColumn<>( "Base Namespace" );
        setColumnProps( baseNsCol, true, true, true, 200, "baseNs" );
        // Library Column
        libraryCol = new TreeTableColumn<>( "Library" );
        setColumnProps( libraryCol, true, true, true, 150, "library" );
        // Version Column
        versionCol = new TreeTableColumn<>( "Version" );
        setColumnProps( versionCol, true, true, true, 50, "version" );

        table.getColumns().addAll( entityCol, typeCol, baseNsCol, libraryCol, versionCol );
    }

    private void initializeTable(TreeTableView<SearchResultItemDAO> table) {
        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Start out fully expanded
        // Set up the TreeTable
        table.setRoot( root );
        table.setShowRoot( false );
        table.setEditable( false );
        table.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        table.setTableMenuButtonVisible( true ); // allow users to select columns

        // // Enable context menus at the row level and add change listener for for applying style
        // table.setRowFactory( (TreeTableView<PropertiesDAO> p) -> new MemberPropertiesRowFactory( this ) );

        // Define Columns and cell content providers
        buildColumns( table );
    }

    @Override
    public void clear() {
        resultsTreeView.getRoot().getChildren().clear();
    }

    @Override
    @FXML
    public void initialize() {
        // log.debug( "Search Results Controller initialized." );
    }

    @Override
    public void post(SearchResultsDAO results) {
        super.post( results ); // Clear tree and save results
        // log.debug( "Posting search results." );
        if (results != null)
            results.createTreeItems( resultsTreeView.getRoot() );
    }

    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        eventPublisherNode = resultsTreeView;

        resultsTreeView.setRoot( new TreeItem<SearchResultItemDAO>() );
        resultsTreeView.setShowRoot( false );

        initializeTable( resultsTreeView );
        // log.debug( "Search results Stage set." );
    }


    private void handleEvent(DexSearchResultsEvent event) {
        try {
            post( event.get() );
        } catch (Exception e) {
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. " );
        if (event instanceof DexSearchResultsEvent)
            handleEvent( (DexSearchResultsEvent) event );
        else
            refresh();
    }

    /**
     * Utility to set String column properties and set value to named field.
     * 
     * @param field used in the cell value factory. must have a getter or StringProperty in the DAO with the field name.
     */
    private void setColumnProps(TreeTableColumn<SearchResultItemDAO,String> c, boolean visable, boolean editable,
        boolean sortable, int width, String field) {
        setColumnProps( c, visable, editable, sortable, width );
        c.setCellValueFactory( new TreeItemPropertyValueFactory<SearchResultItemDAO,String>( field ) );
        c.setCellFactory( TextFieldTreeTableCell.forTreeTableColumn() );
    }

}
