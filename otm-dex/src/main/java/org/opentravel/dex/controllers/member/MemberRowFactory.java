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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.LibraryMemberType;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmObject;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class MemberRowFactory extends TreeTableRow<MemberAndProvidersDAO> {
    private static Log log = LogFactory.getLog( MemberRowFactory.class );

    private final ContextMenu memberMenu = new ContextMenu();
    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private DexIncludedController<?> controller;

    Menu newMenu = null;

    public MemberRowFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu
        // MenuItem addObject = new MenuItem( "Add Object " );

        // Create action for addObject event
        // addObject.setOnAction( this::addMemberEvent );
        newMenu = new Menu( "New" );
        for (LibraryMemberType type : LibraryMemberType.values())
            addItem( newMenu, type.label(), e -> newMember( type ) );

        memberMenu.getItems().add( newMenu );
        setContextMenu( memberMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        // Not sure this helps!
        if (getTreeItem() != null && getTreeItem().getValue() != null) {
            setEditable( getTreeItem().getValue().isEditable() );
        }
    }

    // TODO - create utils class with statics for row factories
    private MenuItem addItem(Menu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    // /**
    // * Add a new member to the tree
    // *
    // * @param t
    // */
    // private void addMemberEvent(ActionEvent t) {
    // // Works - but business logic is wrong.
    // // TreeItem<LibraryMemberTreeDAO> item = controller
    // // .createTreeItem(new OtmCoreObject("new", controller.getModelManager()), getTreeItem().getParent());
    // // super.updateTreeItem(item); // needed to apply stylesheet to new item
    // }
    private void newMember(LibraryMemberType type) {
        OtmObject obj = getValue();
        Object result = null;
        // Run action
        if (obj != null)
            result = obj.getModelManager().getActionManager( true ).run( DexActions.NEWLIBRARYMEMBER, obj, type );
        // result = obj.getActionManager().run( DexActions.NEWLIBRARYMEMBER, obj, type );
        // Update display
        if (result instanceof OtmObject) {
            TreeItem<MemberAndProvidersDAO> item =
                new MemberAndProvidersDAO( (OtmObject) result ).createTreeItem( getTreeItem().getParent() );
            super.updateTreeItem( item );
        }
    }

    /**
     * @return the value OtmObject or null
     */
    private OtmObject getValue() {
        if (getTreeItem() != null && getTreeItem().getValue() != null
            && getTreeItem().getValue().getValue() instanceof OtmObject)
            return getTreeItem().getValue().getValue();
        return null;
    }


    /**
     * @param tc
     * @param newTreeItem
     * @return
     * @return
     */
    // TODO - use style class for warning and error
    private void setCSSClass(TreeTableRow<MemberAndProvidersDAO> tc, TreeItem<MemberAndProvidersDAO> newTreeItem) {
        if (newTreeItem != null) {
            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().isEditable() );

            // newMenu.setDisable( !newTreeItem.getValue().isEditable() );
        }
    }
    // TODO - investigate using ControlsFX for decoration
    // TODO - Dragboard db = r.startDragAndDrop(TransferMode.MOVE);
    // https://www.programcreek.com/java-api-examples/index.php?api=javafx.scene.control.TreeTableRow
}
