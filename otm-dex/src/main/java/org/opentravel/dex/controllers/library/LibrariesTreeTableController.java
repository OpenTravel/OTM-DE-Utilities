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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
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
    private static Logger log = LogManager.getLogger( LibrariesTreeTableController.class );

    public static final String PREFIXCOLUMNLABEL = "Prefix";
    private static final String NAMELABEL = "Name";
    private static final String NAMESPACELABEL = "Namespace";
    private static final String VERSIONLABEL = "Version";
    private static final String EDITABLELABEL = "Editable";
    private static final String STATUSLABEL = "Status";
    private static final String SIZELABEL = "Members";
    private static final String REFERENCELABEL = "References";
    private static final String STATELABEL = "State";
    private static final String LOCKEDLABEL = "Locked-by";
    private static final String READONLYLABEL = "Read-only";
    private static final String PROJECTSLABEL = "Projects";
    private static final String FILELABEL = "File";

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexLibrarySelectionEvent.LIBRARY_SELECTED};
    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexLibrarySelectionEvent.LIBRARY_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    @FXML
    private TreeTableView<LibraryDAO> librariesTreeTable;
    @FXML
    private VBox libraries;
    private TreeItem<LibraryDAO> root; // Root of the tree.


    private boolean editableOnlyFilter = false;

    // private boolean ignoreEvents = false;
    private boolean ignore = false;

    // Editable Columns
    private TreeTableColumn<LibraryDAO,String> nameColumn;
    private TreeTableColumn<LibraryDAO,String> namespaceColumn;
    private TreeTableColumn<LibraryDAO,String> prefixColumn;
    private TreeTableColumn<LibraryDAO,String> versionColumn;

    public LibrariesTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    //
    // Create columns
    //
    private void buildColumns() {
        prefixColumn = createStringColumn( PREFIXCOLUMNLABEL, "prefix", true, true, true, 0 );
        nameColumn = createStringColumn( NAMELABEL, "name", true, true, true, 200 );
        namespaceColumn = createStringColumn( NAMESPACELABEL, "namespace", true, true, true, 250 );
        versionColumn = createStringColumn( VERSIONLABEL, "version", true, true, true, 0 );

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
        TreeTableColumn<LibraryDAO,String> fileColumn =
            createStringColumn( FILELABEL, "fileName", true, false, true, 0 );
        TreeTableColumn<LibraryDAO,Boolean> readonlyColumn = new TreeTableColumn<>( READONLYLABEL );
        readonlyColumn.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,Boolean>( "readonly" ) );

        TreeTableColumn<LibraryDAO,Integer> sizeColumn = new TreeTableColumn<>( SIZELABEL );
        sizeColumn.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,Integer>( "size" ) );
        sizeColumn.setPrefWidth( 100 );
        TreeTableColumn<LibraryDAO,Integer> refColumn = new TreeTableColumn<>( REFERENCELABEL );
        refColumn.setCellValueFactory( new TreeItemPropertyValueFactory<LibraryDAO,Integer>( "reference" ) );
        refColumn.setPrefWidth( 100 );

        librariesTreeTable.getColumns().addAll( nameColumn, prefixColumn, namespaceColumn, versionColumn, statusColumn,
            stateColumn, lockedColumn, projectsColumn, sizeColumn, refColumn, readonlyColumn, editColumn, fileColumn );

        // Start out sorted on names
        nameColumn.setSortType( SortType.ASCENDING );
    }

    @Override
    public void checkNodes() {
        if (!(librariesTreeTable instanceof TreeTableView))
            throw new IllegalStateException( "Library tree table controller not injected by FXML." );
    }

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

    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        eventPublisherNode = libraries;
        if (parent != null)
            postedData = parent.getModelManager();
        configure( postedData, false );
    }


    /**
     * Configuration for non-main controllers. Call after init() and before posting content.
     * 
     * @param sprite
     * @param editableOnly filter setting
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

        librariesTreeTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );

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
        if (editable)
            c.setCellFactory( TextFieldTreeTableCell.forTreeTableColumn() );
        return c;
    }

    /**
     * @return the library DAO associated with the selected item
     */
    private LibraryDAO getSelectedItem() {
        return librariesTreeTable.getSelectionModel().getSelectedItem() != null
            ? librariesTreeTable.getSelectionModel().getSelectedItem().getValue()
            : null;
    }

    protected List<OtmLibrary> getSelectedLibraries() {
        List<OtmLibrary> libs = new ArrayList<>();
        ObservableList<TreeItem<LibraryDAO>> items = librariesTreeTable.getSelectionModel().getSelectedItems();
        items.forEach( i -> {
            if (i.getValue() != null)
                libs.add( i.getValue().getValue() );
        } );
        return libs;
    }

    /**
     * @return the currently selected library or null
     */
    public OtmLibrary getSelectedLibrary() {
        LibraryDAO item = getSelectedItem();
        return item != null ? item.getValue() : null;
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( "HandleEvent received: " + event.getClass().getSimpleName() );
        if (event instanceof DexModelChangeEvent)
            post( ((DexModelChangeEvent) event).getModelManager() );
    }

    /**
     * Listener for selected library members.
     *
     * @param item
     */
    private void librarySelectionListener(TreeItem<LibraryDAO> item) {
        if (ignore || item == null || item.getValue() == null || item.getValue().getValue() == null)
            return;
        // log.debug( "\n" );
        // log.debug( "Library selection listener. Ignore = " + ignore );

        OtmLibrary lib = null;
        if (item.getValue().getValue() instanceof OtmLibrary)
            lib = item.getValue().getValue();
        setEditing( lib != null && lib.isEditable() && lib.isUnmanaged() );
        // log.debug( "library selection listener, library = " + lib );

        ignore = true;
        DexLibrarySelectionEvent event = new DexLibrarySelectionEvent( lib );
        // fireEvent( event );
        Platform.runLater( () -> fireEvent( event ) );
        ignore = false;

        // lag problem - sometimes the tree does not move to the selection.
        // Requesting focus in the user and provider controllers then getting it back cures the problem.
        librariesTreeTable.requestFocus();
        // log.debug( "done - library selection listener " + librariesTreeTable.isFocused() );
    }

    private void setEditing(boolean editable) {
        nameColumn.setEditable( editable );
        namespaceColumn.setEditable( editable );
        prefixColumn.setEditable( editable );
        versionColumn.setEditable( editable );
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmModelManager modelMgr) {
        // log.debug( "Posting all libraries." );
        // ignoreEvents = true;
        if (modelMgr != null) {
            postedData = modelMgr;
            clear();

            for (OtmVersionChain chain : modelMgr.getChains()) {
                LibraryDAO.createNSItems( chain, root, editableOnlyFilter );
            }
        }
        librariesTreeTable.getSortOrder().add( nameColumn );

        // log.debug( "Posted library tree." );
    }

    @Override
    public void refresh() {
        post( postedData );
    }

    /**
     * Use the library tree table only to make selections, no editing. Set the mouse click handler AND make tree table
     * not editable to allow double click.
     * <p>
     * <b>Note:</b> double click should be on the row, not on tree's selection model. For a discussion of why, see
     * https://stackoverflow.com/questions/26563390/detect-doubleclick-on-row-of-tableview-javafx . <blockquote>Just
     * attach the listener directly to the table row when it's created. – James_D </blockquote>
     * 
     * @param handler set the handler and if null, disable editing
     */
    public void selectionMode(EventHandler<? super MouseEvent> handler) {
        librariesTreeTable.setOnMouseClicked( handler );
        librariesTreeTable.setEditable( handler != null );
    }
}
