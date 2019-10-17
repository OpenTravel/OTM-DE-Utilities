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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;

import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
public final class MemberPropertiesRowFactory extends TreeTableRow<PropertiesDAO> {
    private static Log log = LogFactory.getLog( MemberPropertiesRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass INHERITED = PseudoClass.getPseudoClass( "inherited" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );
    private MemberPropertiesTreeTableController controller;

    private final ContextMenu contextMenu = new ContextMenu();
    private Menu addMenu;
    private MenuItem deleteProperty;
    private MenuItem changeType;
    MenuItem validateItem = null;

    // Constructor does not have access to content, just the empty row
    public MemberPropertiesRowFactory(MemberPropertiesTreeTableController controller) {
        this.controller = controller;

        // Create Context menu
        addMenu = new Menu( "Add" );
        setupAddMenu( addMenu );
        deleteProperty = new MenuItem( "Delete" );
        changeType = new MenuItem( "Change Assigned Type" );
        validateItem = new MenuItem( "Validate" );

        // MenuItem upObject = new MenuItem( "Move Up (Future)" );
        // MenuItem downObject = new MenuItem( "Move Down (Future)" );
        // SeparatorMenuItem separator = new SeparatorMenuItem();
        // contextMenu.getItems().addAll( addMenu, deleteProperty, changeType, separator, upObject, downObject );
        contextMenu.getItems().addAll( addMenu, deleteProperty, changeType, validateItem );
        setContextMenu( contextMenu );

        changeType.setOnAction( e -> changeAssignedType() );
        deleteProperty.setOnAction( e -> deleteProperty() );
        validateItem.setOnAction( e -> validateMember() );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    private void setupAddMenu(Menu menu) {
        MenuItem item;
        // FIXME - for business and choice objects, add items for facets and separator
        // Consider - should the list of values be from the action? The facets will not be propertyTypes!
        for (OtmPropertyType type : OtmPropertyType.values()) {
            item = new MenuItem( type.label() );
            item.setOnAction( e -> addProperty( type ) );
            menu.getItems().add( item );
        }
    }

    /**
     * Add a new member to the tree
     * 
     * @param t
     */
    private void addProperty(OtmPropertyType type) {
        OtmPropertyOwner owner = getPropertyOwner();
        // Get a valid subject
        if (owner != null) {
            owner.getActionManager().run( DexActions.NEWPROPERTY, owner, type );
        }
        log.debug( "Test - add in Properties Row Factory." );
        controller.refresh();
    }

    private void deleteProperty() {
        OtmProperty p = getProperty();
        if (p != null) {
            p.getActionManager().run( DexActions.DELETEPROPERTY, p );
        }
        controller.refresh();
    }

    private void validateMember() {
        OtmObject obj = getProperty();
        if (obj != null) {
            obj.isValid( true );
            if (obj.getOwningMember() != null)
                obj.getOwningMember().isValid( true );
            log.debug( "Validate " + obj + " finding count: " + obj.getFindings().count() );
        }
        controller.refresh();

    }

    private OtmProperty getProperty() {
        PropertiesDAO dao = null;
        if (getTreeItem() != null)
            dao = getTreeItem().getValue();
        if (dao != null && dao.getValue() != null && dao.getValue() instanceof OtmProperty)
            return ((OtmProperty) dao.getValue());
        return null;
    }

    private OtmPropertyOwner getPropertyOwner() {
        PropertiesDAO dao = null;
        if (getTreeItem() != null)
            dao = getTreeItem().getValue();
        if (dao != null && dao.getValue() != null) {
            if (dao.getValue() instanceof OtmProperty)
                return ((OtmProperty) dao.getValue()).getParent();
            if (dao.getValue() instanceof OtmPropertyOwner) {
                return (OtmPropertyOwner) dao.getValue();
            }
        }
        return null;
    }

    // Runs if menu item on a row is selected
    private void changeAssignedType() {
        TreeItem<PropertiesDAO> treeItem = getTreeItem();
        if (treeItem != null && treeItem.getValue() != null && treeItem.getValue().getValue() instanceof OtmTypeUser) {
            OtmTypeUser user = (OtmTypeUser) treeItem.getValue().getValue();
            user.getActionManager().run( DexActions.TYPECHANGE, user );
        }
        controller.getMainController().refresh();
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     */
    private void setCSSClass(TreeTableRow<PropertiesDAO> tc, TreeItem<PropertiesDAO> newTreeItem) {
        if (newTreeItem != null && newTreeItem.getValue() != null) {

            OtmProperty property = getProperty();
            OtmPropertyOwner propertyOwner = getPropertyOwner();
            if (propertyOwner != null && propertyOwner.getActionManager() != null) {
                DexActionManager am = propertyOwner.getActionManager();
                addMenu.setDisable( !am.isEnabled( DexActions.NEWPROPERTY, propertyOwner ) );
                deleteProperty.setDisable( !am.isEnabled( DexActions.DELETEPROPERTY, property ) );
                changeType.setDisable( !am.isEnabled( DexActions.TYPECHANGE, property ) );
            }
            if (newTreeItem.getValue().getValue() instanceof OtmChildrenOwner) {
                // Make facets dividers
                tc.pseudoClassStateChanged( DIVIDER, true );
                tc.setEditable( false );
            } else {
                // Set Editable style and state
                tc.pseudoClassStateChanged( DIVIDER, false );
                tc.pseudoClassStateChanged( INHERITED, newTreeItem.getValue().isInherited() );
                tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().isEditable() );
                tc.setEditable( newTreeItem.getValue().isEditable() );
            }
        }
    }
}
