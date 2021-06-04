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

package org.opentravel.dex.controllers.member;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.cellfactories.ValidationMemberTreeTableCellFactory;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexEventLockEvent;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.dex.events.OtmObjectReplacedEvent;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmModelMembersManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmEmptyTableFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorController;

import java.util.HashMap;
import java.util.Map;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Manage the library member navigation tree.
 * 
 * @author dmh
 *
 */
public class MemberTreeTableController extends DexIncludedControllerBase<OtmModelManager> implements DexController {
    private static Log log = LogFactory.getLog( MemberTreeTableController.class );

    // Column labels
    // To Do - externalize strings
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
    TreeTableView<MemberAndProvidersDAO> memberTree;
    @FXML
    private VBox memberTreeController;

    //
    TreeItem<MemberAndProvidersDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<MemberAndProvidersDAO,String> nameColumn; // an editable column

    OtmModelManager currentModelMgr; // this is postedData

    MemberFilterController filter = null;

    private boolean ignoreEvents = false;
    private boolean eventsLocked = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    private boolean treeEditingEnabled = true;

    private DexMainController parentController;

    // All event types listened to by this controller's handlers
    // Object events may change validation state of members
    private static final EventType[] subscribedEvents = {DexFilterChangeEvent.FILTER_CHANGED,
        DexMemberDeleteEvent.MEMBER_DELETED, OtmObjectReplacedEvent.OBJECT_REPLACED,
        DexMemberSelectionEvent.TYPE_USER_SELECTED, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED, OtmObjectChangeEvent.OBJECT_CHANGED,
        OtmObjectModifiedEvent.OBJECT_MODIFIED};
    private static final EventType[] publishedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexMemberSelectionEvent.DOUBLE_CLICK_MEMBER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public MemberTreeTableController() {
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
        memberTree.getColumns().addAll( nameColumn, valColumn, prefixColumn, libColumn, versionColumn, usedTypesCol );
        memberTree.getSortOrder().add( nameColumn );
        memberTree.getSortOrder().add( prefixColumn );
    }

    @Override
    public void checkNodes() {
        if (memberTree == null)
            throw new IllegalStateException( "Tree table view is null." );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (memberTree.getSelectionModel() != null)
            memberTree.getSelectionModel().clearSelection();

        if (memberTree.getRoot() != null)
            memberTree.getRoot().getChildren().clear();

        // log.debug( "Cleared member tree." );
    }

    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        // log.debug("Configuring Member Tree Table.");
        this.parentController = parent;
        eventPublisherNode = memberTreeController;
        if (parent != null)
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

        this.currentModelMgr = modelMgr;
        this.treeEditingEnabled = editable;

        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded

        // Set up the TreeTable
        memberTree.setRoot( getRoot() );
        memberTree.setShowRoot( false );
        memberTree.setEditable( true );
        memberTree.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        memberTree.setTableMenuButtonVisible( true ); // allow users to select columns
        // Enable context menus at the row level and add change listener for for applying style
        memberTree.setRowFactory( (TreeTableView<MemberAndProvidersDAO> p) -> new MemberRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        memberTree.getSelectionModel().select( 0 );
        memberTree.setOnKeyReleased( this::keyReleased );
        // memberTree.setOnMouseClicked(this::mouseClick);
        memberTree.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        setOnMouseClicked( this::mouseClick ); // events for drawing pane

        refresh();
    }

    private Map<OtmObject,TreeItem<MemberAndProvidersDAO>> itemMap =
        new HashMap<>( OtmModelMembersManager.MEMBERCOUNT );

    private OtmEmptyTableFacet emptyTableFacet = null;

    /**
     * Note: TreeItem class does not extend the Node class. Therefore, you cannot apply any visual effects or add menus
     * to the tree items. Use the cell factory mechanism to overcome this obstacle and define as much custom behavior
     * for the tree items as your application requires.
     * <p>
     * // 12/16/2020 - added map to cache DAOs
     * <p>
     * // 12/15/2020 - filter applied by model manager
     * <p>
     * // 1/3/2020 - let the CFs be shown, the users expect to see them
     * 
     * @param member the Otm Library Member to add to the tree
     * @param parent the tree root or parent member
     * @return
     */
    public void createTreeItem(OtmLibraryMember member, TreeItem<MemberAndProvidersDAO> parent) {
        // log.debug( "Creating member tree item for: " + member + " of type " + member.getClass().getSimpleName() );
        // TreeItem<MemberAndProvidersDAO> item = null;
        TreeItem<MemberAndProvidersDAO> item = itemMap.get( member );
        if (item != null) {
            parent.getChildren().add( item );
        } else {
            // Create item for the library member
            item = new MemberAndProvidersDAO( member ).createTreeItem( parent );
            itemMap.put( member, item );

            // Create and add items for children of this member
            if (member instanceof OtmChildrenOwner)
                createChildrenItems( member, item );
        }
    }

    public void createTreeItem(OtmAbstractDisplayFacet member, TreeItem<MemberAndProvidersDAO> parent) {
        new MemberAndProvidersDAO( member ).createTreeItem( parent );
    }

    /**
     * Create tree items for the type provider children of this child owning member
     */
    private void createChildrenItems(OtmChildrenOwner childrenOwner, TreeItem<MemberAndProvidersDAO> parentItem) {
        // Collection<OtmTypeProvider> providers = childrenOwner.getChildrenTypeProviders();
        // List<OtmObject> kids = childrenOwner.getChildren();
        // log.debug( "Creating children items of: " + childrenOwner );
        // if (childrenOwner instanceof OtmContributedFacet) {
        // log.debug( "Processing contributed facet." );
        // log.debug( "Children" + childrenOwner.getChildrenTypeProviders() );
        // }
        // if (childrenOwner instanceof OtmContextualFacet) {
        // log.debug( "Processing contextual facet." );
        // Collection<OtmTypeProvider> children = childrenOwner.getChildrenTypeProviders();
        // log.debug( "Children" + children );
        // }

        childrenOwner.getChildrenTypeProviders().forEach( p -> {
            // log.debug( " Creating child item: " + p );
            TreeItem<MemberAndProvidersDAO> item = null;

            // Create item for the library member
            item = new MemberAndProvidersDAO( p ).createTreeItem( parentItem );

            // Only use contextual facet for recursing
            if (p instanceof OtmContributedFacet && ((OtmContributedFacet) p).getContributor() != null)
                p = ((OtmContributedFacet) p).getContributor();

            // Recurse
            if (p instanceof OtmChildrenOwner)
                createChildrenItems( (OtmChildrenOwner) p, item );
        } );
    }

    public MemberFilterController getFilter() {
        if (filter == null && getMainController() instanceof ObjectEditorController) {
            filter = ((ObjectEditorController) getMainController()).getMemberFilterController();
        }
        if (filter != null)
            filter.setController( this );
        // else
        // log.error( "Missing member filter controller." );
        return filter;
    }

    public TreeItem<MemberAndProvidersDAO> getRoot() {
        return root;
    }

    public MemberAndProvidersDAO getSelected() {
        return memberTree.getSelectionModel().getSelectedItem() != null
            ? memberTree.getSelectionModel().getSelectedItem().getValue() : null;
    }

    // private void handleEvent(DexFilterChangeEvent event) {
    // // if (!ignoreEvents)
    // // refresh();
    // // refresh() is Called directly from filter controller
    // }


    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. Ignore? " + ignoreEvents );
        if (event instanceof DexEventLockEvent)
            handleEvent( (DexEventLockEvent) event ); // super-type method
        else if (!ignoreEvents && !eventsLocked) {
            if (event instanceof DexMemberSelectionEvent)
                handleEvent( (DexMemberSelectionEvent) event );
            // else if (event instanceof DexFilterChangeEvent)
            // handleEvent( (DexFilterChangeEvent) event );
            else if (event instanceof DexChangeEvent) {
                // Future - be selective using event member which may be a contextual facet
                itemMap.clear();
                refresh();
            } else
                refresh(); // else if (event instanceof OtmObjectChangeEvent)
            // refresh();
            // else if (event instanceof OtmObjectReplacedEvent)
            // handleEvent( (OtmObjectReplacedEvent) event );
            // else if (event instanceof DexMemberDeleteEvent)
            // refresh();
            // else if (event instanceof DexModelChangeEvent) {
            // itemMap.clear();
            // refresh();
        }
    }



    // private void handleEvent(DexEventLockEvent event) {
    // // TODO - only react to members of same parent as source
    // Object source = event.getSource();
    // eventsLocked = event.get();
    // }

    // private void handleEvent(OtmObjectReplacedEvent event) {
    // refresh();
    // }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (event.getEventType().equals( DexMemberSelectionEvent.RESOURCE_SELECTED ))
            return;
        if (!ignoreEvents)
            select( event.getMember() );
    }

    public void keyReleased(KeyEvent event) {
        TreeItem<MemberAndProvidersDAO> item = memberTree.getSelectionModel().getSelectedItem();
        int row = memberTree.getSelectionModel().getSelectedIndex();
        if (item == null)
            return;

        // log.debug("Selection row = " + row);
        if (event.getCode() == KeyCode.RIGHT) {
            event.consume();
            item.setExpanded( true );
            memberTree.getSelectionModel().clearAndSelect( row + 1, nameColumn );
        } else if (event.getCode() == KeyCode.LEFT) {
            TreeItem<MemberAndProvidersDAO> parent = item.getParent();
            if (parent != null && parent != item && parent != root) {
                memberTree.getSelectionModel().select( parent );
                parent.setExpanded( false );
                row = memberTree.getSelectionModel().getSelectedIndex();
                memberTree.getSelectionModel().clearAndSelect( row, nameColumn );
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
        // log.debug( "Selection Listener: " + item.getValue() );
        // assert item != null;
        boolean editable = false;
        if (treeEditingEnabled && item.getValue() != null)
            editable = item.getValue().isEditable();
        nameColumn.setEditable( editable ); // TODO - is this still useful?
        ignoreEvents = true;
        if (eventPublisherNode != null)
            eventPublisherNode.fireEvent( new DexMemberSelectionEvent( this, item ) );
        ignoreEvents = false;
    }

    public void mouseClick(MouseEvent event) {
        // this fires after the member selection listener
        OtmObject item = null;
        if (memberTree.getSelectionModel().getSelectedItem() != null)
            item = memberTree.getSelectionModel().getSelectedItem().getValue().getValue();
        // log.debug( "Click selection: " + item );
        if (item instanceof OtmLibraryMember && event.getButton().equals( MouseButton.PRIMARY )
            && event.getClickCount() == 2) {
            // log.debug( "Double click selection: " + item );
            ignoreEvents = true;
            if (eventPublisherNode != null)
                eventPublisherNode.fireEvent( new DexMemberSelectionEvent( (OtmLibraryMember) item, true ) );
            ignoreEvents = false;
        }
    }

    public OtmEmptyTableFacet getEmptyTableObject() {
        return new OtmEmptyTableFacet( currentModelMgr );
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmModelManager modelMgr) {
        ignoreEvents = true;
        // log.debug( "\nPosting member tree." );
        if (modelMgr != null && memberTree != null) {
            currentModelMgr = modelMgr;
            clear();

            // create cells for filter selected members
            currentModelMgr.getMembers( getFilter() ).forEach( m -> createTreeItem( m, root ) );

            // If no members, post an empty row to allow row factory to add menu items
            if (root.getChildren().isEmpty())
                createTreeItem( getEmptyTableObject(), root );

            // Sort members
            try {
                memberTree.sort();
            } catch (Exception e) {
                // log.warn( "Exception sorting: " + e.getLocalizedMessage() );
            }
        }
        ignoreEvents = false;
        // log.debug( "Posted member tree." );
    }

    /**
     * {@inheritDoc} Clear the tree and post the model.
     * <p>
     * Also clear the item cache.
     * 
     * @see #post(OtmModelManager)
     * @see org.opentravel.dex.controllers.DexIncludedControllerBase#refresh()
     */
    @Override
    public void refresh() {
        itemMap.clear();
        post( currentModelMgr );
        // log.debug( "Refreshed member tree." );
    }

    public void select(OtmLibraryMember otm) {
        memberTree.getSelectionModel().clearSelection();
        if (otm != null) {
            for (TreeItem<MemberAndProvidersDAO> item : memberTree.getRoot().getChildren()) {
                if (item.getValue().getValue() == otm) {
                    int row = memberTree.getRow( item );
                    // This may not highlight the row if the event comes from or goes to a different controller.
                    Platform.runLater( () -> {
                        // ignoreEvents = true;
                        memberTree.requestFocus();
                        memberTree.getSelectionModel().clearAndSelect( row );
                        memberTree.scrollTo( row );
                        memberTree.getFocusModel().focus( row );
                        // ignoreEvents = false;
                    } );
                    // log.debug("Selected " + otm.getName() + " in member tree.");
                    return;
                }
            }
            // log.warn( otm.getName() + " not found in member tree." );
        }
    }

    public void setFilter(MemberFilterController filter) {
        this.filter = filter;
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> handler) {
        memberTree.setOnMouseClicked( handler );
    }
}
