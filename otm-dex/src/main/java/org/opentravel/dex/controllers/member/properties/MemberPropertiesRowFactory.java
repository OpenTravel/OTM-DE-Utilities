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
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmFacets.OtmAlias;
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
    private MenuItem deleteItem;
    private MenuItem changeType;
    MenuItem validateItem = null;

    // Constructor does not have access to content, just the empty row
    public MemberPropertiesRowFactory(MemberPropertiesTreeTableController controller) {
        this.controller = controller;

        // Create Context menu
        addMenu = new Menu( "Add" );
        setupAddMenu( addMenu );
        deleteItem = new MenuItem( "Delete" );
        changeType = new MenuItem( "Change Assigned Type" );
        validateItem = new MenuItem( "Validate" );

        // MenuItem upObject = new MenuItem( "Move Up (Future)" );
        // MenuItem downObject = new MenuItem( "Move Down (Future)" );
        // SeparatorMenuItem separator = new SeparatorMenuItem();
        // contextMenu.getItems().addAll( addMenu, deleteProperty, changeType, separator, upObject, downObject );

        contextMenu.getItems().addAll( addMenu, deleteItem, changeType, validateItem );
        setContextMenu( contextMenu );

        changeType.setOnAction( e -> changeAssignedType() );
        deleteItem.setOnAction( e -> delete() );
        validateItem.setOnAction( e -> validateMember() );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    /**
     * Add a new member to the tree
     * 
     * @param t
     */
    private void addProperty(OtmPropertyType type) {
        // Get a valid owner as the subject
        OtmPropertyOwner owner = getPropertyOwner( getObject() );
        if (owner != null) {
            owner.getActionManager().run( DexActions.ADDPROPERTY, owner, type );
            controller.refresh();
        }
        // log.debug( "add in Properties Row Factory." );
    }

    private void changeAssignedType() {
        OtmObject obj = getObject();
        if (obj instanceof OtmTypeUser) {
            obj.getActionManager().run( DexActions.TYPECHANGE, (OtmTypeUser) obj );
            controller.getMainController().refresh();
        }
    }

    private void delete() {
        OtmObject object = getObject();
        if (object instanceof OtmProperty)
            delete( (OtmProperty) object );
        if (object instanceof OtmAlias)
            delete( (OtmAlias) object );
        controller.refresh();
    }

    private void delete(OtmProperty p) {
        p.getActionManager().run( DexActions.DELETEPROPERTY, p );
    }

    private void delete(OtmAlias a) {
        a.getActionManager().run( DexActions.DELETEALIAS, a );
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmObject getObject() {
        PropertiesDAO dao = null;
        if (getTreeItem() != null)
            dao = getTreeItem().getValue();
        if (dao != null && dao.getValue() instanceof OtmObject)
            return dao.getValue();
        return null;
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmProperty getProperty(OtmObject object) {
        if (object instanceof OtmProperty)
            return ((OtmProperty) object);
        return null;
    }

    /**
     * @return treeItem.getValue().getValue() or null
     */
    private OtmAlias getAlias(OtmObject object) {
        if (object instanceof OtmAlias)
            return ((OtmAlias) object);
        return null;
    }

    private OtmPropertyOwner getPropertyOwner(OtmObject object) {
        if (object instanceof OtmProperty)
            return ((OtmProperty) object).getParent();
        if (object instanceof OtmAbstractDisplayFacet)
            return ((OtmAbstractDisplayFacet) object).getParent();
        if (object instanceof OtmPropertyOwner)
            return (OtmPropertyOwner) object;
        // if (object instanceof OtmAlias)
        // return ((OtmAlias)object).getOwningMember();
        return null;

    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     */
    private void setCSSClass(TreeTableRow<PropertiesDAO> tc, TreeItem<PropertiesDAO> newTreeItem) {
        OtmObject object = getObject();
        if (object == null)
            return;
        DexActionManager am = object.getActionManager();

        addMenu.setDisable( true );
        changeType.setDisable( true );
        deleteItem.setDisable( true );
        // Turn on menu items
        if (am != null) {
            if (object instanceof OtmAlias) {
                // log.debug( "Alias = " + getAlias( object ) + am.getClass().getSimpleName() );
                deleteItem.setDisable( !am.isEnabled( DexActions.DELETEALIAS, getAlias( object ) ) );
            } else {
                OtmProperty property = getProperty( object );
                OtmPropertyOwner propertyOwner = getPropertyOwner( object );

                addMenu.setDisable( !am.isEnabled( DexActions.ADDPROPERTY, propertyOwner ) );
                changeType.setDisable( !am.isEnabled( DexActions.TYPECHANGE, property ) );
                deleteItem.setDisable( !am.isEnabled( DexActions.DELETEPROPERTY, property ) );
                // enable/disable each property type menu item
                OtmPropertyType.enableMenuItems( addMenu, propertyOwner );
            }
        }

        // 3/26/2020 - WORKS
        // Set style
        // int fontSize = 18;
        // for (Node n : tc.lookupAll( ".text" )) {
        // n.setStyle( "-fx-font-size: " + Integer.toString( fontSize ) + "pt;" );
        // log.debug( "Set font size on " + n );
        // }
        tc.setEditable( object.isEditable() );
        tc.pseudoClassStateChanged( INHERITED, newTreeItem.getValue().isInherited() );
        tc.pseudoClassStateChanged( EDITABLE, object.isEditable() );
        if (object instanceof OtmChildrenOwner) {
            // Make facets dividers, not aliases
            tc.pseudoClassStateChanged( DIVIDER, !(object instanceof OtmAlias) );
        } else {
            tc.pseudoClassStateChanged( DIVIDER, false );
        }
    }

    private void setupAddMenu(Menu menu) {
        for (MenuItem item : OtmPropertyType.menuItems()) {
            if (item.getUserData() instanceof OtmPropertyType)
                item.setOnAction( e -> addProperty( (OtmPropertyType) item.getUserData() ) );
            menu.getItems().add( item );
        }
    }

    private void validateMember() {
        OtmObject obj = getProperty( getObject() );
        if (obj != null) {
            obj.isValid( true );
            if (obj.getOwningMember() != null)
                obj.getOwningMember().isValid( true );
            controller.refresh();
            // log.debug( "Validate " + obj + " finding count: " + obj.getFindings().count() );
        }
    }

}
