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

package org.opentravel.dex.controllers.library.usage;

import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.dex.events.OtmObjectReplacedEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.List;
import java.util.Map;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * Manage Users of library pane in library window.
 * 
 * @author dmh
 *
 */
public class UsersTreeController extends DexIncludedControllerBase<OtmLibrary> implements DexController {
    // private static Log log = LogFactory.getLog( UsersTreeController.class );

    /*
     * FXML injected
     */
    @FXML
    private TreeView<LibraryAndMembersDAO> usersTree;
    @FXML
    private VBox usersTreeVBox;
    @FXML
    private Label columnLabel;

    private TreeItem<LibraryAndMembersDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {OtmObjectChangeEvent.OBJECT_CHANGED, OtmObjectModifiedEvent.OBJECT_MODIFIED,
            OtmObjectReplacedEvent.OBJECT_REPLACED, DexMemberDeleteEvent.MEMBER_DELETED,
            DexLibrarySelectionEvent.LIBRARY_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public UsersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (usersTree == null || columnLabel == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedData = null;
        usersTree.getRoot().getChildren().clear();
        columnLabel.setText( "" );
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        eventPublisherNode = usersTree;
        configure( parent.getModelManager() );
    }

    /**
     * Configure controller for use by non-main controllers.
     * 
     * @param modelMgr must not be null
     * @param editable sets tree editing enables
     */
    public void configure(OtmModelManager modelMgr) {
        if (modelMgr == null)
            throw new IllegalArgumentException(
                "Model manager is null. Must configure member tree with model manager." );

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        usersTree.setRoot( getRoot() );
        usersTree.setShowRoot( false );
        usersTree.setEditable( true );

        // Add listeners and event handlers
        usersTree.getSelectionModel().select( 0 );
        usersTree.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        // Enable context menus at the row level and add change listener for for applying style
        // FIXME usersTree.setCellFactory( (TreeView<LibraryAndMembersDAO> p) -> new UserCellFactory( this ) );
        refresh();
    }


    public TreeItem<LibraryAndMembersDAO> getRoot() {
        return root;
    }

    public LibraryAndMembersDAO getSelected() {
        return usersTree.getSelectionModel().getSelectedItem() != null
            ? usersTree.getSelectionModel().getSelectedItem().getValue() : null;
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (!ignoreEvents && event != null && event.getEventType() != null) {
            // log.debug( "Users tree received: " + event.getEventType() );
            if (event instanceof DexLibrarySelectionEvent)
                post( ((DexLibrarySelectionEvent) event).getLibrary() );
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
    private void memberSelectionListener(TreeItem<LibraryAndMembersDAO> item) {
        if (item == null || eventPublisherNode == null || ignoreEvents)
            return;

        // log.debug( "Selection Listener: " + item.getValue() );
        OtmLibraryMember member = null;
        if (item.getValue() != null && item.getValue().getValue() instanceof OtmLibraryMember)
            member = (OtmLibraryMember) item.getValue().getValue();

        ignoreEvents = true;
        if (member != null)
            fireEvent( new DexMemberSelectionEvent( member, DexMemberSelectionEvent.MEMBER_SELECTED ) );
        ignoreEvents = false;
    }

    @Override
    public void post(OtmLibrary library) {
        if (library == null || library == postedData)
            return;
        super.post( library );
        // log.debug( "Posting library users." );

        if (columnLabel != null)
            columnLabel.setText( "Users of " + library.getPrefix() + " : " + library.getName() );

        Map<OtmLibrary,List<OtmLibraryMember>> map = library.getUsersMap();
        LibraryAndMembersDAO.createChildrenItemsNoLib( map, getRoot() );

        usersTree.requestFocus();
        // log.debug( "Posted library users." );
    }

    @Override
    public void refresh() {
        OtmLibrary library = postedData;
        postedData = null;
        post( library );
        ignoreEvents = false;
    }
}
