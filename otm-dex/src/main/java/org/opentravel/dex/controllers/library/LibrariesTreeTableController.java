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

package org.opentravel.dex.controllers.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;

import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage the tree table view for libraries in projects (Library Tab)
 * 
 * @author dmh
 *
 */
public class LibrariesTreeTableController extends DexIncludedControllerBase<OtmModelManager> {
    private static Log log = LogFactory.getLog( LibrariesTreeTableController.class );

    public static final String PREFIXCOLUMNLABEL = "Prefix";
    private static final String NAMELABEL = "Name";
    private static final String NAMESPACELABEL = "Namespace";
    private static final String VERSIONLABEL = "Version";
    private static final String EDITABLELABEL = "Editable";
    private static final String STATUSLABEL = "Status";
    private static final String REFERENCELABEL = "References";
    private static final String STATELABEL = "State";
    private static final String LOCKEDLABEL = "Locked-by";
    private static final String READONLYLABEL = "Read-only";
    private static final String PROJECTSLABEL = "Projects";

    @FXML
    private TreeTableView<LibraryDAO> librariesTreeTable;
    @FXML
    private VBox libraries;

    private TreeItem<LibraryDAO> root; // Root of the tree.
    private boolean ignoreEvents = false;
    private boolean editableOnlyFilter = false;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexLibrarySelectionEvent.LIBRARY_SELECTED};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexLibrarySelectionEvent.LIBRARY_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    // Editable Columns
    TreeTableColumn<LibraryDAO,String> nameColumn;

    public LibrariesTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (!(librariesTreeTable instanceof TreeTableView))
            throw new IllegalStateException( "Library tree table controller not injected by FXML." );
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = libraries;
        if (parent != null)
            postedData = parent.getModelManager();
        configure( postedData, false );
    }

    /**
     * Configuration for non-main controllers. Call after init() and before posting content.
     * 
     * @param parent
     * @param editableOnly
     */
    public void configure(OtmModelManager modelManager, boolean editableOnly) {
        if (modelManager != null)
            this.postedData = modelManager;
        this.editableOnlyFilter = editableOnly;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded
        librariesTreeTable.setRoot( root );
        librariesTreeTable.setShowRoot( false );
        librariesTreeTable.setEditable( true );
        librariesTreeTable.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        librariesTreeTable.setTableMenuButtonVisible( true ); // allow users to select columns

        // add a listener for tree selections
        librariesTreeTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, o, n) -> librarySelectionListener( n ) );

        // Enable context menus at the row level and add change listener for for applying style
        librariesTreeTable.setRowFactory( (TreeTableView<LibraryDAO> p) -> new LibraryRowFactory( this ) );

        // Set up the TreeTable
        buildColumns();

        refresh();
        // log.debug( "Configured Libraries Tree Table." );
    }

    public void setSelectionListener(ChangeListener<TreeItem<LibraryDAO>> listener) {
        librariesTreeTable.getSelectionModel().selectedItemProperty().addListener( listener );
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
        librariesTreeTable.setOnMouseClicked( handler );
    }


    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmModelManager modelMgr) {
        // log.debug( "Posting all libraries." );
        ignoreEvents = true;
        if (modelMgr != null) {
            postedData = modelMgr;
            clear();

            // log.debug( "Posting library tree table. Ready to post " + modelMgr.getBaseNamespaces().size()
            // + " base namespaces." );
            // modelMgr.printLibraries();

            // create cells for libraries in a namespace. Latest at top, older ones under it.
            for (String baseNS : modelMgr.getBaseNamespaces()) {
                log.debug( "Posting base namespace: " + baseNS );
                TreeItem<LibraryDAO> latestItem = null;
                OtmLibrary latest = null;
                Set<OtmLibrary> libs = modelMgr.getLibraryChain( baseNS );
                for (OtmLibrary lib : libs)
                    if (lib != null && lib.isLatestVersion()) {
                        if (!editableOnlyFilter || lib.isEditable())
                            latestItem = new LibraryDAO( lib ).createTreeItem( root );
                        latest = lib;
                    }
                // Put 1st item at root, all rest under it.
                if (latest != null)
                    for (OtmLibrary lib : libs)
                        if (lib != latest)
                            if (!editableOnlyFilter || lib.isEditable())
                                new LibraryDAO( lib ).createTreeItem( latestItem );
            }
        }
        ignoreEvents = false;
        // log.debug( "Posted library tree." );
    }

    @Override
    public void refresh() {
        post( postedData );
    }

    private boolean ignore = false;

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexLibrarySelectionEvent)
            handleLibrarySelection( ((DexLibrarySelectionEvent) event) );
        if (event instanceof DexModelChangeEvent)
            post( ((DexModelChangeEvent) event).getModelManager() );
    }

    public void handleLibrarySelection(DexLibrarySelectionEvent event) {
        // Do NOT respond to filter or other library selection.
        // If this is enabled, guard against indexOutOfBounds exceptions
        //
        // OtmLibrary selectedLib = event.getLibrary();
        // if (selectedLib != null) {
        // // log.debug("Library selection Listener: " + selectedLib.getName());
        // for (TreeItem<LibraryDAO> item : librariesTreeTable.getRoot().getChildren())
        // if (item.getValue().getValue() == selectedLib) {
        // ignore = true;
        // librariesTreeTable.getSelectionModel().select( item );
        // ignore = false;
        // }
        // } else {
        // librariesTreeTable.getSelectionModel().clearSelection();
        // }
    }

    /**
     * Listener for selected library members.
     *
     * @param item
     */
    private void librarySelectionListener(TreeItem<LibraryDAO> item) {
        if (item == null || item.getValue() == null || item.getValue().getValue() == null)
            return;

        // log.debug( "Selection Listener: " + item.getValue().getValue().getName() );
        // nameColumn.setEditable( true );

        if (!ignore)
            libraries.fireEvent( new DexLibrarySelectionEvent( libraries, item ) );
    }

    //
    // Create columns
    //
    private void buildColumns() {
        TreeTableColumn<LibraryDAO,String> prefixColumn =
            createStringColumn( PREFIXCOLUMNLABEL, "prefix", true, false, true, 0 );
        nameColumn = createStringColumn( NAMELABEL, "name", true, true, true, 200 );
        TreeTableColumn<LibraryDAO,String> namespaceColumn =
            createStringColumn( NAMESPACELABEL, "namespace", true, true, true, 250 );
        TreeTableColumn<LibraryDAO,String> versionColumn =
            createStringColumn( VERSIONLABEL, "version", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,String> statusColumn =
            createStringColumn( STATUSLABEL, "status", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,String> stateColumn =
            createStringColumn( STATELABEL, "state", true, false, true, 150 );
        TreeTableColumn<LibraryDAO,String> editColumn =
            createStringColumn( EDITABLELABEL, "edit", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,String> lockedColumn =
            createStringColumn( LOCKEDLABEL, "locked", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,String> projectsColumn =
            createStringColumn( PROJECTSLABEL, "projects", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,Boolean> readonlyColumn = new TreeTableColumn<>( READONLYLABEL );
        readonlyColumn.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,Boolean>( "readonly" ) );

        TreeTableColumn<LibraryDAO,Integer> refColumn = new TreeTableColumn<>( REFERENCELABEL );
        refColumn.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,Integer>( "reference" ) );
        refColumn.setPrefWidth( 100 );

        librariesTreeTable.getColumns().addAll( nameColumn, prefixColumn, namespaceColumn, versionColumn, statusColumn,
            stateColumn, lockedColumn, projectsColumn, refColumn, readonlyColumn, editColumn );

        // Start out sorted on names
        nameColumn.setSortType( SortType.ASCENDING );
        librariesTreeTable.getSortOrder().add( nameColumn );
    }

    /**
     * Create a treeTableColumn for a String and set properties.
     * 
     * @return
     */
    private TreeTableColumn<LibraryDAO,String> createStringColumn(String label, String propertyName, boolean visable,
        boolean editable, boolean sortable, int width) {
        TreeTableColumn<LibraryDAO,String> c = new TreeTableColumn<>( label );
        c.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,String>( propertyName ) );
        setColumnProps( c, visable, editable, sortable, width );
        return c;
    }

    // /**
    // * TreeItem class does not extend the Node class.
    // *
    // * Therefore, you cannot apply any visual effects or add menus to the tree items. Use the cell factory mechanism
    // to
    // * overcome this obstacle and define as much custom behavior for the tree items as your application requires.
    // *
    // * @param item
    // * @return
    // */
    // private TreeItem<LibraryDAO> createTreeItem(OtmLibrary library, TreeItem<LibraryDAO> parent) {
    // log.debug( "Create tree item for: " + library );
    // if (parent != null && library != null) {
    // TreeItem<LibraryDAO> item = new TreeItem<>( new LibraryDAO( library ) );
    // item.setExpanded( false );
    // parent.getChildren().add( item );
    // return item;
    // }
    // return null;
    // }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        if (librariesTreeTable.getSelectionModel() != null)
            librariesTreeTable.getSelectionModel().clearSelection();

        if (librariesTreeTable.getRoot() != null)
            librariesTreeTable.getRoot().getChildren().clear();
    }

    /**
     * @return
     */
    public LibraryDAO getSelectedItem() {
        return librariesTreeTable.getSelectionModel().getSelectedItem() != null
            ? librariesTreeTable.getSelectionModel().getSelectedItem().getValue() : null;
    }

}
