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
import javafx.scene.control.ContextMenu;
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
    private DexIncludedController<?> controller;

    private final ContextMenu providerMenu = new ContextMenu();
    // MenuItem updateProviderVersion = new MenuItem( "Update to Latest Version (In-Development)" );

    public UserCellFactory(DexIncludedController<?> controller) {
        this.controller = controller;

        // Create Context menu
        // updateProviderVersion.setOnAction( e -> updateProvider( e ) );
        // providerMenu.getItems().add( updateProviderVersion );
        // setContextMenu( providerMenu );

        // Set style listener (css class)
        // treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    // private void updateProvider(ActionEvent e) {
    // OtmTypeProvider provider = getSelectedProvider();
    // OtmLibraryMember member = getTreeItem().getValue().getUsingMember();
    // if (provider != null && member != null) {
    // log.debug( "todo - update " + member + " to version update " + provider );
    // provider.getActionManager().run( DexActions.VERSIONUPDATE, provider, member );
    // }
    // // Object x = e.getSource();
    // // OtmObject obj = getValue();
    // // DexDAO<?> dao = controller.getSelection();
    // // Object value = dao.getValue();
    // // Object p = getSelectedProvider();
    // // // TODO
    // // log.debug(
    // // "Update to later version: provider = " + obj + " owner is: " + controller.getSelection().getValue() );
    // }

    // private OtmTypeProvider getSelectedProvider() {
    // Object v = controller.getSelection().getValue();
    // return v instanceof OtmTypeProvider ? (OtmTypeProvider) v : null;
    // }

    // /**
    // * @return the value OtmObject or null
    // */
    // private OtmObject getValue() {
    // return getSelectedObject( getTreeItem() );
    // }

    // private OtmObject getSelectedObject(TreeItem<MemberAndProvidersDAO> item) {
    // if (item != null && item.getValue() != null && item.getValue().getValue() instanceof OtmObject)
    // return (item.getValue().getValue());
    // return null;
    // }

    // /**
    // * Get the library member from the DAO if and only if it is editable
    // *
    // * @param item
    // * @return library member or null if missing or not editable
    // */
    // private OtmLibraryMember getEditableMember(TreeItem<MemberAndProvidersDAO> item) {
    // if (item != null && item.getValue() != null && item.getValue().getUsingMember() instanceof OtmLibraryMember)
    // return item.getValue().getUsingMember().isEditable() ? item.getValue().getUsingMember() : null;
    // return null;
    // }

    // /**
    // * @param tc
    // * @param newTreeItem
    // * @return
    // * @return
    // */
    // private void setCSSClass(TreeCell<MemberAndProvidersDAO> tc, TreeItem<MemberAndProvidersDAO> newTreeItem) {
    // updateProviderVersion.setDisable( true );
    // if (getEditableMember( newTreeItem ) != null && UpdateToLaterVersionAction.isEnabled( getSelectedProvider() ))
    // updateProviderVersion.setDisable( false );
    // }

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
                boolean editable = item.getValue() != null && item.getValue().isEditable();
                pseudoClassStateChanged( EDITABLE, item.getValue() != null && item.getValue().isEditable() );
                pseudoClassStateChanged( DIVIDER,
                    item.getValue() != null && item.getValue() instanceof OtmNamespaceFacet );
            }
        }
    }


}
