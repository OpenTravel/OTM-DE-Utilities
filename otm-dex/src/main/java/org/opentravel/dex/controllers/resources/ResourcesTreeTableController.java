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
import org.opentravel.common.cellfactories.ValidationResourceTreeTableCellFactory;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;

import java.util.List;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage the resources navigation tree.
 * 
 * @author dmh
 *
 */
public class ResourcesTreeTableController extends DexIncludedControllerBase<OtmModelManager> implements DexController {
    private static Log log = LogFactory.getLog( ResourcesTreeTableController.class );

    // Column labels
    public static final String PREFIXCOLUMNLABEL = "Prefix";
    private static final String NAMECOLUMNLABEL = "Member";
    private static final String VERSIONCOLUMNLABEL = "Version";
    private static final String LIBRARYLABEL = "Library";
    private static final String WHEREUSEDLABEL = "Types Used";

    /*
     * FXML injected
     */
    @FXML
    private VBox resourcesTreeTableView;
    @FXML
    private TreeTableView<ResourcesDAO> resourcesTreeTable;

    //
    TreeItem<ResourcesDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<ResourcesDAO,String> nameColumn; // an editable column

    OtmModelManager currentModelMgr; // this is postedData


    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    // private boolean treeEditingEnabled = true;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED,
        DexResourceChangeEvent.RESOURCE_CHANGED, DexResourceChildModifiedEvent.RESOURCE_CHILD_MODIFIED,
        DexModelChangeEvent.MODEL_CHANGED, DexMemberDeleteEvent.MEMBER_DELETED};

    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED,
        DexMemberSelectionEvent.RESOURCE_SELECTED, DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourcesTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create columns
     */
    private void buildColumns() {

        TreeTableColumn<ResourcesDAO,String> prefixColumn = new TreeTableColumn<>( PREFIXCOLUMNLABEL );
        prefixColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcesDAO,String>( "prefix" ) );
        setColumnProps( prefixColumn, true, false, true, 100 );
        prefixColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

        nameColumn = new TreeTableColumn<>( NAMECOLUMNLABEL );
        nameColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcesDAO,String>( "name" ) );
        setColumnProps( nameColumn, true, true, true, 200 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<ResourcesDAO,String> versionColumn = new TreeTableColumn<>( VERSIONCOLUMNLABEL );
        versionColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcesDAO,String>( "version" ) );

        TreeTableColumn<ResourcesDAO,String> libColumn = new TreeTableColumn<>( LIBRARYLABEL );
        libColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcesDAO,String>( "library" ) );

        TreeTableColumn<ResourcesDAO,String> usedTypesCol = new TreeTableColumn<>( WHEREUSEDLABEL );
        usedTypesCol.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcesDAO,String>( "usedTypes" ) );

        TreeTableColumn<ResourcesDAO,ImageView> valColumn = new TreeTableColumn<>( "" );
        valColumn.setCellFactory( c -> new ValidationResourceTreeTableCellFactory() );
        setColumnProps( valColumn, true, false, false, 25 );

        // Add columns to table
        resourcesTreeTable.getColumns().addAll( nameColumn, valColumn, libColumn, versionColumn, prefixColumn,
            usedTypesCol );
        resourcesTreeTable.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourcesTreeTable == null)
            throw new IllegalStateException( "Resource tree table is null." );
    }

    @Override
    public void clear() {
        resourcesTreeTable.getRoot().getChildren().clear();
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = resourcesTreeTableView;
        this.currentModelMgr = parent.getModelManager();

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        resourcesTreeTable.setRoot( getRoot() );
        resourcesTreeTable.setShowRoot( false );
        resourcesTreeTable.setEditable( false );
        buildColumns();

        // Add listeners and event handlers
        resourcesTreeTable.getSelectionModel().select( 0 );
        resourcesTreeTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        // Enable context menus at the row level and add change listener for for applying style
        resourcesTreeTable.setRowFactory( (TreeTableView<ResourcesDAO> p) -> new ResourcesTreeTableRowFactory( this ) );

        refresh();
    }

    public void createTreeItem(OtmResource resource, TreeItem<ResourcesDAO> parent) {
        // The resource
        TreeItem<ResourcesDAO> rItem = new ResourcesDAO( resource ).createTreeItem( parent );

        // Parent Refs
        resource.getParentRefs().forEach( pr -> new ResourcesDAO( pr ).createTreeItem( rItem ) );

        // Parameter Groups
        resource.getParameterGroups().forEach( pg -> {
            TreeItem<ResourcesDAO> child = new ResourcesDAO( pg ).createTreeItem( rItem );
            createChildrenItems( pg, child );
        } );

        // Action facets
        resource.getActionFacets().forEach( af -> new ResourcesDAO( af ).createTreeItem( rItem ) );

        // Actions
        resource.getActions().forEach( a -> {
            TreeItem<ResourcesDAO> child = new ResourcesDAO( a ).createTreeItem( rItem );
            createChildrenItems( a, child );
        } );
    }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner member, TreeItem<ResourcesDAO> parentItem) {
        // member.getChildrenTypeProviders().forEach( p -> {
        member.getChildren().forEach( p -> {
            TreeItem<ResourcesDAO> cfItem = new ResourcesDAO( p ).createTreeItem( parentItem );

            // Only use contextual facet for recursing
            if (p instanceof OtmContributedFacet && ((OtmContributedFacet) p).getContributor() != null)
                p = ((OtmContributedFacet) p).getContributor();

            // Inherited responses
            if (p instanceof OtmAction)
                for (OtmObject inherited : ((OtmAction) p).getInheritedChildren())
                    new ResourcesDAO( inherited ).createTreeItem( cfItem, p );

            // Recurse
            if (p instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) p, cfItem );
        } );
    }

    public TreeItem<ResourcesDAO> getRoot() {
        return root;
    }

    @Override
    public ResourcesDAO getSelection() {
        return resourcesTreeTable.getSelectionModel().getSelectedItem() != null
            ? resourcesTreeTable.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexFilterChangeEvent event) {
        if (!ignoreEvents)
            refresh();
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents)
            select( event.getMember() );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            if (event instanceof DexFilterChangeEvent)
                handleEvent( (DexFilterChangeEvent) event );
            if (event instanceof DexModelChangeEvent)
                post( ((DexModelChangeEvent) event).getModelManager() );
            else
                refresh();
        }
    }


    /**
     * Listener for selected library members in the tree table.
     * 
     * @param item
     */
    private void memberSelectionListener(TreeItem<ResourcesDAO> item) {
        if (item == null || item.getValue() == null || !(item.getValue().getValue() instanceof OtmObject))
            return;
        OtmObject object = item.getValue().getValue();
        if (mainController != null)
            if (object.isValid( true ))
                mainController.postStatus( object.getName() + " is valid." );
            else
                mainController.postStatus( object.getName() + " is not valid." );
        // log.debug("Selection Listener: " + item.getValue());

        // Fire event for selecting resources
        if (eventPublisherNode != null) {
            DexEvent event = null;
            if (object instanceof OtmResource)
                event = new DexMemberSelectionEvent( (OtmResource) object );
            else if (object instanceof OtmLibraryMember)
                event = new DexMemberSelectionEvent( (OtmLibraryMember) object );
            else if (object instanceof OtmResourceChild)
                event = new DexResourceChildSelectionEvent( (OtmResourceChild) object );

            ignoreEvents = true;
            if (event != null)
                eventPublisherNode.fireEvent( event );
            ignoreEvents = false;
        }
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmModelManager modelMgr) {
        ignoreEvents = true;
        if (modelMgr != null)
            currentModelMgr = modelMgr;
        if (resourcesTreeTable != null) {
            resourcesTreeTable.getSelectionModel().clearSelection();
            resourcesTreeTable.getRoot().getChildren().clear();
            // create cells for sorted list of resources
            List<OtmResource> resources = currentModelMgr.getResources( true );
            resources.forEach( r -> createTreeItem( r, root ) );

            resourcesTreeTable.refresh();
            // log.debug( "Posted " + resources.size() + " resources." );
        }
        ignoreEvents = false;
    }

    @Override
    public void refresh() {
        // Validate all resources
        currentModelMgr.getResources( false ).forEach( r -> r.isValid( true ) );
        post( currentModelMgr );
    }

    public void select(OtmLibraryMember otm) {
        if (otm != null) {
            for (TreeItem<ResourcesDAO> item : resourcesTreeTable.getRoot().getChildren()) {
                if (item.getValue().getValue() == otm) {
                    int row = resourcesTreeTable.getRow( item );
                    // This may not highlight the row if the event comes from or goes to a different controller.
                    Platform.runLater( () -> {
                        // ignoreEvents = true;
                        resourcesTreeTable.requestFocus();
                        resourcesTreeTable.getSelectionModel().clearAndSelect( row );
                        resourcesTreeTable.scrollTo( row );
                        resourcesTreeTable.getFocusModel().focus( row );
                        // ignoreEvents = false;
                    } );
                    // log.debug("Selected " + otm.getName() + " in member tree.");
                    return;
                }
            }
            log.warn( otm.getName() + " not found in member tree." );
        }
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
        resourcesTreeTable.setOnMouseClicked( handler );
    }
}
