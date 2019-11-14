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
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexRepositoryItemReplacedEvent;
import org.opentravel.dex.events.DexRepositoryItemSelectionEvent;
import org.opentravel.dex.events.DexRepositoryNamespaceSelectionEvent;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.util.HashMap;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

/**
 * Tree table controller for all libraries in a namespace view. Creates table containing repository item properties.
 * <p>
 * This class is designed to be injected into a parent controller by FXML loader. It has a VBOX containing the label
 * header and a tree table view.
 * 
 * @author dmh
 *
 */
public class NamespaceLibrariesTreeTableController extends DexIncludedControllerBase<NamespacesDAO> {
    private static Log log = LogFactory.getLog( NamespaceLibrariesTreeTableController.class );

    private static final EventType[] publishedEvents = {DexRepositoryItemSelectionEvent.REPOSITORY_ITEM_SELECTED,
        DexRepositoryItemReplacedEvent.REPOSITORY_ITEM_REPLACED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] subscribedEvents = {DexRepositoryNamespaceSelectionEvent.REPOSITORY_NS_SELECTED,
        DexRepositoryItemReplacedEvent.REPOSITORY_ITEM_REPLACED};

    // Injected fields
    @FXML
    private TreeTableView<RepoItemDAO> nsLibrariesTreeTableView;

    @FXML
    private Label permissionLabel;
    @FXML
    private Label namespaceLabel;

    private TreeItem<RepoItemDAO> root;
    private NamespacesDAO currentNamespaceDAO = null;

    public NamespaceLibrariesTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create Columns and set cell values
     */
    private void buildColumns(TreeTableView<RepoItemDAO> table) {
        TreeTableColumn<RepoItemDAO,String> fileCol = new TreeTableColumn<>( "Library" );
        fileCol.setCellValueFactory( new TreeItemPropertyValueFactory<RepoItemDAO,String>( "libraryName" ) );
        setColumnProps( fileCol, true, false, true, 250 );

        TreeTableColumn<RepoItemDAO,String> versionCol = new TreeTableColumn<>( "Version" );
        versionCol.setCellValueFactory( new TreeItemPropertyValueFactory<RepoItemDAO,String>( "version" ) );
        setColumnProps( versionCol, true, false, true, 0 );

        TreeTableColumn<RepoItemDAO,String> statusCol = new TreeTableColumn<>( "Status" );
        statusCol.setCellValueFactory( new TreeItemPropertyValueFactory<RepoItemDAO,String>( "status" ) );
        setColumnProps( statusCol, true, false, true, 0 );

        TreeTableColumn<RepoItemDAO,String> lockedCol = new TreeTableColumn<>( "Locked By" );
        lockedCol.setCellValueFactory( new TreeItemPropertyValueFactory<RepoItemDAO,String>( "locked" ) );
        setColumnProps( lockedCol, true, false, true, 0 );

        TreeTableColumn<RepoItemDAO,String> remarkCol = new TreeTableColumn<>( "Last Remark" );
        remarkCol.setCellValueFactory( new TreeItemPropertyValueFactory<RepoItemDAO,String>( "history" ) );
        setColumnProps( remarkCol, true, false, true, 300 );

        table.getColumns().setAll( fileCol, versionCol, statusCol, lockedCol, remarkCol );
    }

    @Override
    public void checkNodes() {
        if (!(nsLibrariesTreeTableView instanceof TreeTableView))
            throw new IllegalStateException( "Libraries tree table not injected." );
        if (!(permissionLabel instanceof Label))
            throw new IllegalStateException( "Permission label not injected." );
        if (!(namespaceLabel instanceof Label))
            throw new IllegalStateException( "Namespace label not injected." );
        // log.debug("Constructing namespace libraries tree controller.");
    }

    @Override
    public void clear() {
        nsLibrariesTreeTableView.getRoot().getChildren().clear();
    }

    @Override
    public void configure(DexMainController main) {
        super.configure( main );
        eventPublisherNode = nsLibrariesTreeTableView;

        // Super.configure assures tree view is not null
        nsLibrariesTreeTableView.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> repoItemSelectionListener( newValue ) );

    }

    public RepoItemDAO getSelectedItem() {
        return nsLibrariesTreeTableView.getSelectionModel().getSelectedItem().getValue();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug("Received event: " + event.getClass().getSimpleName());
        if (event instanceof DexRepositoryNamespaceSelectionEvent)
            handleEvent( (DexRepositoryNamespaceSelectionEvent) event );
        else if (event instanceof DexRepositoryItemReplacedEvent)
            handleEvent( (DexRepositoryItemReplacedEvent) event );
    }

    private void handleEvent(DexRepositoryNamespaceSelectionEvent event) {
        // log.debug("Namespace selected.");
        try {
            post( event.getValue() );
        } catch (Exception e) {
            mainController.postError( e, "Error displaying repository namespace" );
        }
    }

    private void handleEvent(DexRepositoryItemReplacedEvent event) {
        currentNamespaceDAO = null;
    }

    @Override
    public void initialize() {
        // log.debug("Initializing namespace libraries tree controller.");

        // Initialize and build columns for library tree table
        root = initializeTree();
        buildColumns( nsLibrariesTreeTableView );

    }

    private TreeItem<RepoItemDAO> initializeTree() {
        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded
        // Set up the TreeTable
        nsLibrariesTreeTableView.setRoot( root );
        nsLibrariesTreeTableView.setShowRoot( false );
        nsLibrariesTreeTableView.setEditable( false );

        // Enable context menus at the row level and add change listener for for applying style
        nsLibrariesTreeTableView
            .setRowFactory( (TreeTableView<RepoItemDAO> p) -> new NamespaceLibrariesRowFactory( this ) );
        return root;
    }

    @Override
    public void post(NamespacesDAO nsNode) throws Exception {
        super.post( nsNode );
        log.debug( "Posting new namespace node: " + nsNode );
        currentNamespaceDAO = nsNode;
        // Clear the table
        clear();

        if (nsNode == null || nsNode.getFullPath() == null || nsNode.getFullPath().isEmpty()) {
            // throw new IllegalArgumentException( "Missing repository and namespace." );
            log.debug( "Skipping post - nsNode or full path is missing." );
            return;
        }
        // Display the namespace and permission
        namespaceLabel.textProperty().bind( nsNode.fullPathProperty() );
        permissionLabel.textProperty().bind( nsNode.permissionProperty() );

        // Get a table of the latest of each library of any status
        HashMap<String,TreeItem<RepoItemDAO>> latestVersions = new HashMap<>();
        if (nsNode.getLatestItems() != null)
            for (RepositoryItem ri : nsNode.getLatestItems()) {
                RepoItemDAO repoItemNode = new RepoItemDAO( ri, mainController.getStatusController() );
                TreeItem<RepoItemDAO> treeItem = new TreeItem<>( repoItemNode );
                treeItem.setExpanded( true );
                root.getChildren().add( treeItem );
                latestVersions.put( ri.getLibraryName(), treeItem );
            }

        if (nsNode.getAllItems() != null)
            for (RepositoryItem rItem : nsNode.getAllItems()) {
                if (latestVersions.containsKey( rItem.getLibraryName() )) {
                    RepoItemDAO parent = latestVersions.get( rItem.getLibraryName() ).getValue();
                    if (!parent.versionProperty().get().equals( rItem.getVersion() )) {
                        RepoItemDAO repoItemNode = new RepoItemDAO( rItem, mainController.getStatusController() );
                        TreeItem<RepoItemDAO> treeItem = new TreeItem<>( repoItemNode );
                        latestVersions.get( rItem.getLibraryName() ).getChildren().add( treeItem );
                    }
                }
            }
    }

    @Override
    public void refresh() {
        if (currentNamespaceDAO != null) {
            currentNamespaceDAO.refresh( this );
            try {
                post( currentNamespaceDAO );
            } catch (Exception e) {
                log.error( "Error refreshing namespace libraries tree table: " + e.getLocalizedMessage() );
            }
        }
    }

    /**
     * Respond to a selection in the table.
     * 
     * @param newValue
     * @return
     */
    private void repoItemSelectionListener(TreeItem<RepoItemDAO> newValue) {
        if (newValue != null && newValue.getValue() != null)
            eventPublisherNode.fireEvent( new DexRepositoryItemSelectionEvent( this, newValue.getValue() ) );
    }

}
