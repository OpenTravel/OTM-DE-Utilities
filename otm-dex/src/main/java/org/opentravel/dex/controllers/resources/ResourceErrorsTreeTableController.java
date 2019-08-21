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
import org.opentravel.common.ValidationUtils;
import org.opentravel.common.cellfactories.ValidationErrorsAndWarningsTreeTableCellFactory;
import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;

/**
 * Manage the library member navigation tree.
 * 
 * @author dmh
 *
 */
public class ResourceErrorsTreeTableController extends DexIncludedControllerBase<OtmResource> implements DexController {
    private static Log log = LogFactory.getLog( ResourceErrorsTreeTableController.class );

    // Column labels
    public static final String LEVELLABEL = "Level";
    private static final String OBJECTLABEL = "Member";
    private static final String DESCRIPTIONLABEL = "Description";

    /*
     * FXML injected
     */
    @FXML
    TreeTableView<ErrorsAndWarningsDAO> resourceErrors;
    @FXML
    private TitledPane resourceErrorsPane;
    @FXML
    private ImageView objectImageView;

    //
    TreeItem<ErrorsAndWarningsDAO> root; // Root of the navigation tree. Is displayed.
    TreeTableColumn<ErrorsAndWarningsDAO,String> nameColumn; // an editable column


    private boolean ignoreEvents = false;
    // By default, the tree is editable. Setting this to false will prevent edits.
    private boolean treeEditingEnabled = true;

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED,
        DexMemberSelectionEvent.RESOURCE_SELECTED, DexModelChangeEvent.MODEL_CHANGED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};

    /**
     * Construct a member tree table controller that can publish and receive events.
     */
    public ResourceErrorsTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create columns
     */
    private void buildColumns() {

        TreeTableColumn<ErrorsAndWarningsDAO,String> levelColumn = new TreeTableColumn<>( LEVELLABEL );
        levelColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ErrorsAndWarningsDAO,String>( "level" ) );
        setColumnProps( levelColumn, true, false, true, 50 );
        levelColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

        nameColumn = new TreeTableColumn<>( OBJECTLABEL );
        nameColumn.setCellValueFactory( new TreeItemPropertyValueFactory<ErrorsAndWarningsDAO,String>( "name" ) );
        setColumnProps( nameColumn, true, true, true, 200 );
        nameColumn.setSortType( SortType.ASCENDING );

        TreeTableColumn<ErrorsAndWarningsDAO,String> descColumn = new TreeTableColumn<>( DESCRIPTIONLABEL );
        descColumn
            .setCellValueFactory( new TreeItemPropertyValueFactory<ErrorsAndWarningsDAO,String>( "description" ) );
        setColumnProps( descColumn, true, true, true, 500 );


        TreeTableColumn<ErrorsAndWarningsDAO,ImageView> valColumn = new TreeTableColumn<>( "" );
        valColumn.setCellFactory( c -> new ValidationErrorsAndWarningsTreeTableCellFactory() );
        setColumnProps( valColumn, true, false, false, 25 );

        // Add columns to table
        resourceErrors.getColumns().addAll( nameColumn, valColumn, levelColumn, descColumn );
        resourceErrors.getSortOrder().add( nameColumn );
    }

    @Override
    public void checkNodes() {
        if (resourceErrors == null)
            throw new IllegalStateException( "Resource errors tree table is null." );
    }

    /**
     * {@inheritDoc} Remove all items from the member tree.
     */
    @Override
    public void clear() {
        resourceErrors.getSelectionModel().clearSelection();
        resourceErrors.getRoot().getChildren().clear();
        postedData = null;
    }

    /**
     * Configure the controller for use by main controller.
     */
    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        // log.debug("Configuring Member Tree Table.");
        eventPublisherNode = resourceErrorsPane;
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
        resourceErrors.setRoot( getRoot() );
        resourceErrors.setShowRoot( false );
        resourceErrors.setEditable( true );
        resourceErrors.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        resourceErrors.setTableMenuButtonVisible( true ); // allow users to select columns
        // Enable context menus at the row level and add change listener for for applying style
        // resourceErrors.setRowFactory( (TreeTableView<ErrorsAndWarningsDAO> p) -> new MemberRowFactory( this ) );
        buildColumns();

        // Add listeners and event handlers
        resourceErrors.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> memberSelectionListener( newValue ) );

        refresh();
    }


    public TreeItem<ErrorsAndWarningsDAO> getRoot() {
        return root;
    }

    public ErrorsAndWarningsDAO getSelected() {
        return resourceErrors.getSelectionModel().getSelectedItem() != null
            ? resourceErrors.getSelectionModel().getSelectedItem().getValue() : null;
    }

    // private void handleEvent(DexFilterChangeEvent event) {
    // if (!ignoreEvents)
    // refresh();
    // }

    private void handleEvent(DexMemberSelectionEvent event) {
        if (!ignoreEvents && event != null && event.getMember() instanceof OtmResource)
            post( (OtmResource) event.getMember() );
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


    /**
     * Listener for selected library members in the tree table.
     * 
     * @param item
     */
    private void memberSelectionListener(TreeItem<ErrorsAndWarningsDAO> item) {
        if (item == null || item.getValue() == null)
            return;
        // log.debug("Selection Listener: " + item.getValue());

        // Fire an event for the owning library member (if any)
        OtmObject object = item.getValue().getValue();
        OtmLibraryMember member = null;
        if (object != null && !(object instanceof OtmLibraryMember))
            member = object.getOwningMember();
        if (eventPublisherNode != null && member != null) {
            ignoreEvents = true;
            eventPublisherNode.fireEvent( new DexMemberSelectionEvent( member ) );
            ignoreEvents = false;
        }
    }

    /**
     * Get the library members from the model manager and put them into a cleared tree.
     * 
     * @param modelMgr
     */
    @Override
    public void post(OtmResource resource) {
        ignoreEvents = true;
        if (resource != null && resourceErrors != null) {
            clear();
            postedData = resource;

            // Put OK/Warn/Error icon in title
            objectImageView.setImage( resource.validationImage().getImage() );

            ValidationFindings findings = resource.getFindings();
            String f = ValidationUtils.getMessagesAsString( findings );

            for (ValidationFinding finding : findings.getAllFindingsAsList())
                new ErrorsAndWarningsDAO( finding ).createTreeItem( root );
        }
        ignoreEvents = false;
    }

    @Override
    public void refresh() {
        post( postedData );
    }

}
