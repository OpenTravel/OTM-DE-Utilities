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

package org.opentravel.dex.controllers.member.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.properties.PropertiesDAO;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * Manage the library member and its type users in the where used view. (center column)
 * 
 * @author dmh
 *
 */
public class TypeUsersTreeController extends DexIncludedControllerBase<OtmLibraryMember> implements DexController {
    private static Log log = LogFactory.getLog( TypeUsersTreeController.class );

    /*
     * FXML injected
     */
    @FXML
    TreeView<PropertiesDAO> typeUsersTree;
    @FXML
    Label columnLabel;
    @FXML
    private VBox typeUsersVBox;

    TreeItem<PropertiesDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public TypeUsersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (typeUsersVBox == null)
            throw new IllegalStateException( "Type Users' member where used is null." );
        if (typeUsersTree == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedData = null;
        typeUsersTree.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Type Users Tree.");
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

        eventPublisherNode = typeUsersVBox;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        typeUsersTree.setRoot( getRoot() );
        typeUsersTree.setShowRoot( false );
        typeUsersTree.setEditable( true );

        // Add listeners and event handlers
        typeUsersTree.getSelectionModel().select( 0 );
        // whereUsedTreeTable.setOnKeyReleased(this::keyReleased);
        // whereUsedTreeTable.setOnMouseClicked(this::mouseClick);
        typeUsersTree.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        // log.debug("Where used table configured.");
        refresh();
    }

    public TreeItem<PropertiesDAO> getRoot() {
        return root;
    }

    public PropertiesDAO getSelected() {
        return typeUsersTree.getSelectionModel().getSelectedItem() != null
            ? typeUsersTree.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents)
            try {
                post( event.getMember() );
            } catch (Exception e) {
                // No-op
            }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug(event.getEventType() + " event received. Ignore? " + ignoreEvents);
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }

    // public void keyReleased(KeyEvent event) {
    // // TreeItem<PropertiesDAO> item = whereUsedTreeTable.getSelectionModel().getSelectedItem();
    // // ObservableList<TreeTablePosition<PropertiesDAO, ?>> cells =
    // // whereUsedTreeTable.getSelectionModel().getSelectedCells();
    // int row = typeUsersTree.getSelectionModel().getSelectedIndex();
    // log.debug("Selection row = " + row);
    // if (event.getCode() == KeyCode.RIGHT) {
    // typeUsersTree.getSelectionModel().getSelectedItem().setExpanded(true);
    // typeUsersTree.getSelectionModel().select(row);
    // // whereUsedTreeTable.getSelectionModel().focus(row);
    // // Not sure how to: whereUsedTreeTable.getSelectionModel().requestFocus();
    // // event.consume();
    // } else if (event.getCode() == KeyCode.LEFT) {
    // typeUsersTree.getSelectionModel().getSelectedItem().setExpanded(false);
    // typeUsersTree.getSelectionModel().select(row);
    // // whereUsedTreeTable.getSelectionModel().focus(row);
    // // event.consume();
    // }
    // }

    /**
     * Listener for selected library members in the tree table.
     *
     * @param item
     */
    private void memberSelectionListener(TreeItem<PropertiesDAO> item) {
        if (item == null || eventPublisherNode == null)
            return;
        log.debug( "Selection Listener: " + item.getValue() );
        OtmTypeUser p = null;
        if (item.getValue() != null && item.getValue().getValue() instanceof OtmTypeUser)
            p = (OtmTypeUser) item.getValue().getValue();

        OtmLibraryMember member = p != null ? p.getAssignedType().getOwningMember() : null;
        if (member == null && item.getValue() != null && item.getValue().getValue() instanceof OtmLibraryMember)
            member = (OtmLibraryMember) item.getValue().getValue();
        ignoreEvents = true;
        if (member != null)
            fireEvent( new DexMemberSelectionEvent( member, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED ) );
        ignoreEvents = false;
    }

    // public void mouseClick(MouseEvent event) {
    // // this fires after the member selection listener
    // if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
    // log.debug("Double click selection: ");
    // // + whereUsedTreeTable.getSelectionModel().getSelectedItem().getValue().nameProperty().toString());
    // }
    //
    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     * @throws Exception
     */
    @Override
    public void post(OtmLibraryMember member) throws Exception {
        if (member != null) {
            super.post( member );
            if (columnLabel != null)
                columnLabel.setText( "Properties of " + member.getName() + " that use types" );
            new PropertiesDAO( member, this ).createChildrenItems( root, new MemberAndUserFilter() );
        }
    }

    @Override
    public void refresh() {
        try {
            post( postedData );
        } catch (Exception e) {
            // No-op
        }
        ignoreEvents = false;
    }

    public void select(OtmLibraryMember otm) {
        if (otm != null) {
            for (TreeItem<PropertiesDAO> item : typeUsersTree.getRoot().getChildren()) {
                if (item.getValue().getValue() == otm) {
                    int row = typeUsersTree.getRow( item );
                    // This may not highlight the row if the event comes from or goes to a different controller.
                    Platform.runLater( () -> {
                        // ignoreEvents = true;
                        typeUsersTree.requestFocus();
                        typeUsersTree.getSelectionModel().clearAndSelect( row );
                        typeUsersTree.scrollTo( row );
                        typeUsersTree.getFocusModel().focus( row );
                        // ignoreEvents = false;
                    } );
                    // log.debug("Selected " + otm.getName() + " in member tree.");
                    return;
                }
            }
            // log.debug(otm.getName() + " not found in member tree.");
        }
    }

    // public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
    // typeUsersTree.setOnMouseClicked(handler);
    // }
}
