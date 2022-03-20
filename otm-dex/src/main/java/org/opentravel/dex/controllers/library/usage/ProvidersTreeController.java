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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexDAO;
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

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * Manage tree for libraries that use subject library.
 * 
 * @author dmh
 *
 */
public class ProvidersTreeController extends DexIncludedControllerBase<OtmLibrary> {
    private static Logger log = LogManager.getLogger( ProvidersTreeController.class );

    /*
     * FXML injected
     */
    @FXML
    TreeView<LibraryAndMembersDAO> providersTree;
    @FXML
    private VBox providersVBox;
    @FXML
    private Label columnLabel;

    TreeItem<LibraryAndMembersDAO> root; // Root of the navigation tree. Is displayed.
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
    public ProvidersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (providersVBox == null)
            throw new IllegalStateException( "TypeProvidersTreeController's Member Where Used is null." );
        if (providersTree == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedData = null;
        providersTree.getRoot().getChildren().clear();
        columnLabel.setText( "" );
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        // log.debug("Configuring Member Tree Table.");
        configure( parent.getModelManager() );
    }

    /**
     * Configure controller for use by non-main controllers.
     * 
     * @param modelMgr must not be null
     * @param editable sets tree editing enables
     */
    private void configure(OtmModelManager modelMgr) {
        if (modelMgr == null)
            throw new IllegalArgumentException(
                "Model manager is null. Must configure member tree with model manager." );

        eventPublisherNode = providersVBox;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        providersTree.setRoot( getRoot() );
        providersTree.setShowRoot( false );
        providersTree.setEditable( false );

        // Add listeners and event handlers
        providersTree.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        // Enable context menus at the row level and add change listener for for applying style
        // FIXME providersTree.setCellFactory( (TreeView<LibraryAndMembersDAO> p) -> new TypeProviderCellFactory( this )
        // );

        // log.debug("Where used table configured.");
        refresh();
    }

    public TreeItem<LibraryAndMembersDAO> getRoot() {
        return root;
    }

    @Override
    public DexDAO<?> getSelection() {
        if (providersTree.getSelectionModel().getSelectedItem() != null)
            return providersTree.getSelectionModel().getSelectedItem().getValue();
        return null;
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (!ignoreEvents && event != null && event.getEventType() != null) {
            if (event instanceof DexLibrarySelectionEvent)
                post( ((DexLibrarySelectionEvent) event).getLibrary() );
            else if (event instanceof DexModelChangeEvent)
                refresh();
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
            return; // Nothing to do

        // log.debug( "Selection Listener: " + item.getValue() );
        OtmLibraryMember member = null;
        if (item.getValue() != null && item.getValue().getValue() instanceof OtmLibraryMember)
            member = (OtmLibraryMember) item.getValue().getValue();

        ignoreEvents = true;
        if (member != null)
            fireEvent( new DexMemberSelectionEvent( member, DexMemberSelectionEvent.MEMBER_SELECTED ) );
        ignoreEvents = false;
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmLibrary library) {
        if (library == null || library == postedData)
            return;
        super.post( library );
        // log.debug( "Posting providers of types to " + library.getName() );

        if (columnLabel != null)
            columnLabel.setText( "Providers of types to " + library.getName() + " " + library.getVersion() );
        // log.debug( "Posting type providers to: " + member );

        LibraryAndMembersDAO.createChildrenItems( library.getProvidersMap(), getRoot() );
        // providersTree.requestFocus();
        // log.debug( "Posted providers of types to " + library.getName() );
    }

    @Override
    public void refresh() {
        OtmLibrary lib = postedData;
        postedData = null;
        if (lib != null && getModelManager().contains( lib.getTL() ))
            post( lib );
        else
            clear();
        ignoreEvents = false;

    }
}
