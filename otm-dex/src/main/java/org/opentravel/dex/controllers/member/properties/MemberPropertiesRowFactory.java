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
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmTypeUser;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
/**
 * TreeTableRow is an IndexedCell, but rarely needs to be used by developers creating TreeTableView instances. The only
 * time TreeTableRow is likely to be encountered at all by a developer is if they wish to create a custom rowFactory
 * that replaces an entire row of a TreeTableView.
 * 
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeTableRow.html
 */
public final class MemberPropertiesRowFactory extends TreeTableRow<PropertiesDAO> {
    private static Log log = LogFactory.getLog( MemberPropertiesRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass INHERITED = PseudoClass.getPseudoClass( "inherited" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );
    private final ContextMenu addMenu = new ContextMenu();
    private MemberPropertiesTreeTableController controller;

    // Constructor does not have access to content, just the empty row
    public MemberPropertiesRowFactory(MemberPropertiesTreeTableController controller) {
        this.controller = controller;

        // Create Context menu
        MenuItem addObject = new MenuItem( "Add Property (Demo)" );
        MenuItem changeType = new MenuItem( "Change Assigned Type" );
        MenuItem upObject = new MenuItem( "Move Up (Future)" );
        MenuItem downObject = new MenuItem( "Move Down (Future)" );
        SeparatorMenuItem separator = new SeparatorMenuItem();
        addMenu.getItems().addAll( addObject, changeType, separator, upObject, downObject );
        setContextMenu( addMenu );

        // Create action for addObject event
        addObject.setOnAction( this::addMemberEvent );
        changeType.setOnAction( e -> changeAssignedTypeListener() );

        // // Set editable style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        // treeItemProperty().getValue() is always null!
        // getItem() is always null!

        // log.debug("");
    }

    /**
     * Add a new member to the tree
     * 
     * @param t
     */
    private void addMemberEvent(ActionEvent t) {
        log.debug( "TODO - implement add member event in Properties Row Factory." );
        // TreeItem<OtmTreeTableNode> item = createTreeItem(new OtmCoreObject("new"), getTreeItem().getParent());
        // super.updateTreeItem(item); // needed to apply stylesheet to new item

        // TreeItem<PropertiesDAO> treeItem = getTreeItem();
        // if (treeItem != null) {
        // OtmObject otm = treeItem.getValue().getValue();
        // }
    }

    // Runs if menu item on a row is selected
    private void changeAssignedTypeListener() {
        TreeItem<PropertiesDAO> treeItem = getTreeItem();
        if (treeItem != null && treeItem.getValue() != null && treeItem.getValue().getValue() instanceof OtmTypeUser) {
            OtmTypeUser user = (OtmTypeUser) treeItem.getValue().getValue();
            user.getActionManager().run( DexActions.TYPECHANGE, user, null );
        }
        controller.getMainController().refresh();
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     */
    // TODO - use style class for warning and error
    private void setCSSClass(TreeTableRow<PropertiesDAO> tc, TreeItem<PropertiesDAO> newTreeItem) {
        if (newTreeItem != null && newTreeItem.getValue() != null) {
            // Disable context menu items
            // TODO - leave add property enabled
            getContextMenu().getItems().forEach( i -> i.setDisable( !newTreeItem.getValue().isEditable() ) );

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
                // parentDAO = newTreeItem.getParent().getValue();
            }
        }
    }
    // TODO - investigate using ControlsFX for decoration
    // TODO - Dragboard db = r.startDragAndDrop(TransferMode.MOVE);
    // https://www.programcreek.com/java-api-examples/index.php?api=javafx.scene.control.TreeTableRow

    // startEdit, commitEdit, cancelEdit do not run on row

    // Runs often, but no access to cells in the row to act upon them
    // @Override
    // public void updateItem(OtmTreeTableNode item, boolean empty) {
    // super.updateItem(item, empty);
    // }
}
