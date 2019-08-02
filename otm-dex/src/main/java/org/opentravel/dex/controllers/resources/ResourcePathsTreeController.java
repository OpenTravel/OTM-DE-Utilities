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
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.model.OtmResourceChild;
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
    public static final String METHODCOLUMNLABEL = "Method";
    private static final String NAMECOLUMNLABEL = "Name";
    private static final String URLCOLUMNLABEL = "URL";
    private static final String CONTENTCOLUMNLABEL = "Content";

    /*
     * FXML injected
     */
    @FXML
    private VBox resourcePathsTreeView;
    @FXML
    private TreeTableView<ResourcePathsDAO> resourcePathsTree;
    @FXML
    private TitledPane pathsTitlePane;


    private TreeItem<ResourcePathsDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;
    private OtmResource currentResource;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED,
        DexModelChangeEvent.MODEL_CHANGED, DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED};
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
        setColumnProps( nameColumn, true, true, true, 100 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<ResourcePathsDAO,String> methodColumn = new TreeTableColumn<>( METHODCOLUMNLABEL );
        methodColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "method" ) );
        setColumnProps( methodColumn, true, false, true, 75 );
        methodColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

        TreeTableColumn<ResourcePathsDAO,String> urlColumn = new TreeTableColumn<>( URLCOLUMNLABEL );
        urlColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "url" ) );
        setColumnProps( urlColumn, true, true, true, 600 );

        TreeTableColumn<ResourcePathsDAO,String> contentColumn = new TreeTableColumn<>( CONTENTCOLUMNLABEL );
        contentColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ResourcePathsDAO,String>( "payload" ) );
        setColumnProps( contentColumn, true, true, true, 100 );

        // Add columns to table
        resourcePathsTree.getColumns().addAll( nameColumn, methodColumn, urlColumn, contentColumn );
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
        eventPublisherNode = resourcePathsTreeView;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        resourcePathsTree.setRoot( getRoot() );
        resourcePathsTree.setShowRoot( false );
        resourcePathsTree.setEditable( false );
        buildColumns();

        // Add listeners and event handlers

        refresh();
    }


    public TreeItem<ResourcePathsDAO> getRoot() {
        return root;
    }

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

        }
        ignoreEvents = false;
    }

    @Override
    public void refresh() {
        post( currentResource );
    }

}
