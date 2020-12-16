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
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage where-used tree for the posted library member. (1st column in tab)
 * 
 * @author dmh
 *
 */
public class UsersTreeController extends DexIncludedControllerBase<OtmLibraryMember> implements DexController {
//    private static Log log = LogFactory.getLog( UsersTreeController.class );

    //
    // FIXME - does not display an object when one of the choice facets is used as a type
    // see TravelInfo in TVP Finance
    // Does display the object when the choice facet is selected as the member
    //

    /*
     * FXML injected
     */
    @FXML
    TreeView<MemberAndUsersDAO> usersTree;
    @FXML
    private VBox memberWhereUsed;
    @FXML
    private Label columnLabel;

    TreeItem<MemberAndUsersDAO> root; // Root of the navigation tree. Is displayed.
    private boolean ignoreEvents = false;

    private OtmModelManager modelMgr;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED,
        DexModelChangeEvent.MODEL_CHANGED, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED};
    private static final EventType[] publishedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexMemberSelectionEvent.TYPE_USER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public UsersTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (usersTree == null)
            throw new IllegalStateException( "Type Users Tree view is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        postedData = null;
        usersTree.getRoot().getChildren().clear();
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        // log.debug("Configuring Member Tree Table.");
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
        this.modelMgr = modelMgr;

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
        usersTree.setOnMouseClicked( this::mouseClick );

        // Enable context menus at the row level and add change listener for for applying style
        usersTree.setCellFactory( (TreeView<MemberAndUsersDAO> p) -> new UserCellFactory( this ) );

        // log.debug("Where used table configured.");
        refresh();
    }

    // TODO - make this a reusable method.
    private void mouseClick(MouseEvent event) {
        // this fires after the member selection listener
        if (event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2) {
            TreeItem<MemberAndUsersDAO> item = usersTree.getSelectionModel().getSelectedItem();
            OtmObject obj = null;
            if (item != null)
                obj = item.getValue().getValue();
            if (obj != null) {
                OtmLibraryMember member = null;
                if (obj instanceof OtmLibraryMember)
                    member = (OtmLibraryMember) obj;
                else
                    member = obj.getOwningMember();

                log.debug( "Double Click on: " + member );
                if (member != null)
                    fireEvent( new DexMemberSelectionEvent( member, DexMemberSelectionEvent.MEMBER_SELECTED ) );
            }
        }
    }


    /**
     * For the member, get all members that use this member or any of its descendants as types.
     * <p>
     * Note: TreeItem class does not extend the Node class.
     * 
     * @param member post the where-used types for the member.
     * @param sprite the tree root or parent member
     * @return
     */
    public void createTreeItems(OtmLibraryMember member) {
        if (member == null || member.getWhereUsed() == null)
            return;
        // log.debug("Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName());
        // Usage Categories
        // As type
        // As site where contributed
        // As base type
        // As object exposed by resource
        // As extension facet in resource
        //
        // TODO - as parameter reference in a resource

        Map<String,UsersManager> namespaceMap = new HashMap<>();

        member.getWhereUsed().forEach( w -> addToMap( w, member, namespaceMap ) );
        member.getChildrenContributedFacets().forEach( cf -> addToMap( cf.getContributor(), member, namespaceMap ) );
    }

    private void addToMap(OtmLibraryMember w, OtmLibraryMember member, Map<String,UsersManager> namespaceMap) {
        if (w == null)
            return;
        if (!namespaceMap.containsKey( w.getPrefix() ))
            namespaceMap.put( w.getPrefix(), new UsersManager( w, member, root ) );
        // CF test must come first since they report having base type
        if (w instanceof OtmContextualFacet)
            namespaceMap.get( w.getPrefix() ).addContributed( w );
        else if (w.getBaseType() == member)
            namespaceMap.get( w.getPrefix() ).addBase( w );
        else if (w instanceof OtmResource)
            namespaceMap.get( w.getPrefix() ).addResource( w );
        else
            namespaceMap.get( w.getPrefix() ).addAssigned( w );
    }

    // // Create map of namespace prefixes with their tree items
    // Map<String,TreeItem<MemberAndUsersDAO>> usedPrefixes = new HashMap<>();
    // member.getWhereUsed().forEach( u -> {
    // if (!usedPrefixes.containsKey( u.getPrefix() )) {
    // TreeItem<MemberAndUsersDAO> nsItem =
    // new TreeItem<>( new MemberAndUsersDAO( new OtmNamespaceFacet( u ) ) );
    // usedPrefixes.put( u.getPrefix(), nsItem );
    // root.getChildren().add( nsItem );
    // nsItem.setExpanded( true );
    // }
    // } );
    //
    // member.getWhereUsed().forEach( u -> {
    // TreeItem<MemberAndUsersDAO> item = new TreeItem<>( new MemberAndUsersDAO( u ) );
    // if (usedPrefixes.get( u.getPrefix() ) != null)
    // usedPrefixes.get( u.getPrefix() ).getChildren().add( item );
    // else
    // root.getChildren().add( item );
    // } );
    // }


    // public void createTreeItems(List<EntitySearchResult> results) {
    // if (results == null)
    // return;
    // // log.debug("Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName());
    //
    // // Get all providers for this member
    // List<OtmLibraryMember> foundObjects = new ArrayList<>();
    // for (EntitySearchResult result : results) {
    // // This won't work ... entities are not in the model!
    // NamedEntity tl = result.findEntity( modelMgr.getTlModel() );
    // OtmObject otm = OtmModelElement.get( (TLModelElement) tl );
    // if (otm instanceof OtmLibraryMember)
    // foundObjects.add( (OtmLibraryMember) otm );
    // else
    // log.debug( "found named entity that is not library member." );
    // }
    // foundObjects.forEach( o -> new MemberAndUsersDAO( o ).createTreeItem( root ) );
    // }

    public TreeItem<MemberAndUsersDAO> getRoot() {
        return root;
    }

    public MemberAndUsersDAO getSelected() {
        return usersTree.getSelectionModel().getSelectedItem() != null
            ? usersTree.getSelectionModel().getSelectedItem().getValue() : null;
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (event.getEventType() == DexMemberSelectionEvent.MEMBER_SELECTED)
            post( event.getMember() );
        else if (event.getEventType() == DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED)
            usersTree.getSelectionModel().clearSelection();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (!ignoreEvents && event != null && event.getEventType() != null) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
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
    private void memberSelectionListener(TreeItem<MemberAndUsersDAO> item) {
        if (item == null || eventPublisherNode == null)
            return;
        // log.debug( "Selection Listener: " + item.getValue() );
        OtmLibraryMember member = null;
        if (item.getValue() != null && item.getValue().getValue() instanceof OtmLibraryMember)
            member = (OtmLibraryMember) item.getValue().getValue();
        ignoreEvents = true;
        if (member != null)
            fireEvent( new DexMemberSelectionEvent( member, DexMemberSelectionEvent.TYPE_USER_SELECTED ) );
        ignoreEvents = false;
    }

    @Override
    public void post(OtmLibraryMember member) {
        if (member == null || member == postedData)
            return;

        super.post( member );
        createTreeItems( member );
        if (columnLabel != null)
            columnLabel.setText( "Users " );
    }

    @Override
    public void refresh() {
        OtmLibraryMember member = postedData;
        postedData = null;
        post( member );
        ignoreEvents = false;
    }

    public void select(OtmLibraryMember otm) {
        if (otm != null) {
            for (TreeItem<MemberAndUsersDAO> item : usersTree.getRoot().getChildren()) {
                if (item.getValue().getValue() == otm) {
                    int row = usersTree.getRow( item );
                    // This may not highlight the row if the event comes from or goes to a different controller.
                    Platform.runLater( () -> {
                        // ignoreEvents = true;
                        usersTree.requestFocus();
                        usersTree.getSelectionModel().clearAndSelect( row );
                        usersTree.scrollTo( row );
                        usersTree.getFocusModel().focus( row );
                        // ignoreEvents = false;
                    } );
                    // log.debug("Selected " + otm.getName() + " in member tree.");
                    return;
                }
            }
            log.warn( otm.getName() + " not found in member tree." );
        }
    }
}
