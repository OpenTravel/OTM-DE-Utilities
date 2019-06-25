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

package org.opentravel.dex.controllers.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.UnlockLibraryDialogContoller;
import org.opentravel.dex.repository.RepositoryResultHandler;
import org.opentravel.dex.tasks.repository.LockLibraryTask;
import org.opentravel.dex.tasks.repository.UnlockLibraryTask;
import org.opentravel.model.otmContainers.OtmLibrary;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

/**
 * @author dmh
 *
 */
public final class LibraryRowFactory extends TreeTableRow<LibraryDAO> {
    private static Log log = LogFactory.getLog( LibraryRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );

    private LibrariesTreeTableController controller;
    private DexMainController mainController;

    private final ContextMenu contextMenu = new ContextMenu();
    private MenuItem lockLibrary;
    private MenuItem unlockLibrary;
    private MenuItem whereUsed;

    // controller injected from FXML
    public LibraryRowFactory(LibrariesTreeTableController controller) {
        this();
        // this.controller = controller;
        this.controller = controller;
        mainController = controller.getMainController();
    }

    public LibraryRowFactory() {

        // Create Context menu
        lockLibrary = new MenuItem( "Lock" );
        unlockLibrary = new MenuItem( "Unlock" );
        whereUsed = new MenuItem( "Show Where Used (future)" );

        lockLibrary.setOnAction( e -> lockLibrary() );
        unlockLibrary.setOnAction( (e) -> unlockLibraryEventHandler() );
        whereUsed.setOnAction( this::addMemberEvent );

        contextMenu.getItems().addAll( lockLibrary, unlockLibrary, whereUsed );
        setContextMenu( contextMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    /**
     * Add a new member to the tree
     * 
     */
    private void lockLibrary() {
        log.debug( "Lock in Row Factory.  " + controller.getSelectedItem().getValue().hashCode() );
        if (controller.getSelectedItem() != null && controller.getSelectedItem().getValue() != null)
            new LockLibraryTask( controller.getSelectedItem().getValue(), new RepositoryResultHandler( mainController ),
                mainController.getStatusController(), controller, mainController.getModelManager() ).go();
    }

    private void unlockLibraryEventHandler() {
        log.debug( "Unlock in Row Factory.   " + controller.getSelectedItem().getValue().getClass().hashCode() );
        UnlockLibraryDialogContoller uldc = UnlockLibraryDialogContoller.init();
        uldc.showAndWait( "" );
        boolean commitWIP = uldc.getCommitState();
        String remarks = uldc.getCommitRemarks();

        new UnlockLibraryTask( controller.getSelectedItem().getValue(), commitWIP, remarks,
            new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
    }

    /**
     * Add a new member to the tree
     * 
     * @param t
     */
    private void addMemberEvent(ActionEvent t) {
        // Works - but business logic is wrong.
        // TreeItem<LibraryMemberTreeDAO> item = controller
        // .createTreeItem(new OtmCoreObject("new", controller.getModelManager()), getTreeItem().getParent());
        // super.updateTreeItem(item); // needed to apply stylesheet to new item
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     * @return
     */
    // TODO - use style class for warning and error
    private void setCSSClass(TreeTableRow<LibraryDAO> tc, TreeItem<LibraryDAO> newTreeItem) {
        if (newTreeItem != null && newTreeItem.getValue() != null) {
            OtmLibrary library = newTreeItem.getValue().getValue();
            if (library != null) {
                lockLibrary.setDisable( !library.canBeLocked() );
                unlockLibrary.setDisable( !library.canBeUnlocked() );
                whereUsed.setDisable( true );
            }
            // return n.getLibrary().getProjectItem().getState().equals(RepositoryItemState.MANAGED_WIP);

            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().getValue().isEditable() );
        }
    }
}
