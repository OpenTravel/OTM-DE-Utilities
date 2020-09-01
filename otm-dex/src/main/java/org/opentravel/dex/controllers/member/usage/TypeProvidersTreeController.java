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
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmNamespaceFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.HashMap;
import java.util.Map;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage tree for members and type providers that provide types to the posted member (3rd, right hand column).
 * 
 * @author dmh
 *
 */
public class TypeProvidersTreeController extends DexIncludedControllerBase<OtmLibraryMember> implements DexController {
    private static Log log = LogFactory.getLog( TypeProvidersTreeController.class );

    /*
     * FXML injected
     */
    @FXML
    TreeView<MemberAndProvidersDAO> typeProvidersTree;
    @FXML
    private VBox typeProvidersVBox;
    @FXML
    private Label columnLabel;

    TreeItem<MemberAndProvidersDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED,
            OtmObjectChangeEvent.OBJECT_CHANGED, DexMemberSelectionEvent.TYPE_USER_SELECTED};
    private static final EventType[] publishedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public TypeProvidersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (typeProvidersVBox == null)
            throw new IllegalStateException( "TypeProvidersTreeController's Member Where Used is null." );
        if (typeProvidersTree == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedData = null;
        typeProvidersTree.getRoot().getChildren().clear();
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

        eventPublisherNode = typeProvidersVBox;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        typeProvidersTree.setRoot( getRoot() );
        typeProvidersTree.setShowRoot( false );
        typeProvidersTree.setEditable( false );

        // Add listeners and event handlers
        typeProvidersTree.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );
        typeProvidersTree.setOnMouseClicked( this::doubleClick );

        // Enable context menus at the row level and add change listener for for applying style
        typeProvidersTree.setCellFactory( (TreeView<MemberAndProvidersDAO> p) -> new TypeProviderCellFactory( this ) );

        // log.debug("Where used table configured.");
        refresh();
    }

    public void doubleClick(MouseEvent click) {
        if (click.getClickCount() == 2) {
            // Broadcast a broader event type than single click
            memberSelectionListener( typeProvidersTree.getSelectionModel().getSelectedItem(),
                DexMemberSelectionEvent.MEMBER_SELECTED );
        }
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

    @Override
    public DexDAO<?> getSelection() {
        if (typeProvidersTree.getSelectionModel().getSelectedItem() != null)
            return typeProvidersTree.getSelectionModel().getSelectedItem().getValue();
        return null;
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (event.getEventType() == DexMemberSelectionEvent.MEMBER_SELECTED)
            post( event.getMember() );
        else if (event.getEventType() == DexMemberSelectionEvent.TYPE_USER_SELECTED) {
            typeProvidersTree.getSelectionModel().clearSelection();
            // TODO - this logic selects the right tree item, but it does not highlight.
            // TODO - use the event member to find the corresponding tree item or row number to select it.
            // TODO - re-factor into OtmNamespaceFacet.contains()
            // OtmLibraryMember member = event.getMember();
            // for (TreeItem<MemberAndProvidersDAO> ti : typeProvidersTree.getRoot().getChildren()) {
            // OtmObject candidate = ti.getValue().getValue();
            // if (candidate instanceof OtmNamespaceFacet
            // && ((OtmNamespaceFacet) candidate).getNamespace().equals( member.getNamespace() ))
            // for (TreeItem<MemberAndProvidersDAO> ti2 : ti.getChildren()) {
            // // OtmObject c2 = ti2.getValue().getValue();
            // if (ti2.getValue().getValue() == event.getMember()) {
            // typeProvidersTree.getSelectionModel().select( ti2 );
            // typeProvidersTree.getFocusModel()
            // .focus( typeProvidersTree.getSelectionModel().getSelectedIndex() );
            // log.debug( "Type provider tree - Selected " + ti2.getValue().getValue() + " at index "
            // + typeProvidersTree.getSelectionModel().getSelectedIndex() );
            // }
            // }
            // }
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (!ignoreEvents && event != null && event.getEventType() != null) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            else if (event instanceof OtmObjectChangeEvent)
                refresh();
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
    private void memberSelectionListener(TreeItem<MemberAndProvidersDAO> item) {
        memberSelectionListener( item, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED );
    }

    private void memberSelectionListener(TreeItem<MemberAndProvidersDAO> item,
        EventType<DexMemberSelectionEvent> eventType) {
        if (item == null || eventPublisherNode == null)
            return; // Nothing to do
        // log.debug( "Selection Listener: " + item.getValue() );
        OtmLibraryMember member = null;
        if (item.getValue() != null && item.getValue().getValue() instanceof OtmObject)
            member = item.getValue().getValue().getOwningMember();

        if (!ignoreEvents && member != null)
            fireEvent( new DexMemberSelectionEvent( member, eventType ) );
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmLibraryMember member) {
        if (member == null || member == postedData)
            return;
        super.post( member );
        if (columnLabel != null)
            columnLabel.setText( "Providers" );

        // log.debug( "Posting type providers to: " + member );

        // Create map of namespace prefixes and tree items
        Map<String,TreeItem<MemberAndProvidersDAO>> usedPrefixes = new HashMap<>();
        member.getUsedTypes().forEach( u -> {
            if (!usedPrefixes.containsKey( u.getPrefix() )) {
                TreeItem<MemberAndProvidersDAO> nsItem =
                    new TreeItem<>( new MemberAndProvidersDAO( new OtmNamespaceFacet( u ) ) );
                usedPrefixes.put( u.getPrefix(), nsItem );
                root.getChildren().add( nsItem );
                nsItem.setExpanded( true );
            }
        } );

        member.getUsedTypes().forEach( u -> {
            TreeItem<MemberAndProvidersDAO> item = new TreeItem<>( new MemberAndProvidersDAO( u, member ) );
            if (usedPrefixes.get( u.getPrefix() ) != null)
                usedPrefixes.get( u.getPrefix() ).getChildren().add( item );
            else
                root.getChildren().add( item );
        } );
    }

    @Override
    public void refresh() {
        OtmLibraryMember member = postedData;
        postedData = null;
        post( member );
        ignoreEvents = false;
    }
}
