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
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Manage tree for members and type providers that provide types to the posted member.
 * 
 * @author dmh
 *
 */
public class TypeProvidersTreeController extends DexIncludedControllerBase<OtmModelManager> implements DexController {
    private static Log log = LogFactory.getLog( TypeProvidersTreeController.class );

    /*
     * FXML injected
     */
    @FXML
    TreeView<MemberAndProvidersDAO> typeProvidersTree;
    @FXML
    private VBox memberWhereUsed;

    TreeItem<MemberAndProvidersDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;

    private OtmLibraryMember postedMember;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public TypeProvidersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (typeProvidersTree == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedMember = null;
        typeProvidersTree.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = memberWhereUsed;
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

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        typeProvidersTree.setRoot( getRoot() );
        typeProvidersTree.setShowRoot( false );
        typeProvidersTree.setEditable( true );

        // Add listeners and event handlers
        // typeProvidersTree.getSelectionModel().select(0);
        // typeProvidersTree.getSelectionModel().selectedItemProperty()
        // .addListener((v, old, newValue) -> memberSelectionListener(newValue));

        // log.debug("Where used table configured.");
        refresh();
    }

    /**
     * Note: TreeItem class does not extend the Node class. Therefore, you cannot apply any visual effects or add menus
     * to the tree items. Use the cell factory mechanism to overcome this obstacle and define as much custom behavior
     * for the tree items as your application requires.
     * 
     * @param member the Otm Library Member to add to the tree
     * @param parent the tree root or parent member
     * @return
     */
    public void createTreeItem(OtmLibraryMember member, TreeItem<MemberAndProvidersDAO> parent) {
        // log.debug("Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName());

        // Skip over contextual facets that have been injected into an object. Their contributed facets will be modeled.
        if ((member instanceof OtmContextualFacet && ((OtmContextualFacet) member).getWhereContributed() != null))
            return;

        // Create item for the library member
        TreeItem<MemberAndProvidersDAO> item = createTreeItem( (OtmTypeProvider) member, parent );

        // Create and add items for children
        if (member instanceof OtmChildrenOwner)
            createChildrenItems( member, item );
    }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner member, TreeItem<MemberAndProvidersDAO> parentItem) {
        member.getChildrenTypeProviders().forEach( p -> {
            TreeItem<MemberAndProvidersDAO> cfItem = createTreeItem( p, parentItem );
            // Only user contextual facet for recursing
            if (p instanceof OtmContributedFacet && ((OtmContributedFacet) p).getContributor() != null)
                p = ((OtmContributedFacet) p).getContributor();
            // Recurse
            if (p instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) p, cfItem );
        } );
    }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    private TreeItem<MemberAndProvidersDAO> createTreeItem(OtmTypeProvider provider,
        TreeItem<MemberAndProvidersDAO> parent) {
        TreeItem<MemberAndProvidersDAO> item = new TreeItem<>( new MemberAndProvidersDAO( provider ) );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );
        ImageView graphic = ImageManager.get( provider );
        item.setGraphic( graphic );
        Tooltip.install( graphic, new Tooltip( provider.getObjectTypeName() ) );
        return item;
    }

    public TreeItem<MemberAndProvidersDAO> getRoot() {
        return root;
    }

    public MemberAndProvidersDAO getSelected() {
        return typeProvidersTree.getSelectionModel().getSelectedItem() != null
            ? typeProvidersTree.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents)
            post( event.getMember() );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (!ignoreEvents) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();
        }
    }
    //
    // public void keyReleased(KeyEvent event) {
    // // TreeItem<MemberDAO> item = whereUsedTreeTable.getSelectionModel().getSelectedItem();
    // // ObservableList<TreeTablePosition<MemberDAO, ?>> cells =
    // // whereUsedTreeTable.getSelectionModel().getSelectedCells();
    // int row = typeProvidersTree.getSelectionModel().getSelectedIndex();
    // log.debug("Selection row = " + row);
    // if (event.getCode() == KeyCode.RIGHT) {
    // typeProvidersTree.getSelectionModel().getSelectedItem().setExpanded(true);
    // typeProvidersTree.getSelectionModel().select(row);
    // // whereUsedTreeTable.getSelectionModel().focus(row);
    // // Not sure how to: whereUsedTreeTable.getSelectionModel().requestFocus();
    // // event.consume();
    // } else if (event.getCode() == KeyCode.LEFT) {
    // typeProvidersTree.getSelectionModel().getSelectedItem().setExpanded(false);
    // typeProvidersTree.getSelectionModel().select(row);
    // // whereUsedTreeTable.getSelectionModel().focus(row);
    // // event.consume();
    // }
    // }

    // /**
    // * Listener for selected library members in the tree table.
    // *
    // * @param item
    // */
    // private void memberSelectionListener(TreeItem<MemberAndProvidersDAO> item) {
    // if (item == null)
    // return;
    // log.debug("Selection Listener: " + item.getValue());
    // assert item != null;
    // boolean editable = false;
    // if (item.getValue() != null)
    // editable = item.getValue().isEditable();
    // // nameColumn.setEditable(editable); // TODO - is this still useful?
    // ignoreEvents = true;
    // if (eventPublisherNode != null)
    // eventPublisherNode.fireEvent(new DexMemberSelectionEvent(this, item));
    // ignoreEvents = false;
    // }

    // public void mouseClick(MouseEvent event) {
    // // this fires after the member selection listener
    // if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
    // log.debug("Double click selection: ");
    // // + whereUsedTreeTable.getSelectionModel().getSelectedItem().getValue().nameProperty().toString());
    // }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    // @Override
    public void post(OtmLibraryMember member) {
        clear();
        postedMember = member;
        if (member == null)
            return;

        // log.debug( "Posting type providers to: " + member );
        // TODO - organize by namespace then object
        member.getUsedTypes().forEach( u -> {
            TreeItem<MemberAndProvidersDAO> item = new TreeItem<>( new MemberAndProvidersDAO( u ) );
            root.getChildren().add( item );
        } );
        // typeProvidersTree.sort();
    }

    @Override
    public void refresh() {
        post( postedMember );
        ignoreEvents = false;
    }

    // public void select(OtmLibraryMember otm) {
    // post( otm );
    // if (otm != null) {
    // for (TreeItem<MemberDAO> item : typeProvidersTree.getRoot().getChildren()) {
    // if (item.getValue().getValue() == otm) {
    // int row = typeProvidersTree.getRow(item);
    // // This may not highlight the row if the event comes from or goes to a different controller.
    // Platform.runLater(() -> {
    // // ignoreEvents = true;
    // typeProvidersTree.requestFocus();
    // typeProvidersTree.getSelectionModel().clearAndSelect(row);
    // typeProvidersTree.scrollTo(row);
    // typeProvidersTree.getFocusModel().focus(row);
    // // ignoreEvents = false;
    // });
    // log.debug("Selected " + otm.getName() + " in member tree.");
    // return;
    // }
    // }
    // log.debug(otm.getName() + " not found in member tree.");
    // }
    // }

    // public void setFilter(MemberFilterController filter) {
    // this.filter = filter;
    // }

    // public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
    // typeProvidersTree.setOnMouseClicked(handler);
    // }
}
