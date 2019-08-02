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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/**
 * Manage the library member navigation tree.
 * 
 * @author dmh
 *
 */
public class ResourceRQTreeTableController extends DexIncludedControllerBase<OtmResource> implements DexController {
    private static Log log = LogFactory.getLog( ResourceRQTreeTableController.class );

    // Column labels
    // TODO - externalize strings
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
    TreeTableView<MemberAndProvidersDAO> resourceRQTreeTable;
    @FXML
    private VBox resourceRQTreeTableView;

    //
    TreeItem<MemberAndProvidersDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<MemberAndProvidersDAO,String> nameColumn; // an editable column

    // OtmModelManager currentModelMgr; // this is postedData

    MemberFilterController filter = null;

    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    private boolean treeEditingEnabled = true;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexFilterChangeEvent.FILTER_CHANGED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourceRQTreeTableController() {
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
        resourceRQTreeTable.getColumns().addAll( nameColumn, valColumn, libColumn, versionColumn, prefixColumn,
            usedTypesCol );
        resourceRQTreeTable.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourceRQTreeTable == null)
            throw new IllegalStateException( "Resource RQ tree table is null." );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        resourceRQTreeTable.getSelectionModel().clearSelection();
        resourceRQTreeTable.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = resourceRQTreeTableView;
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

        this.treeEditingEnabled = editable;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        resourceRQTreeTable.setRoot( getRoot() );
        resourceRQTreeTable.setShowRoot( false );
        resourceRQTreeTable.setEditable( true );
        resourceRQTreeTable.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        resourceRQTreeTable.setTableMenuButtonVisible( true ); // allow users to select columns
        // Enable context menus at the row level and add change listener for for applying style
        resourceRQTreeTable.setRowFactory( (TreeTableView<MemberAndProvidersDAO> p) -> new MemberRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        resourceRQTreeTable.getSelectionModel().select( 0 );
        resourceRQTreeTable.setOnKeyReleased( this::keyReleased );
        // memberTree.setOnMouseClicked(this::mouseClick);
        resourceRQTreeTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        refresh();
    }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner childrenOwner, TreeItem<MemberAndProvidersDAO> parentItem) {
        for (OtmObject child : childrenOwner.getChildren()) {
            TreeItem<MemberAndProvidersDAO> cfItem = new MemberAndProvidersDAO( child ).createTreeItem( parentItem );

            // Only use contextual facet for recursing
            if (child instanceof OtmContributedFacet && ((OtmContributedFacet) child).getContributor() != null)
                child = ((OtmContributedFacet) child).getContributor();

            // Recurse
            if (child instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) child, cfItem );
        }
    }

    public MemberFilterController getFilter() {
        return filter;
    }

    public TreeItem<MemberAndProvidersDAO> getRoot() {
        return root;
    }

    public MemberAndProvidersDAO getSelected() {
        return resourceRQTreeTable.getSelectionModel().getSelectedItem() != null
            ? resourceRQTreeTable.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexFilterChangeEvent event) {
        if (!ignoreEvents)
            refresh();
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents && event.getMember() instanceof OtmResource)
            post( (OtmResource) event.getMember() );
    }

    @Override
    public void handleEvent(Event event) {
        // log.debug(event.getEventType() + " event received. Ignore? " + ignoreEvents);
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            if (event instanceof DexFilterChangeEvent)
                handleEvent( (DexFilterChangeEvent) event );
            // if (event instanceof DexResourceChildSelectionEvent)
            // post( ((DexResourceChildSelectionEvent) event).get() );
            if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }

    public void keyReleased(KeyEvent event) {
        TreeItem<MemberAndProvidersDAO> item = resourceRQTreeTable.getSelectionModel().getSelectedItem();
        int row = resourceRQTreeTable.getSelectionModel().getSelectedIndex();
        // log.debug("Selection row = " + row);
        if (event.getCode() == KeyCode.RIGHT) {
            event.consume();
            item.setExpanded( true );
            resourceRQTreeTable.getSelectionModel().clearAndSelect( row + 1, nameColumn );
        } else if (event.getCode() == KeyCode.LEFT) {
            TreeItem<MemberAndProvidersDAO> parent = item.getParent();
            if (parent != null && parent != item && parent != root) {
                resourceRQTreeTable.getSelectionModel().select( parent );
                parent.setExpanded( false );
                row = resourceRQTreeTable.getSelectionModel().getSelectedIndex();
                resourceRQTreeTable.getSelectionModel().clearAndSelect( row, nameColumn );
                event.consume();
            }
        }
    }

    /**
     * Listener for selected library members in the tree table.
     * 
     * @param item
     */
    private void memberSelectionListener(TreeItem<MemberAndProvidersDAO> item) {
        if (item == null)
            return;
        log.debug( "Selection Listener: " + item.getValue() );
        // assert item != null;
        // boolean editable = false;
        // if (treeEditingEnabled && item.getValue() != null)
        // editable = item.getValue().isEditable();
        // nameColumn.setEditable( editable ); // TODO - is this still useful?
        ignoreEvents = true;
        // if (eventPublisherNode != null)
        // eventPublisherNode.fireEvent( new DexMemberSelectionEvent( this, item ) );
        ignoreEvents = false;
    }

    // public void mouseClick(MouseEvent event) {
    // // this fires after the member selection listener
    // if (event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2)
    // log.debug( "Double click selection: " );
    // // + memberTree.getSelectionModel().getSelectedItem().getValue().nameProperty().toString());
    // }

    /**
     * 
     * One request per action, multiple actions per resource
     * <p>
     * Request PayloadType is an ActionFacet
     * <p>
     * AF has either: BasePayload and Reference facet
     * <p>
     * Base payload properties are added to the content model of the AF
     * 
     * @see org.opentravel.dex.controllers.DexIncludedControllerBase#post(java.lang.Object)
     */
    @Override
    public void post(OtmResource resource) {
        postedData = resource;
        clear();

        if (resource != null)
            resource.getActionRequests().forEach( a -> createTreeItem( a, root ) );
    }

    public void createTreeItem(OtmActionRequest request, TreeItem<MemberAndProvidersDAO> parent) {
        // Create item for the request
        TreeItem<MemberAndProvidersDAO> rqItem = new MemberAndProvidersDAO( request ).createTreeItem( parent );

        // Model the pay load if any
        OtmActionFacet actionFacet = request.getPayloadType();
        if (actionFacet != null) {
            OtmObject payload = actionFacet.getRequestPayload();
            log.debug( "Posting payload of: " + payload );

            TreeItem<MemberAndProvidersDAO> payloadItem = new MemberAndProvidersDAO( payload ).createTreeItem( rqItem );

            if (payload instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) payload, payloadItem );
            // new MemberAndProvidersDAO( actionFacet.getRequestPayload() ).createTreeItem( item );
        }
    }

    @Override
    public void refresh() {
        post( postedData );
    }

    public void select(OtmLibraryMember otm) {
        // if (otm != null) {
        // for (TreeItem<MemberAndProvidersDAO> item : resourceRQTreeTable.getRoot().getChildren()) {
        // if (item.getValue().getValue() == otm) {
        // int row = resourceRQTreeTable.getRow( item );
        // // This may not highlight the row if the event comes from or goes to a different controller.
        // Platform.runLater( () -> {
        // // ignoreEvents = true;
        // resourceRQTreeTable.requestFocus();
        // resourceRQTreeTable.getSelectionModel().clearAndSelect( row );
        // resourceRQTreeTable.scrollTo( row );
        // resourceRQTreeTable.getFocusModel().focus( row );
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
    // resourceRQTreeTable.setOnMouseClicked( handler );
    // }
}
