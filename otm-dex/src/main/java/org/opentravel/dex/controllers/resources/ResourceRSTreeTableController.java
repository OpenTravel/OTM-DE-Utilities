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
import org.opentravel.common.cellfactories.ValidationMemberTreeTableCellFactory;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.controllers.member.MemberRowFactory;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionResponse;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Manage the Resource Response tree.
 * 
 * @author dmh
 *
 */
@Deprecated
public class ResourceRSTreeTableController extends DexIncludedControllerBase<OtmResource> implements DexController {
    private static Log log = LogFactory.getLog( ResourceRSTreeTableController.class );

    // Column labels
    public static final String PREFIXCOLUMNLABEL = "Prefix";
    private static final String NAMECOLUMNLABEL = "Member";
    private static final String VERSIONCOLUMNLABEL = "Version";
    private static final String LIBRARYLABEL = "Library";
    // private static final String ERRORLABEL = "Errors";
    private static final String WHEREUSEDLABEL = "Types Used";

    /*
     * FXML injected
     */
    @FXML
    TreeTableView<MemberAndProvidersDAO> resourceRSTreeTable;
    @FXML
    private VBox resourceRSTreeTableView;
    @FXML
    private TitledPane rsTitledPane;

    //
    TreeItem<MemberAndProvidersDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<MemberAndProvidersDAO,String> nameColumn; // an editable column

    OtmModelManager currentModelMgr; // this is postedData

    MemberFilterController filter = null;

    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    private boolean treeEditingEnabled = true;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED, DexFilterChangeEvent.FILTER_CHANGED,
            DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourceRSTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create columns
     */
    private void buildColumns() {

        TreeTableColumn<MemberAndProvidersDAO,String> prefixColumn = new TreeTableColumn<>( PREFIXCOLUMNLABEL );
        prefixColumn.setCellValueFactory( new TreeItemPropertyValueFactory<MemberAndProvidersDAO,String>( "prefix" ) );
        setColumnProps( prefixColumn, true, false, true, 100 );
        prefixColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

        nameColumn = new TreeTableColumn<>( NAMECOLUMNLABEL );
        nameColumn.setCellValueFactory( new TreeItemPropertyValueFactory<MemberAndProvidersDAO,String>( "name" ) );
        setColumnProps( nameColumn, true, true, true, 200 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<MemberAndProvidersDAO,String> versionColumn = new TreeTableColumn<>( VERSIONCOLUMNLABEL );
        versionColumn
            .setCellValueFactory( new TreeItemPropertyValueFactory<MemberAndProvidersDAO,String>( "version" ) );

        TreeTableColumn<MemberAndProvidersDAO,String> libColumn = new TreeTableColumn<>( LIBRARYLABEL );
        libColumn.setCellValueFactory( new TreeItemPropertyValueFactory<MemberAndProvidersDAO,String>( "library" ) );

        TreeTableColumn<MemberAndProvidersDAO,String> usedTypesCol = new TreeTableColumn<>( WHEREUSEDLABEL );
        usedTypesCol
            .setCellValueFactory( new TreeItemPropertyValueFactory<MemberAndProvidersDAO,String>( "usedTypes" ) );

        TreeTableColumn<MemberAndProvidersDAO,ImageView> valColumn = new TreeTableColumn<>( "" );
        valColumn.setCellFactory( c -> new ValidationMemberTreeTableCellFactory() );
        setColumnProps( valColumn, true, false, false, 25 );

        // Add columns to table
        resourceRSTreeTable.getColumns().addAll( nameColumn, valColumn, libColumn, versionColumn, prefixColumn,
            usedTypesCol );
        resourceRSTreeTable.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourceRSTreeTable == null)
            throw new IllegalStateException( "Resource RS tree table is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        resourceRSTreeTable.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = resourceRSTreeTableView;
        configure( parent.getModelManager(), treeEditingEnabled );
    }

    /**
     * Configure controller for use by non-main controllers.
     * 
     * @param modelMgr must not be null
     * @param editable sets tree editing enables
     */
    public void configure(OtmModelManager modelMgr, boolean editable) {
        if (modelMgr == null)
            throw new IllegalArgumentException(
                "Model manager is null. Must configure member tree with model manager." );

        this.currentModelMgr = modelMgr;
        this.treeEditingEnabled = editable;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        resourceRSTreeTable.setRoot( getRoot() );
        resourceRSTreeTable.setShowRoot( false );
        resourceRSTreeTable.setEditable( true );
        resourceRSTreeTable.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        resourceRSTreeTable.setTableMenuButtonVisible( true ); // allow users to select columns
        // Enable context menus at the row level and add change listener for for applying style
        resourceRSTreeTable.setRowFactory( (TreeTableView<MemberAndProvidersDAO> p) -> new MemberRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        // resourceRSTreeTable.getSelectionModel().select( 0 );
        // resourceRSTreeTable.setOnKeyReleased( this::keyReleased );
        // // memberTree.setOnMouseClicked(this::mouseClick);
        // resourceRSTreeTable.getSelectionModel().selectedItemProperty()
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
    // public void createTreeItem(OtmLibraryMember member, TreeItem<MemberAndProvidersDAO> parent) {
    // // log.debug("Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName());
    //
    // // Apply Filter
    // if (filter != null && !filter.isSelected( member ))
    // return;
    // // Skip over contextual facets that have been injected into an object. Their contributed facets will be modeled.
    // if ((member instanceof OtmContextualFacet && ((OtmContextualFacet) member).getWhereContributed() != null))
    // return;
    //
    // // Create item for the library member
    // TreeItem<MemberAndProvidersDAO> item = new MemberAndProvidersDAO( member ).createTreeItem( parent );
    //
    // // Create and add items for children
    // if (member instanceof OtmChildrenOwner)
    // createChildrenItems( member, item );
    // }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner childOwner, TreeItem<MemberAndProvidersDAO> parentItem) {
        childOwner.getChildren().forEach( child -> {
            TreeItem<MemberAndProvidersDAO> cfItem = new MemberAndProvidersDAO( child ).createTreeItem( parentItem );

            // Only use contextual facet for recursing
            if (child instanceof OtmContributedFacet && ((OtmContributedFacet) child).getContributor() != null)
                child = ((OtmContributedFacet) child).getContributor();

            // Recurse
            if (child instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) child, cfItem );
        } );
    }

    // public MemberFilterController getFilter() {
    // return filter;
    // }

    public TreeItem<MemberAndProvidersDAO> getRoot() {
        return root;
    }

    public MemberAndProvidersDAO getSelected() {
        return resourceRSTreeTable.getSelectionModel().getSelectedItem() != null
            ? resourceRSTreeTable.getSelectionModel().getSelectedItem().getValue() : null;
    }

    // private void handleEvent(DexFilterChangeEvent event) {
    // if (!ignoreEvents)
    // refresh();
    // }


    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents && event.getMember() instanceof OtmResource)
            post( (OtmResource) event.getMember() );
    }

    private void handleEvent(DexResourceChildSelectionEvent event) {
        if (!ignoreEvents && event.get() instanceof OtmResourceChild)
            post( event.get().getOwningMember() );
    }

    @Override
    public void handleEvent(Event event) {
        // log.debug(event.getEventType() + " event received. Ignore? " + ignoreEvents);
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            if (event instanceof DexResourceChildSelectionEvent)
                handleEvent( (DexResourceChildSelectionEvent) event );
            if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }


    // /**
    // * Listener for selected library members in the tree table.
    // *
    // * @param item
    // */
    // private void memberSelectionListener(TreeItem<MemberAndProvidersDAO> item) {
    // if (item == null)
    // return;
    // // log.debug("Selection Listener: " + item.getValue());
    // assert item != null;
    // boolean editable = false;
    // if (treeEditingEnabled && item.getValue() != null)
    // editable = item.getValue().isEditable();
    // nameColumn.setEditable( editable ); // TODO - is this still useful?
    // ignoreEvents = true;
    // if (eventPublisherNode != null)
    // eventPublisherNode.fireEvent( new DexMemberSelectionEvent( this, item ) );
    // ignoreEvents = false;
    // }

    @Override
    public void post(OtmResource resource) {
        postedData = resource;
        clear();

        if (resource != null)
            resource.getActions().forEach( a -> createTreeItem( a, root ) );

    }

    public void createTreeItem(OtmAction action, TreeItem<MemberAndProvidersDAO> parent) {
        // Create item for the request
        TreeItem<MemberAndProvidersDAO> actionItem = new MemberAndProvidersDAO( action ).createTreeItem( parent );

        // Create request item
        if (action.getRequest() != null) {
            TreeItem<MemberAndProvidersDAO> requestItem =
                new MemberAndProvidersDAO( action.getRequest() ).createTreeItem( actionItem );
            createPayloadItems( action.getRequest().getPayload(), requestItem );
        }

        // Create response items
        for (OtmActionResponse response : action.getResponses()) {
            TreeItem<MemberAndProvidersDAO> responseItem =
                new MemberAndProvidersDAO( response ).createTreeItem( actionItem );
            createPayloadItems( response.getPayload(), responseItem );
        }

    }

    // Model the pay load if any
    private void createPayloadItems(OtmObject payload, TreeItem<MemberAndProvidersDAO> ownerItem) {
        log.debug( "Posting payload of: " + payload );
        if (payload != null) {
            TreeItem<MemberAndProvidersDAO> payloadItem =
                new MemberAndProvidersDAO( payload ).createTreeItem( ownerItem );
            if (payload instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) payload, payloadItem );
        }

    }

    // /**
    // * Get the library members from the model manager and put them into a cleared tree.
    // *
    // * @param modelMgr
    // */
    // @Override
    // public void post(OtmModelManager modelMgr) {
    // ignoreEvents = true;
    // if (modelMgr != null && resourceRSTreeTable != null) {
    // currentModelMgr = modelMgr;
    // resourceRSTreeTable.getSelectionModel().clearSelection();
    // resourceRSTreeTable.getRoot().getChildren().clear();
    //
    // // create cells for members
    // currentModelMgr.getMembers().forEach( m -> createTreeItem( m, root ) );
    // try {
    // resourceRSTreeTable.sort();
    // } catch (Exception e) {
    // // why does first sort always throw exception?
    // log.warn( "Exception sorting: " + e.getLocalizedMessage() );
    // }
    // }
    // ignoreEvents = false;
    // }

    @Override
    public void refresh() {
        post( postedData );
    }

    // public void select(OtmLibraryMember otm) {
    // if (otm != null) {
    // for (TreeItem<MemberAndProvidersDAO> item : resourceRSTreeTable.getRoot().getChildren()) {
    // if (item.getValue().getValue() == otm) {
    // int row = resourceRSTreeTable.getRow( item );
    // // This may not highlight the row if the event comes from or goes to a different controller.
    // Platform.runLater( () -> {
    // // ignoreEvents = true;
    // resourceRSTreeTable.requestFocus();
    // resourceRSTreeTable.getSelectionModel().clearAndSelect( row );
    // resourceRSTreeTable.scrollTo( row );
    // resourceRSTreeTable.getFocusModel().focus( row );
    // // ignoreEvents = false;
    // } );
    // // log.debug("Selected " + otm.getName() + " in member tree.");
    // return;
    // }
    // }
    // log.warn( otm.getName() + " not found in member tree." );
    // }
    // }

    // public void setFilter(MemberFilterController filter) {
    // this.filter = filter;
    // }
    //
    // public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
    // resourceRSTreeTable.setOnMouseClicked( handler );
    // }
}
