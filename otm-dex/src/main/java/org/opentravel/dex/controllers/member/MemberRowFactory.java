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
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;

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
    MenuItem deleteItem = null;
    MenuItem validateItem = null;

    public MemberRowFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu
        deleteItem = addItem( memberMenu, "Delete", e -> deleteMember() );
        newMenu = new Menu( "New" );
        // Create sub-menu for new objects
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values())
            addItem( newMenu, type.label(), e -> newMember( type ) );
        memberMenu.getItems().add( newMenu );
        validateItem = addItem( memberMenu, "Validate", e -> validateMember() );
        setContextMenu( memberMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        // // Not sure this helps!
        // if (getTreeItem() != null && getTreeItem().getValue() != null) {
        // setEditable( getTreeItem().getValue().isEditable() );
        // }
    }

    // TODO - create utils class with statics for row factories
    private MenuItem addItem(Menu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private MenuItem addItem(ContextMenu menu, String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem( label );
        menu.getItems().add( item );
        item.setOnAction( handler );
        return item;
    }

    private void deleteMember() {
        OtmObject obj = getValue();
        if (obj instanceof OtmLibraryMember)
            obj.getActionManager().run( DexActions.DELETELIBRARYMEMBER, (OtmLibraryMember) obj );
        super.updateTreeItem( getTreeItem().getParent() );
    }

    private void validateMember() {
        OtmObject obj = getValue();
        if (obj instanceof OtmObject) {
            obj.isValid( true );
            if (obj.getFindings() != null)
                log.debug( "Validate " + obj + " finding count: " + obj.getFindings().count() );
        }
        controller.refresh();
    }

    private void newMember(OtmLibraryMemberType type) {
        OtmObject obj = getValue();
        // If they didn't select anything
        if (obj == null && controller.getMainController() != null)
            // Use any member the model manager delivers
            for (OtmLibraryMember member : controller.getMainController().getModelManager().getMembers()) {
            obj = member;
            break;
            }
        // Run action
        if (obj != null)
            obj.getModelManager().getActionManager( true ).run( DexActions.NEWLIBRARYMEMBER, obj, type );
        controller.refresh();
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
        if (newTreeItem != null && newTreeItem.getValue() instanceof MemberAndProvidersDAO) {
            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().isEditable() );

            deleteItem.setDisable( !newTreeItem.getValue().isEditable() );
            // newMenu.setDisable( !newTreeItem.getValue().isEditable() );
        }
    }
    // TODO - investigate using ControlsFX for decoration
    // TODO - Dragboard db = r.startDragAndDrop(TransferMode.MOVE);
    // https://www.programcreek.com/java-api-examples/index.php?api=javafx.scene.control.TreeTableRow
}
