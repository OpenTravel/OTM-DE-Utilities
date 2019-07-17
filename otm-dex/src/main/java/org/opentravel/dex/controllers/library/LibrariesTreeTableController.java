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
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;

import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Manage the tree table view for libraries in projects (Library Tab)
 * 
 * @author dmh
 *
 */
public class LibrariesTreeTableController extends DexIncludedControllerBase<OtmModelManager> {
    private static Log log = LogFactory.getLog( LibrariesTreeTableController.class );

    // TODO - use properties
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

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexLibrarySelectionEvent.LIBRARY_SELECTED};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexLibrarySelectionEvent.LIBRARY_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    // Editable Columns
    // None

    private OtmModelManager modelMgr;

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

        modelMgr = parent.getModelManager();
        if (modelMgr == null)
            throw new IllegalStateException( "Model manager is null but needed for the library view controller." );

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded
        librariesTreeTable.setRoot( root );
        librariesTreeTable.setShowRoot( false );
        librariesTreeTable.setEditable( true );
        // libraryTree.getSelectionModel().setCellSelectionEnabled(true); // allow individual cells to be edited
        librariesTreeTable.setTableMenuButtonVisible( true ); // allow users to select columns

        // add a listener for tree selections
        librariesTreeTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, o, n) -> librarySelectionListener( n ) );

        // Set up the TreeTable
        buildColumns();

        // Enable context menus at the row level and add change listener for for applying style
        librariesTreeTable.setRowFactory( (TreeTableView<LibraryDAO> p) -> new LibraryRowFactory( this ) );

        // create cells for members
        for (OtmLibrary lib : modelMgr.getLibraries()) {
            createTreeItem( lib, root );
        }
    }

    public void setSelectionListener(ChangeListener<TreeItem<LibraryDAO>> listener) {
        librariesTreeTable.getSelectionModel().selectedItemProperty().addListener( listener );
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmModelManager modelMgr) {
        if (modelMgr != null)
            this.modelMgr = modelMgr;
        refresh();
    }

    @Override
    public void refresh() {
        log.debug( "Refreshing library tree table." );
        // create cells for libraries in a namespace. Latest at top, older ones under it.
        librariesTreeTable.getRoot().getChildren().clear();
        for (String baseNS : modelMgr.getBaseNamespaces()) {
            TreeItem<LibraryDAO> latestItem = null;
            OtmLibrary latest = null;
            Set<OtmLibrary> libs = modelMgr.getLibraryChain( baseNS );
            for (OtmLibrary lib : libs)
                if (lib != null && lib.isLatestVersion()) {
                    latestItem = createTreeItem( lib, root );
                    latest = lib;
                }
            // Put 1st item at root, all rest under it.
            if (latest != null)
                for (OtmLibrary lib : libs)
                    if (lib != latest)
                        createTreeItem( lib, latestItem );
        }
    }

    private boolean ignore = false;

    @Override
    public void handleEvent(Event event) {
        if (event instanceof DexLibrarySelectionEvent)
            eventHandler( ((DexLibrarySelectionEvent) event) );
        if (event instanceof DexModelChangeEvent)
            post( ((DexModelChangeEvent) event).getModelManager() );
    }

    public void eventHandler(DexLibrarySelectionEvent event) {
        OtmLibrary selectedLib = event.getLibrary();
        if (selectedLib != null) {
            // log.debug("Library selection Listener: " + selectedLib.getName());
            for (TreeItem<LibraryDAO> item : librariesTreeTable.getRoot().getChildren())
                if (item.getValue().getValue() == selectedLib) {
                    ignore = true;
                    librariesTreeTable.getSelectionModel().select( item );
                    ignore = false;
                }
        } else {
            librariesTreeTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Listener for selected library members.
     *
     * @param item
     */
    private void librarySelectionListener(TreeItem<LibraryDAO> item) {
        if (item == null || item.getValue() == null || item.getValue().getValue() == null)
            return;

        // log.debug("Selection Listener: " + item.getValue().getValue().getName());

        if (!ignore)
            libraries.fireEvent( new DexLibrarySelectionEvent( libraries, item ) );
    }

    //
    // Create columns
    //
    private void buildColumns() {
        TreeTableColumn<LibraryDAO,String> prefixColumn =
            createStringColumn( PREFIXCOLUMNLABEL, "prefix", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,String> nameColumn = createStringColumn( NAMELABEL, "name", true, false, true, 200 );
        TreeTableColumn<LibraryDAO,String> namespaceColumn =
            createStringColumn( NAMESPACELABEL, "namespace", true, false, true, 250 );
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

    /**
     * TreeItem class does not extend the Node class.
     * 
     * Therefore, you cannot apply any visual effects or add menus to the tree items. Use the cell factory mechanism to
     * overcome this obstacle and define as much custom behavior for the tree items as your application requires.
     * 
     * @param item
     * @return
     */
    private TreeItem<LibraryDAO> createTreeItem(OtmLibrary library, TreeItem<LibraryDAO> parent) {
        if (library != null) {
            TreeItem<LibraryDAO> item = new TreeItem<>( new LibraryDAO( library ) );
            item.setExpanded( false );
            parent.getChildren().add( item );
            return item;
        }
        return null;
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        librariesTreeTable.getRoot().getChildren().clear();
    }

    /**
     * @return
     */
    public LibraryDAO getSelectedItem() {
        return librariesTreeTable.getSelectionModel().getSelectedItem().getValue();
    }

}
