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

package org.opentravel.dex.controllers.member.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.DexRepeatMaxConverter;
import org.opentravel.common.cellfactories.AssignedTypePropertiesTreeTableCellFactory;
import org.opentravel.common.cellfactories.ValidationPropertiesTreeTableCellFactory;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexFacetSelectionEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexPropertySelectionEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.dex.events.OtmObjectReplacedEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Manage a facets and properties in a tree table.
 * 
 * @author dmh
 *
 */
public class MemberPropertiesTreeTableController extends DexIncludedControllerBase<OtmLibraryMember> {
    private static Log log = LogFactory.getLog( MemberPropertiesTreeTableController.class );

    private static final EventType[] publishedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED,
            DexFacetSelectionEvent.FACET_SELECTED, DexPropertySelectionEvent.PROPERTY_SELECTED};

    private static final EventType[] subscribedEvents =
        {DexMemberDeleteEvent.MEMBER_DELETED, OtmObjectReplacedEvent.OBJECT_REPLACED,
            OtmObjectChangeEvent.OBJECT_CHANGED, OtmObjectModifiedEvent.OBJECT_MODIFIED,
            DexMemberSelectionEvent.TYPE_USER_SELECTED, DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED,
            DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    @FXML
    protected TreeTableView<PropertiesDAO> propertiesTable;
    @FXML
    private VBox memberProperties;

    // Table Columns
    protected TreeItem<PropertiesDAO> root;
    protected TreeTableColumn<PropertiesDAO,String> nameCol;
    protected TreeTableColumn<PropertiesDAO,String> roleCol;
    protected TreeTableColumn<PropertiesDAO,String> typeCol;
    protected TreeTableColumn<PropertiesDAO,String> minCol;
    protected TreeTableColumn<PropertiesDAO,Integer> maxCol;

    protected TreeTableColumn<PropertiesDAO,String> exampleCol;
    protected TreeTableColumn<PropertiesDAO,String> descCol;
    protected TreeTableColumn<PropertiesDAO,String> deprecatedCol;
    protected TreeTableColumn<PropertiesDAO,String> otherDocCol;

    /**
     * Create a facet and property treeTable with manager.
     * 
     */
    public MemberPropertiesTreeTableController() {
        super( subscribedEvents, publishedEvents );
    }

    /**
     * Create Columns and set cell values
     */
    @SuppressWarnings("unchecked")
    private void buildColumns(TreeTableView<PropertiesDAO> table) {

        // Name Column
        nameCol = new TreeTableColumn<>( "Name" );
        setColumnProps( nameCol, true, true, false, 200, "name" );

        // Role Column
        roleCol = new TreeTableColumn<>( "Role" );
        setColumnProps( roleCol, true, true, false, 100 );
        roleCol.setCellValueFactory( new TreeItemPropertyValueFactory<PropertiesDAO,String>( "role" ) );
        roleCol.setCellFactory( ChoiceBoxTreeTableCell.forTreeTableColumn( PropertiesDAO.getRoleList() ) );

        // Assigned type column
        typeCol = new TreeTableColumn<>( "Assigned Type" );
        setColumnProps( typeCol, true, true, false, 150 );
        typeCol.setCellValueFactory( new TreeItemPropertyValueFactory<PropertiesDAO,String>( "assignedType" ) );
        typeCol.setCellFactory( c -> new AssignedTypePropertiesTreeTableCellFactory( this ) );

        // Example Column
        exampleCol = new TreeTableColumn<>( "Example" );
        setColumnProps( exampleCol, true, true, false, 0, "example" );
        // setColumnProps( exampleCol, false, false, false, 0 );

        // Documentation Column - spans
        TreeTableColumn<PropertiesDAO,String> documentationCol = new TreeTableColumn<>( "Documentation" );
        // Description Column
        descCol = new TreeTableColumn<>( "Description" );
        setColumnProps( descCol, true, true, false, -150, "description" );
        // setColumnProps( descCol, true, true, false, 150, "description" );
        // Deprecation Column
        deprecatedCol = new TreeTableColumn<>( "Deprecation" );
        setColumnProps( deprecatedCol, true, true, false, 50, "deprecation" );
        documentationCol.getColumns().addAll( deprecatedCol, descCol );
        // Other
        // otherDocCol = new TreeTableColumn<>( "Other" );
        // setColumnProps( otherDocCol, false, false, false, 0 );
        // documentationCol.getColumns().addAll( descCol, deprecatedCol, otherDocCol );
        // setColumnProps( descCol, true, true, false, 0 );
        // setColumnProps( deprecatedCol, false, false, false, 0 );

        // Repeat: min and max Column
        minCol = new TreeTableColumn<>( "min" );
        setColumnProps( minCol, true, true, false, 75 );
        minCol.setCellValueFactory( new TreeItemPropertyValueFactory<PropertiesDAO,String>( "min" ) );
        minCol.setCellFactory( ChoiceBoxTreeTableCell.forTreeTableColumn( PropertiesDAO.minList() ) );

        maxCol = new TreeTableColumn<>( "max" );
        setColumnProps( minCol, true, true, false, 0 );
        // javafx.geometry.Pos.
        maxCol.setStyle( "-fx-alignment: CENTER;" );
        maxCol.setCellValueFactory( new TreeItemPropertyValueFactory<PropertiesDAO,Integer>( "max" ) );
        maxCol.setCellFactory( TextFieldTreeTableCell.forTreeTableColumn( new DexRepeatMaxConverter() ) );

        TreeTableColumn<PropertiesDAO,String> constraintCol = new TreeTableColumn<>( "Repeat" );
        constraintCol.getColumns().addAll( minCol, maxCol );

        // Validation column
        TreeTableColumn<PropertiesDAO,ImageView> valCol = new TreeTableColumn<>( "" );
        valCol.setPrefWidth( 25 );
        valCol.setEditable( false );
        valCol.setSortable( false );
        valCol.setCellFactory( c -> new ValidationPropertiesTreeTableCellFactory() );

        table.getColumns().addAll( nameCol, valCol, typeCol, roleCol, constraintCol, exampleCol, documentationCol );
        // Bind column width to table width
        bindWidth( table, descCol );
        // descCol.prefWidthProperty().bind( table.widthProperty().subtract( tableWidth( table, descCol ) ) );
    }

    protected void bindWidth(TreeTableView<PropertiesDAO> table, TreeTableColumn<PropertiesDAO,?> col) {
        double width = 0;
        for (TreeTableColumn<PropertiesDAO,?> c : table.getColumns())
            if (c != col && c.isVisible()) {
                width += c.widthProperty().doubleValue();
                // If column visibility changes, recompute width
                c.visibleProperty().addListener( new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVisibility,
                        Boolean newVisibility) {
                        bindWidth( table, col );
                    }
                } );
            }
        col.prefWidthProperty().bind( table.widthProperty().subtract( width ) );
    }

    @Override
    public void checkNodes() {
        if (propertiesTable == null)
            throw new IllegalStateException( "Property table not injected by FXML" );
    }

    /**
     * Remove all items from the table
     */
    @Override
    public void clear() {
        if (propertiesTable.getRoot().getChildren() != null)
            propertiesTable.getRoot().getChildren().clear();
        if (propertiesTable.getSelectionModel() != null)
            propertiesTable.getSelectionModel().clearSelection();
        postedData = null;
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = propertiesTable;

        propertiesTable.getSelectionModel().selectedItemProperty()
            .addListener( (v, old, newValue) -> propertySelectionListener( newValue ) );

        // Layout the table
        initializeTable( propertiesTable );

        // 3/26/2020 - doesn't really work ... only does the empty table node
        // To update, choose some node that's an ancestor of all your nodes which have the "my-view" style class (it
        // sounds like your tab pane will work, but in general you could just use the root node of the scene) and do
        // int fontSize = 18;
        // for (Node n : memberProperties.lookupAll( ".text" )) {
        // n.setStyle( "-fx-font-size: " + Integer.toString( fontSize ) + "pt;" );
        // log.debug( "Set font size on " + n );
        // }

    }

    private void disableEditing() {
        for (TreeTableColumn<PropertiesDAO,?> col : propertiesTable.getColumns())
            col.setEditable( false );
    }

    @Override
    public void handleEvent(AbstractOtmEvent e) {
        log.debug( "event handler: " + e.getClass().getSimpleName() );
        if (e instanceof DexMemberSelectionEvent)
            handleEvent( (DexMemberSelectionEvent) e );
        else if (e instanceof DexModelChangeEvent)
            handleEvent( (DexModelChangeEvent) e );
        else if (e instanceof OtmObjectChangeEvent)
            handleEvent( (OtmObjectChangeEvent) e );
        else if (e instanceof OtmObjectReplacedEvent)
            handleEvent( (OtmObjectReplacedEvent) e );
        else if (e instanceof OtmObjectModifiedEvent)
            handleEvent( (OtmObjectModifiedEvent) e );
        else if (e instanceof DexMemberDeleteEvent)
            handleEvent( (DexMemberDeleteEvent) e );
    }

    private void handleEvent(DexMemberDeleteEvent e) {
        if (e.get() == postedData)
            clear();
        if (e.getAlternateMember() != null)
            post( e.getAlternateMember() );
        else
            refresh();
    }

    public void handleEvent(DexModelChangeEvent event) {
        if (event.get() instanceof OtmLibraryMember)
            post( (OtmLibraryMember) event.get() );
        else
            clear();
    }

    private void handleEvent(OtmObjectChangeEvent e) {
        if (e.get() instanceof OtmLibraryMember)
            post( (OtmLibraryMember) e.get() );
        else
            refresh();
    }

    private void handleEvent(OtmObjectModifiedEvent e) {
        refresh();
    }

    private void handleEvent(OtmObjectReplacedEvent e) {
        if (e.getOrginalObject().getOwningMember() == e.getReplacementObject().getOwningMember())
            log.error( "BAD HERE" );
        OtmPropertyOwner parent = null;
        if (e.getOrginalObject() instanceof OtmProperty)
            parent = ((OtmProperty) e.getOrginalObject()).getParent();

        post( e.get().getOwningMember() );

        if (e.getOrginalObject() instanceof OtmProperty && parent != ((OtmProperty) e.getOrginalObject()).getParent())
            log.debug( "CHANGED PARENT. OOPS." );

        if (e.getOrginalObject().getOwningMember() == e.getReplacementObject().getOwningMember())
            log.error( "BAD HERE" );
    }

    public void handleMaxEdit(TreeTableColumn.CellEditEvent<PropertiesDAO,String> event) {
        if (event != null && event.getTreeTablePosition() != null) {
            TreeItem<PropertiesDAO> currentItem = event.getRowValue();
            if (currentItem != null)
                currentItem.getValue().setMax( event.getNewValue() );
        } else
            log.warn( "ERROR - cell max edit handler has null." );
    }

    public void handleEvent(DexMemberSelectionEvent event) {
        if (event != null)
            post( event.getMember() );
    }

    private void initializeTable(TreeTableView<PropertiesDAO> table) {
        // Set the hidden root item
        root = new TreeItem<>();
        root.setExpanded( true ); // Start out fully expanded
        // Set up the TreeTable
        table.setRoot( root );
        table.setShowRoot( false );
        table.setEditable( true );
        table.getSelectionModel().setCellSelectionEnabled( true ); // allow individual cells to be edited
        table.setTableMenuButtonVisible( true ); // allow users to select columns

        // Enable context menus at the row level and add change listener for for applying style
        table.setRowFactory( (TreeTableView<PropertiesDAO> p) -> new MemberPropertiesRowFactory( this ) );

        // Define Columns and cell content providers
        buildColumns( table );

    }

    @Override
    public void post(OtmLibraryMember member) {
        clear();
        postedData = member;
        if (member != null)
            new PropertiesDAO( member, this ).createChildrenItems( root, null );
    }

    /**
     * Set edit-ability of columns
     * 
     * A note about selection: A TreeTableCell visually shows it is selected when two conditions are met: 1.The
     * TableSelectionModel.isSelected(int, TableColumnBase) method returns true for the row / column that this cell
     * represents, and 2.The cell selection mode property is set to true (to represent that it is allowable to select
     * individual cells (and not just rows of cells)).
     * 
     * @param item
     */
    private void propertySelectionListener(TreeItem<PropertiesDAO> item) {
        if (item == null || item.getValue() == null || item.getValue().getValue() == null)
            return;
        OtmObject obj = null;
        DexActionManager actionManager = null;
        if (item.getValue().getValue() instanceof OtmObject) {
            obj = item.getValue().getValue();
            actionManager = obj.getActionManager();
        }
        log.debug( "Selection: " + obj + " " + obj.getClass().getSimpleName() );
        if (actionManager != null) {
            nameCol.setEditable( actionManager.isEnabled( DexActions.NAMECHANGE, obj ) );
            descCol.setEditable( actionManager.isEnabled( DexActions.DESCRIPTIONCHANGE, obj ) );
            typeCol.setEditable( actionManager.isEnabled( DexActions.TYPECHANGE, obj ) );
            roleCol.setEditable( actionManager.isEnabled( DexActions.PROPERTYROLECHANGE, obj ) );
            minCol.setEditable( actionManager.isEnabled( DexActions.MANDITORYCHANGE, obj ) );
            maxCol.setEditable( actionManager.isEnabled( DexActions.SETREPEATCOUNT, obj ) );
            exampleCol.setEditable( actionManager.isEnabled( DexActions.EXAMPLECHANGE, obj ) );
            deprecatedCol.setEditable( actionManager.isEnabled( DexActions.DEPRECATIONCHANGE, obj ) );
        } else {
            disableEditing();
        }
        if (obj instanceof OtmProperty)
            fireEvent( new DexPropertySelectionEvent( (OtmProperty) obj ) );
        if (obj instanceof OtmFacet || obj instanceof OtmContextualFacet)
            fireEvent( new DexFacetSelectionEvent( obj ) );
        // postObjectStatus( obj );
    }

    @Override
    public void refresh() {
        post( postedData );
    }

    /**
     * Set String column properties and set value to named field.
     */
    private void setColumnProps(TreeTableColumn<PropertiesDAO,String> c, boolean visable, boolean editable,
        boolean sortable, int width, String field) {
        setColumnProps( c, visable, editable, sortable, width );
        c.setCellValueFactory( new TreeItemPropertyValueFactory<PropertiesDAO,String>( field ) );
        c.setCellFactory( TextFieldTreeTableCell.forTreeTableColumn() );
    }

}
