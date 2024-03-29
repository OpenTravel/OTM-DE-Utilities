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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.resource.OtmAction;

import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class ActionRowFactory extends TreeTableRow<ActionsDAO> {
    private static Logger log = LogManager.getLogger( ActionRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass INHERITED = PseudoClass.getPseudoClass( "inherited" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );

    private final ContextMenu addMenu = new ContextMenu();
    private DexIncludedController<?> controller;

    public ActionRowFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu
        // MenuItem addObject = new MenuItem( "Add Object (Future)" );
        // addMenu.getItems().add( addObject );
        // setContextMenu( addMenu );

        // Create action for addObject event
        // addObject.setOnAction( this::addMemberEvent );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );

        // // Not sure this helps!
        // if (getTreeItem() != null && getTreeItem().getValue() != null) {
        // setEditable( getTreeItem().getValue().isEditable() );
        // }
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     * @return
     */
    private void setCSSClass(TreeTableRow<ActionsDAO> tc, TreeItem<ActionsDAO> newTreeItem) {
        if (newTreeItem != null && newTreeItem.getValue() != null) {
            // tc.pseudoClassStateChanged( EDITABLE, false );
            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().isEditable() );

            // Make Actions dividers
            tc.pseudoClassStateChanged( DIVIDER, newTreeItem.getValue().getValue() instanceof OtmAction );

            // Ask the DAO if it is inherited
            tc.pseudoClassStateChanged( INHERITED, newTreeItem.getValue().isInherited() );
        }
    }
}
