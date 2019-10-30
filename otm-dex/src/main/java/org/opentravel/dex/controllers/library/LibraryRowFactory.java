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
import org.opentravel.common.DexProjectHandler;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.popup.UnlockLibraryDialogContoller;
import org.opentravel.dex.repository.RepositoryResultHandler;
import org.opentravel.dex.tasks.repository.LockLibraryTask;
import org.opentravel.dex.tasks.repository.PromoteLibraryTask;
import org.opentravel.dex.tasks.repository.UnlockLibraryTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
    private DexStatusController statusController = null;
    private OtmModelManager modelManager = null;

    private final ContextMenu contextMenu = new ContextMenu();
    private MenuItem lockLibrary;
    private MenuItem unlockLibrary;
    private MenuItem projectAdd;
    private MenuItem projectRemove;
    private MenuItem saveLibrary;
    private MenuItem manage;

    private Menu versionMenu;
    private MenuItem major = new MenuItem( "Major" );
    private MenuItem minor = new MenuItem( "Minor" );
    private MenuItem patch = new MenuItem( "Patch" );

    private Menu promote;
    private MenuItem underReview = new MenuItem( "Under Review" );
    private MenuItem finalState = new MenuItem( "Final" );
    private MenuItem obsolete = new MenuItem( "Make Obsolete" );;

    public LibraryRowFactory() {
        // Create Context menu
        lockLibrary = new MenuItem( "Lock" );
        unlockLibrary = new MenuItem( "Unlock" );
        projectAdd = new MenuItem( "Add to project" );
        projectRemove = new MenuItem( "Remove from project" );
        saveLibrary = new MenuItem( "Save" );

        manage = new MenuItem( "Manage - future" );
        promote = new Menu( "Promote" );
        promote.getItems().addAll( underReview, finalState, obsolete );

        versionMenu = new Menu( "Version - future" );
        versionMenu.getItems().addAll( major, minor, patch );

        lockLibrary.setOnAction( e -> lockLibrary() );
        unlockLibrary.setOnAction( e -> unlockLibrary() );
        projectAdd.setOnAction( this::addToProject );
        projectRemove.setOnAction( this::removeLibrary );
        saveLibrary.setOnAction( e -> saveLibrary() );
        promote.setOnAction( this::promoteLibrary );

        contextMenu.getItems().addAll( saveLibrary, new SeparatorMenuItem(), lockLibrary, unlockLibrary );
        contextMenu.getItems().addAll( new SeparatorMenuItem(), projectAdd, projectRemove );
        contextMenu.getItems().addAll( new SeparatorMenuItem(), manage, promote, versionMenu );
        setContextMenu( contextMenu );

        // Set style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    // controller injected from FXML
    public LibraryRowFactory(LibrariesTreeTableController controller) {
        this();
        this.controller = controller;
        mainController = controller.getMainController();
        if (mainController != null) {
            statusController = mainController.getStatusController();
            modelManager = mainController.getModelManager();
        }
    }

    private void addToProject(ActionEvent e) {
        OtmLibrary library = null;
        if (controller.getSelectedItem() != null && controller.getSelectedItem().getValue() != null)
            library = controller.getSelectedItem().getValue();
        if (library == null)
            return;
        new DexProjectHandler().addToProject( library );
        controller.refresh();
    }

    private OtmLibrary getSelected() {
        OtmLibrary library = null;
        if (controller.getSelectedItem() != null && controller.getSelectedItem().getValue() != null)
            library = controller.getSelectedItem().getValue();
        return library;
    }

    /**
     * Add a new member to the tree
     * 
     */
    private void lockLibrary() {
        log.debug( "Lock in Row Factory.  " + controller.getSelectedItem().getValue().hashCode() );
        if (controller.getSelectedItem() != null && controller.getSelectedItem().getValue() != null)
            new LockLibraryTask( controller.getSelectedItem().getValue(), new RepositoryResultHandler( mainController ),
                statusController, controller, modelManager ).go();
    }

    /**
     * Add a new member to the tree
     * 
     */
    private void promoteLibrary(ActionEvent e) {
        OtmLibrary lib = getSelected();
        log.debug( "Promote " + lib + " in Row Factory.  " + controller.getSelectedItem().getValue().hashCode() );

        e.getSource();
        if (e.getTarget() == finalState || e.getTarget() == obsolete || e.getTarget() == underReview) {
            if (lib != null)
                log.debug( "Set to final" );
            new PromoteLibraryTask( lib, new RepositoryResultHandler( mainController ), statusController, controller,
                modelManager ).go();
        }
        // new LockLibraryTask( controller.getSelectedItem().getValue(),
        // new RepositoryResultHandler( mainController ),
        // mainController.getStatusController(), controller, mainController.getModelManager() ).go();
    }

    private void removeLibrary(ActionEvent e) {
        OtmLibrary library = getSelected();
        if (library == null)
            return;
        new DexProjectHandler().removeLibrary( library );
        controller.refresh();
    }

    private void saveLibrary() {
        OtmLibrary library = getSelected();
        if (library == null)
            return;
        library.save();
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     * @return
     */
    private void setCSSClass(TreeTableRow<LibraryDAO> tc, TreeItem<LibraryDAO> newTreeItem) {
        if (newTreeItem != null && newTreeItem.getValue() != null) {
            OtmLibrary library = newTreeItem.getValue().getValue();
            if (library != null && library.getModelManager() != null) {
                lockLibrary.setDisable( !library.canBeLocked() );
                unlockLibrary.setDisable( !library.canBeUnlocked() );
                // whereUsed.setDisable( true );
                projectAdd.setDisable( !library.getModelManager().hasProjects() );
                projectRemove.setDisable( library.getProjects().isEmpty() );
                //
                finalState.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.FINAL ) );
                underReview.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.UNDER_REVIEW ) );
                obsolete.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.OBSOLETE ) );
            } else {
                lockLibrary.setDisable( true );
                unlockLibrary.setDisable( true );
                projectAdd.setDisable( true );
                projectRemove.setDisable( true );
                //
                finalState.setDisable( true );
                underReview.setDisable( true );
                obsolete.setDisable( true );
            }
            promote.setDisable( finalState.isDisable() && underReview.isDisable() && obsolete.isDisable() );

            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().getValue().isEditable() );
        }
    }

    private void unlockLibrary() {
        log.debug( "Unlock in Row Factory.   " + controller.getSelectedItem().getValue().getClass().hashCode() );
        UnlockLibraryDialogContoller uldc = UnlockLibraryDialogContoller.init();
        uldc.showAndWait( "" );
        boolean commitWIP = uldc.getCommitState();
        String remarks = uldc.getCommitRemarks();

        new UnlockLibraryTask( controller.getSelectedItem().getValue(), commitWIP, remarks,
            new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
    }
}
