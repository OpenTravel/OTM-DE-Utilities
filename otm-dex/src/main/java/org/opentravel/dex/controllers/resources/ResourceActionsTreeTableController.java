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
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.dex.events.DexResourceModifiedEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameter;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

/**
 * Manage the library member navigation tree.
 * 
 * @author dmh
 *
 */
public class ResourceActionsTreeTableController extends DexIncludedControllerBase<OtmResource>
    implements DexController {
    private static Log log = LogFactory.getLog( ResourceActionsTreeTableController.class );

    // Column labels
    private static final String NAMECOLUMNLABEL = "";
    private static final String AFLABEL = "";
    // private static final String CONTENTlABEL = "";

    /*
     * FXML injected
     */
    @FXML
    TreeTableView<ActionsDAO> resourceActionsTreeTable;
    @FXML
    private TitledPane resourceActionsTreeTablePane;

    //
    TreeItem<ActionsDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<ActionsDAO,String> nameColumn; // an editable column

    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    private boolean treeEditingEnabled = false;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexResourceChildModifiedEvent.RESOURCE_CHILD_MODIFIED,
        DexResourceModifiedEvent.RESOURCE_MODIFIED, DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED,
        DexResourceChangeEvent.RESOURCE_CHANGED, DexMemberSelectionEvent.RESOURCE_SELECTED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourceActionsTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create columns
     */
    private void buildColumns() {

        nameColumn = new TreeTableColumn<>( NAMECOLUMNLABEL );
        nameColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ActionsDAO,String>( "name" ) );
        setColumnProps( nameColumn, true, true, true, 200 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<ActionsDAO,String> afColumn = new TreeTableColumn<>( AFLABEL );
        afColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ActionsDAO,String>( "dataColumn" ) );
        setColumnProps( afColumn, true, true, true, 500 );

        // TreeTableColumn<ActionsDAO,String> contentColumn = new TreeTableColumn<>( CONTENTlABEL );
        // contentColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ActionsDAO,String>( "content" ) );
        // setColumnProps( contentColumn, true, true, true, 300 );

        // TreeTableColumn<ActionsDAO,ImageView> valColumn = new TreeTableColumn<>( "" );
        // valColumn.setCellFactory( c -> new ValidationActionTreeTableCellFactory() );
        // setColumnProps( valColumn, true, false, false, 25 );

        // Add columns to table
        resourceActionsTreeTable.getColumns().addAll( nameColumn, afColumn );
        // resourceActionsTreeTable.getColumns().addAll( nameColumn, afColumn, contentColumn, valColumn );
        resourceActionsTreeTable.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourceActionsTreeTable == null)
            throw new IllegalStateException( "Resource RQ tree table is null." );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        resourceActionsTreeTable.getSelectionModel().clearSelection();
        resourceActionsTreeTable.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = resourceActionsTreeTablePane;
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
        resourceActionsTreeTable.setRoot( getRoot() );
        resourceActionsTreeTable.setShowRoot( false );
        resourceActionsTreeTable.setEditable( true );
        resourceActionsTreeTable.getSelectionModel().setCellSelectionEnabled( true );

        // Force the tree to take up the rest of the horizontal space
        resourceActionsTreeTablePane.setPrefHeight( 2000 );
        resourceActionsTreeTable.minHeight( 2000 );
        resourceActionsTreeTable.prefHeightProperty().bind( resourceActionsTreeTablePane.heightProperty() );
        // resourceActionsTreeTablePane.prefHeightProperty().bind( resourceActionsTreeTable.heightProperty() );

        resourceActionsTreeTable.setTableMenuButtonVisible( true ); // allow users to select columns
        // Enable context menus at the row level and add change listener for for applying style
        resourceActionsTreeTable.setRowFactory( (TreeTableView<ActionsDAO> p) -> new ActionRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        resourceActionsTreeTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        refresh();
    }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner childrenOwner, TreeItem<ActionsDAO> parentItem) {
        if (childrenOwner.getChildren() != null)
            for (OtmObject child : childrenOwner.getChildren()) {
                TreeItem<ActionsDAO> cfItem = new ActionsDAO( child ).createTreeItem( parentItem );

                // Only use contextual facet for recursing
                if (child instanceof OtmContributedFacet && ((OtmContributedFacet) child).getContributor() != null)
                    child = ((OtmContributedFacet) child).getContributor();

                // Recurse
                if (child instanceof OtmChildrenOwner)
                    createChildrenItems( (OtmChildrenOwner) child, cfItem );
            }
    }

    public TreeItem<ActionsDAO> getRoot() {
        return root;
    }

    public ActionsDAO getSelected() {
        return resourceActionsTreeTable.getSelectionModel().getSelectedItem() != null
            ? resourceActionsTreeTable.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents && event.getMember() instanceof OtmResource)
            post( (OtmResource) event.getMember() );
    }

    private void handleEvent(DexResourceChangeEvent event) {
        if (!ignoreEvents && event.getResource() instanceof OtmResource)
            post( (OtmResource) event.getResource() );
    }

    private void handleEvent(DexResourceChildSelectionEvent event) {
        if (!ignoreEvents && event.get() instanceof OtmResourceChild)
            post( event.get().getOwningMember() );
    }

    private void handleEvent(DexResourceChildModifiedEvent event) {
        if (!ignoreEvents && event.get() instanceof OtmResourceChild && event.get().getOwningMember() == postedData)
            refresh();
    }

    private void handleEvent(DexResourceModifiedEvent event) {
        if (!ignoreEvents && event.get() instanceof OtmResource)
            refresh();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug(event.getEventType() + " event received. Ignore? " + ignoreEvents);
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            else if (event instanceof DexFilterChangeEvent)
                handleEvent( (DexFilterChangeEvent) event );
            else if (event instanceof DexResourceChildSelectionEvent)
                handleEvent( (DexResourceChildSelectionEvent) event );
            else if (event instanceof DexResourceChangeEvent)
                handleEvent( (DexResourceChangeEvent) event );
            else if (event instanceof DexResourceModifiedEvent)
                handleEvent( (DexResourceModifiedEvent) event );
            else if (event instanceof DexResourceChildModifiedEvent)
                handleEvent( (DexResourceChildModifiedEvent) event );
            else if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }

    /**
     * Listener for selected library members in the tree table.
     *
     * @param item
     */
    private void memberSelectionListener(TreeItem<ActionsDAO> item) {
        if (item == null)
            return;
        if (item.getValue() != null && item.getValue().getValue() != null) {
            // log.debug( "Selection Listener: " + item.getValue() );
            OtmObject obj = item.getValue().getValue();
            OtmLibraryMember member = null;
            if (obj instanceof OtmContributedFacet)
                obj = ((OtmContributedFacet) obj).getContributor();
            if (obj instanceof OtmLibraryMember)
                member = (OtmLibraryMember) obj;
            else if (obj instanceof OtmParameter && ((OtmParameter) obj).getFieldAssignedType() != null)
                member = ((OtmParameter) obj).getFieldAssignedType().getOwningMember();
            else if (obj instanceof OtmTypeUser && ((OtmTypeUser) obj).getAssignedType() != null)
                member = ((OtmTypeUser) obj).getAssignedType().getOwningMember();
            ignoreEvents = true;
            if (member != null)
                eventPublisherNode.fireEvent( new DexMemberSelectionEvent( member ) );
            ignoreEvents = false;
        }
    }

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

        if (resource != null) {
            // create items for each action
            resource.getActions().forEach( a -> createTreeItem( a, root ) );
        }
    }

    public void createTreeItem(OtmAction action, TreeItem<ActionsDAO> parent) {
        // Create item for the request
        TreeItem<ActionsDAO> actionItem = new ActionsDAO( action ).createTreeItem( parent );
        actionItem.setExpanded( true );

        // Create parent reference items (if any)
        action.getOwningMember().getAllParentRefs( true ).forEach( pr -> {
            if (pr.isParentFirstClass())
                new ActionsDAO( pr, action ).createTreeItem( actionItem );
        } );

        // Create request item
        if (action.getRequest() != null) {
            TreeItem<ActionsDAO> requestItem = new ActionsDAO( action.getRequest() ).createTreeItem( actionItem );
            // Show the parameters
            if (action.getRequest().getParamGroup() != null)
                action.getRequest().getParamGroup().getChildren()
                    .forEach( p -> new ActionsDAO( p ).createTreeItem( requestItem ) );
            createPayloadItems( action.getRequest().getPayload(), requestItem );
            // TODO - add inherited parameters ??
        }

        // Create response items
        for (OtmActionResponse response : action.getResponses()) {
            TreeItem<ActionsDAO> responseItem = new ActionsDAO( response ).createTreeItem( actionItem );
            // responseItem.setExpanded( true );
            createPayloadItems( response.getPayload(), responseItem );
        }

        // Test - inherited responses
        for (OtmObject inherited : action.getInheritedChildren())
            // TreeItem<ActionsDAO> responseItem =
            new ActionsDAO( inherited ).createTreeItem( actionItem );

    }

    // Model the pay load if any. Payload model is the Business Object or other objects.
    private void createPayloadItems(OtmObject payload, TreeItem<ActionsDAO> ownerItem) {
        // log.debug( "Posting payload of: " + payload );
        if (payload != null) {
            TreeItem<ActionsDAO> payloadItem = new ActionsDAO( payload ).createTreeItem( ownerItem );
            if (payload instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) payload, payloadItem );
        }

    }

    @Override
    public void refresh() {
        post( postedData );
    }

}
