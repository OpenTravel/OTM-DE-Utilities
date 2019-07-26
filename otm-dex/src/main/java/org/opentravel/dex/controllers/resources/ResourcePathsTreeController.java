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

package org.opentravel.dex.controllers.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage the resources navigation tree.
 * 
 * @author dmh
 *
 */
public class ResourcePathsTreeController extends DexIncludedControllerBase<OtmResource> implements DexController {
    private static Log log = LogFactory.getLog( ResourcePathsTreeController.class );

    // Column labels
    // TODO - externalize strings
    public static final String METHODCOLUMNLABEL = "Prefix";
    private static final String NAMECOLUMNLABEL = "Member";
    private static final String URLCOLUMNLABEL = "Version";
    // private static final String LIBRARYLABEL = "Library";
    // // private static final String ERRORLABEL = "Errors";
    // private static final String WHEREUSEDLABEL = "Types Used";

    /*
     * FXML injected
     */
    @FXML
    private VBox resourcePathsTreeView;
    @FXML
    private TreeTableView<ResourcePathsDAO> resourcePathsTree;
    @FXML
    private TitledPane pathsTitlePane;


    //
    TreeItem<ResourcePathsDAO> root; // Root of the navigation tree. Is displayed.

    // OtmModelManager currentModelMgr; // this is postedData

    // MemberFilterController filter = null;

    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    // private boolean treeEditingEnabled = true;

    private OtmResource currentResource;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourcePathsTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create columns
     */
    private void buildColumns() {

        TreeTableColumn<ResourcePathsDAO,String> nameColumn = new TreeTableColumn<>( NAMECOLUMNLABEL );
        nameColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "name" ) );
        setColumnProps( nameColumn, true, true, true, 50 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<ResourcePathsDAO,String> prefixColumn = new TreeTableColumn<>( METHODCOLUMNLABEL );
        prefixColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "method" ) );
        setColumnProps( prefixColumn, true, false, true, 50 );
        prefixColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

        TreeTableColumn<ResourcePathsDAO,String> versionColumn = new TreeTableColumn<>( URLCOLUMNLABEL );
        versionColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "url" ) );
        setColumnProps( versionColumn, true, true, true, 300 );

        // TreeTableColumn<ResourcePathsDAO,String> libColumn = new TreeTableColumn<>( LIBRARYLABEL );
        // libColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "library" ) );
        //
        // TreeTableColumn<ResourcePathsDAO,String> usedTypesCol = new TreeTableColumn<>( WHEREUSEDLABEL );
        // usedTypesCol.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "usedTypes" ) );
        //
        // TreeTableColumn<ResourcePathsDAO,ImageView> valColumn = new TreeTableColumn<>( "" );
        // valColumn.setCellFactory( c -> new ValidationMemberTreeTableCellFactory() );
        // setColumnProps( valColumn, true, false, false, 25 );

        // Add columns to table
        resourcePathsTree.getColumns().addAll( nameColumn, versionColumn, prefixColumn );
        resourcePathsTree.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourcePathsTreeView == null)
            throw new IllegalStateException( "Resource paths tree view is null." );
        if (resourcePathsTree == null)
            throw new IllegalStateException( "Resource paths tree is null." );
    }

    @Override
    public void clear() {
        resourcePathsTree.getRoot().getChildren().clear();
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = resourcePathsTreeView;
        // configure( parent.getModelManager(), treeEditingEnabled );
        // }
        //
        // /**
        // * Configure controller for use by non-main controllers.
        // *
        // * @param modelMgr must not be null
        // * @param editable sets tree editing enables
        // */
        // public void configure(OtmModelManager modelMgr, boolean editable) {
        // if (modelMgr == null)
        // throw new IllegalArgumentException(
        // "Model manager is null. Must configure member tree with model manager." );
        //
        // this.currentModelMgr = parent.getModelManager();
        // this.treeEditingEnabled = editable;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        resourcePathsTree.setRoot( getRoot() );
        resourcePathsTree.setShowRoot( false );
        resourcePathsTree.setEditable( false );
        // resourcesTree.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        // resourcesTree.setTableMenuButtonVisible( true ); // allow users to select columns

        // Enable context menus at the row level and add change listener for for applying style
        // resourcePathsTree.setRowFactory( (TreeTableView<ResourcePathsDAO> p) -> new MemberRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        // resourcePathsTree.getSelectionModel().select( 0 );
        // resourcePathsTree.setOnKeyReleased( this::keyReleased );
        // // resourcesTree.setOnMouseClicked(this::mouseClick);
        // resourcePathsTree.getSelectionModel().selectedItemProperty()
        // .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        refresh();
    }

    // /**
    // * Note: TreeItem class does not extend the Node class. Therefore, you cannot apply any visual effects or add
    // menus
    // * to the tree items. Use the cell factory mechanism to overcome this obstacle and define as much custom behavior
    // * for the tree items as your application requires.
    // *
    // * @param member the Otm Library Member to add to the tree
    // * @param parent the tree root or parent member
    // * @return
    // */
    // public void createTreeItem(OtmResource member, TreeItem<ResourcePathsDAO> parent) {
    // // log.debug("Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName());
    //
    // // // Apply Filter
    // // if (filter != null && !filter.isSelected( member ))
    // // return;
    // // Skip over contextual facets that have been injected into an object. Their contributed facets will be modeled.
    // // if ((member instanceof OtmContextualFacet && ((OtmContextualFacet) member).getWhereContributed() != null))
    // // return;
    //
    // // Create item for the library member
    // TreeItem<ResourcePathsDAO> item = new ResourcePathsDAO( member ).createTreeItem( parent );
    //
    // // Create and add items for children
    // if (member instanceof OtmChildrenOwner)
    // createChildrenItems( member, item );
    // }

    // /**
    // * Create tree items for the type provider children of this child owning member
    // */
    // private void createChildrenItems(OtmChildrenOwner member, TreeItem<ResourcePathsDAO> parentItem) {
    // member.getChildrenTypeProviders().forEach( p -> {
    // TreeItem<ResourcePathsDAO> cfItem = new ResourcePathsDAO( p ).createTreeItem( parentItem );
    //
    // // Only use contextual facet for recursing
    // if (p instanceof OtmContributedFacet && ((OtmContributedFacet) p).getContributor() != null)
    // p = ((OtmContributedFacet) p).getContributor();
    //
    // // Recurse
    // if (p instanceof OtmChildrenOwner)
    // createChildrenItems( (OtmChildrenOwner) p, cfItem );
    // } );
    // }

    // public MemberFilterController getFilter() {
    // return filter;
    // }

    public TreeItem<ResourcePathsDAO> getRoot() {
        return root;
    }

    // public ResourcePathsDAO getSelected() {
    // return resourcePathsTree.getSelectionModel().getSelectedItem() != null
    // ? resourcePathsTree.getSelectionModel().getSelectedItem().getValue() : null;
    // }

    // private void handleEvent(DexFilterChangeEvent event) {
    // if (!ignoreEvents)
    // refresh();
    // }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents)
            if (event.getMember() instanceof OtmResource)
                post( (OtmResource) event.getMember() );
    }

    @Override
    public void handleEvent(Event event) {
        // log.debug(event.getEventType() + " event received. Ignore? " + ignoreEvents);
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            // if (event instanceof DexFilterChangeEvent)
            // handleEvent( (DexFilterChangeEvent) event );
            if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }

    public void keyReleased(KeyEvent event) {
        TreeItem<ResourcePathsDAO> item = resourcePathsTree.getSelectionModel().getSelectedItem();
        int row = resourcePathsTree.getSelectionModel().getSelectedIndex();
        // log.debug("Selection row = " + row);
        // if (event.getCode() == KeyCode.RIGHT) {
        // event.consume();
        // item.setExpanded( true );
        // resourcesTree.getSelectionModel().clearAndSelect( row + 1, nameColumn );
        // } else if (event.getCode() == KeyCode.LEFT) {
        // TreeItem<ResourcePathsDAO> parent = item.getParent();
        // if (parent != null && parent != item && parent != root) {
        // resourcesTree.getSelectionModel().select( parent );
        // parent.setExpanded( false );
        // row = resourcesTree.getSelectionModel().getSelectedIndex();
        // resourcesTree.getSelectionModel().clearAndSelect( row, nameColumn );
        // event.consume();
        // }
        // }
    }

    /**
     * Listener for selected library members in the tree table.
     * 
     * @param item
     */
    private void memberSelectionListener(TreeItem<ResourcePathsDAO> item) {
        if (item == null)
            return;
        // log.debug("Selection Listener: " + item.getValue());
        assert item != null;
        // boolean editable = false;
        // if (treeEditingEnabled && item.getValue() != null)
        // editable = item.getValue().isEditable();
        // nameColumn.setEditable( editable ); // TODO - is this still useful?
        ignoreEvents = true;

        // FIXME
        // if (eventPublisherNode != null)
        // eventPublisherNode.fireEvent( new DexMemberSelectionEvent( this, item ) );
        ignoreEvents = false;
    }

    public void mouseClick(MouseEvent event) {
        // this fires after the member selection listener
        if (event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2)
            log.debug( "Double click selection: " );
        // + resourcesTree.getSelectionModel().getSelectedItem().getValue().nameProperty().toString());
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmResource resource) {
        ignoreEvents = true;
        if (resource != null && resourcePathsTree != null) {
            resourcePathsTree.getSelectionModel().clearSelection();
            resourcePathsTree.getRoot().getChildren().clear();

            resource.getActionRequests().forEach( a -> {
                new ResourcePathsDAO( a ).createTreeItem( getRoot() );
            } );

            // create cells for members
            // currentModelMgr.getMembers().forEach( m -> createTreeItem( m, root ) );
            // try {
            // resourcesTree.sort();
            // } catch (Exception e) {
            // // why does first sort always throw exception?
            // log.warn( "Exception sorting: " + e.getLocalizedMessage() );
            // }
        }
        ignoreEvents = false;
    }

    @Override
    public void refresh() {
        post( currentResource );
    }

    public void select(OtmLibraryMember otm) {
        // if (otm != null) {
        // for (TreeItem<ResourcePathsDAO> item : resourcePathsTree.getRoot().getChildren()) {
        // if (item.getValue().getValue() == otm) {
        // int row = resourcePathsTree.getRow( item );
        // // This may not highlight the row if the event comes from or goes to a different controller.
        // Platform.runLater( () -> {
        // // ignoreEvents = true;
        // resourcePathsTree.requestFocus();
        // resourcePathsTree.getSelectionModel().clearAndSelect( row );
        // resourcePathsTree.scrollTo( row );
        // resourcePathsTree.getFocusModel().focus( row );
        // // ignoreEvents = false;
        // } );
        // // log.debug("Selected " + otm.getName() + " in member tree.");
        // return;
        // }
        // }
        // log.warn( otm.getName() + " not found in member tree." );
        // }
    }

    // public void setFilter(MemberFilterController filter) {
    // this.filter = filter;
    // }

    // public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
    // resourcePathsTree.setOnMouseClicked( handler );
    // }
}
