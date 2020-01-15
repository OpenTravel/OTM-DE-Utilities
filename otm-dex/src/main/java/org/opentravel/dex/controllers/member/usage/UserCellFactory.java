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
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.otmFacets.OtmNamespaceFacet;

import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class UserCellFactory extends TreeCell<MemberAndUsersDAO> {
    private static Log log = LogFactory.getLog( UserCellFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );
    // private DexIncludedController<?> controller;

    // private final ContextMenu providerMenu = new ContextMenu();
    // MenuItem updateProviderVersion = new MenuItem( "Update to Latest Version (In-Development)" );

    public UserCellFactory(DexIncludedController<?> controller) {
        // this.controller = controller;
    }


    @Override
    public void updateItem(MemberAndUsersDAO item, boolean empty) {
        super.updateItem( item, empty );
        if (empty) {
            setText( null );
            setGraphic( null );
            pseudoClassStateChanged( DIVIDER, false );
        } else {
            if (isEditing()) {
                // if (textField != null) {
                // textField.setText(getString());
                // }
                setText( null );
                // setGraphic(textField);
            } else {
                setText( item.getValue().getName() );
                ImageView graphic = ImageManager.get( item.getValue() );
                setGraphic( graphic );
                // if (!getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
                // setContextMenu( providerMenu );
                // }
                pseudoClassStateChanged( EDITABLE, item.getValue() != null && item.getValue().isEditable() );
                pseudoClassStateChanged( DIVIDER, item.getValue() instanceof OtmNamespaceFacet );
            }
        }
    }


}
