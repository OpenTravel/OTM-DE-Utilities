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
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.controllers.popup.UnlockAndCommitLibraryDialogController;
import org.opentravel.dex.controllers.popup.UnlockAndCommitLibraryDialogController.TaskRequested;
import org.opentravel.dex.controllers.repository.RepositoryResultHandler;
import org.opentravel.dex.tasks.repository.CommitLibraryTask;
import org.opentravel.dex.tasks.repository.LockLibraryTask;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.dex.tasks.repository.PromoteLibraryTask;
import org.opentravel.dex.tasks.repository.UnlockLibraryTask;
import org.opentravel.dex.tasks.repository.VersionLibraryTask;
import org.opentravel.dex.tasks.repository.VersionLibraryTask.VersionType;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryManager;

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
    // private MenuItem unlockLibrary;
    private MenuItem commitLibrary = new MenuItem( "Commit and Unlock" );
    private MenuItem projectAdd;
    private MenuItem projectRemove;
    private MenuItem saveLibrary;
    private Menu manage;

    private Menu versionMenu;
    private MenuItem major = new MenuItem( "Major" );
    private MenuItem minor = new MenuItem( "Minor" );
    private MenuItem patch = new MenuItem( "Patch" );

    private Menu promote;
    private MenuItem underReview = new MenuItem( "Under Review" );
    private MenuItem finalState = new MenuItem( "Final" );
    private MenuItem obsolete = new MenuItem( "Make Obsolete" );;
    private MenuItem refresh = new MenuItem( "Refresh" );

    public LibraryRowFactory() {
        // Create Context menu
        lockLibrary = new MenuItem( "Lock" );
        // unlockLibrary = new MenuItem( "Commit and Unlock" );
        projectAdd = new MenuItem( "Add to project" );
        projectRemove = new MenuItem( "Remove from project" );
        saveLibrary = new MenuItem( "Save" );

        manage = new Menu( "Manage" );

        promote = new Menu( "Promote" );
        promote.getItems().addAll( underReview, finalState, obsolete );

        versionMenu = new Menu( "Version" );
        versionMenu.getItems().addAll( major, minor );
        // versionMenu.getItems().addAll( major, minor, patch );

        lockLibrary.setOnAction( e -> lockLibrary() );
        // unlockLibrary.setOnAction( e -> unlockLibrary() );
        commitLibrary.setOnAction( e -> commitLibrary() );
        projectAdd.setOnAction( this::addToProject );
        projectRemove.setOnAction( this::removeLibrary );
        saveLibrary.setOnAction( e -> saveLibrary() );
        promote.setOnAction( this::promoteLibrary );
        manage.setOnAction( this::manageLibrary );
        versionMenu.setOnAction( this::versionLibrary );
        refresh.setOnAction( e -> refreshView() );

        contextMenu.getItems().addAll( saveLibrary, new SeparatorMenuItem(), lockLibrary, commitLibrary );
        contextMenu.getItems().addAll( new SeparatorMenuItem(), projectAdd, projectRemove );
        contextMenu.getItems().addAll( new SeparatorMenuItem(), manage, promote, versionMenu );
        contextMenu.getItems().addAll( new SeparatorMenuItem(), refresh );
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
            configureManageMenu( mainController.getRepositoryManager() );
        }
    }

    private void configureManageMenu(RepositoryManager repoMgr) {
        manage.getItems().add( new MenuItem( ManageLibraryTask.LOCAL_REPO ) );
        repoMgr.listRemoteRepositories().forEach( r -> manage.getItems().add( new MenuItem( r.getId() ) ) );
    }

    private void refreshView() {
        controller.refresh();
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
     * Lock a library
     */
    private void lockLibrary() {
        // log.debug( "Lock in Row Factory. " + controller.getSelectedItem().getValue().hashCode() );
        OtmLibrary lib = getSelected();
        if (lib != null)
            new LockLibraryTask( lib, new RepositoryResultHandler( mainController ), statusController, controller,
                modelManager ).go();
    }

    /**
     * Promote a library state
     */
    private void promoteLibrary(ActionEvent e) {
        OtmLibrary lib = getSelected();
        if (lib != null) {
            // log.debug( "Promote " + lib + " in Row Factory. " + controller.getSelectedItem().getValue().hashCode() );
            TLLibraryStatus targetStatus = null;
            if (e.getTarget() == finalState)
                targetStatus = TLLibraryStatus.FINAL;
            else if (e.getTarget() == obsolete)
                targetStatus = TLLibraryStatus.OBSOLETE;
            else if (e.getTarget() == underReview)
                targetStatus = TLLibraryStatus.UNDER_REVIEW;

            if (PromoteLibraryTask.isEnabled( lib, targetStatus ))
                new PromoteLibraryTask( lib, new RepositoryResultHandler( mainController ), statusController,
                    controller, modelManager ).go();
            else
                postReason( "Can not promote library.", PromoteLibraryTask.getReason( lib, targetStatus ) );
        }
    }

    /**
     * Promote a library state
     */
    private void manageLibrary(ActionEvent e) {
        OtmLibrary lib = getSelected();
        if (lib != null) {
            // log.debug( "manage " + lib + " in Row Factory. " + controller.getSelectedItem().getValue() );

            if (ManageLibraryTask.isEnabled( lib )) {
                e.getSource();
                if (e.getTarget() instanceof MenuItem) {
                    String repoId = ((MenuItem) e.getTarget()).getText();
                    new ManageLibraryTask( repoId, lib, new RepositoryResultHandler( mainController ), mainController )
                        .go();
                }
            } else {
                postReason( "Can not manage library.", ManageLibraryTask.getReason( lib ) );
            }
        }
        // FIXME - repository view does not show the new library
        // TODO - include namespace compatibility in isEnabled and present reason if appropriate
    }

    private void postReason(String action, String reason) {
        DialogBoxContoller dbc = DialogBoxContoller.init();
        if (dbc != null)
            dbc.show( action, reason );
    }

    /**
     * Create new version of a library
     */
    private void versionLibrary(ActionEvent e) {
        OtmLibrary lib = getSelected();
        // log.debug( "Version " + lib + " in Row Factory. " + controller.getSelectedItem().getValue().hashCode() );
        VersionLibraryTask.VersionType type = null;
        if (e.getTarget() == major)
            type = VersionType.MAJOR;
        else if (e.getTarget() == minor)
            type = VersionType.MINOR;
        else if (e.getTarget() == patch)
            type = VersionType.PATCH;

        if (VersionLibraryTask.isEnabled( lib )) {
            if (type != null)
                new VersionLibraryTask( type, lib, new RepositoryResultHandler( mainController ), statusController,
                    controller, modelManager ).go();
        } else {
            postReason( "Can not version library.", VersionLibraryTask.getReason( lib ) );
        }
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
                commitLibrary.setDisable( !library.canBeUnlocked() );
                // whereUsed.setDisable( true );
                projectAdd.setDisable( !library.getModelManager().hasProjects() );
                projectRemove.setDisable( library.getProjects().isEmpty() );
                //
                boolean versionEnabled = VersionLibraryTask.isEnabled( library );
                patch.setDisable( !VersionLibraryTask.isEnabled( library ) );
                minor.setDisable( !VersionLibraryTask.isEnabled( library ) );
                major.setDisable( !VersionLibraryTask.isEnabled( library ) );
                //
                finalState.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.FINAL ) );
                underReview.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.UNDER_REVIEW ) );
                obsolete.setDisable( !PromoteLibraryTask.isEnabled( library, TLLibraryStatus.OBSOLETE ) );
            } else {
                lockLibrary.setDisable( true );
                commitLibrary.setDisable( true );
                projectAdd.setDisable( true );
                projectRemove.setDisable( true );
                //
                finalState.setDisable( true );
                underReview.setDisable( true );
                obsolete.setDisable( true );
            }
            promote.setDisable( finalState.isDisable() && underReview.isDisable() && obsolete.isDisable() );
            // versionMenu.setDisable( patch.isDisable() && minor.isDisable() && major.isDisable() );
            manage.setDisable( !ManageLibraryTask.isEnabled( library ) );

            tc.pseudoClassStateChanged( EDITABLE, newTreeItem.getValue().getValue().isEditable() );
        }
    }

    private void commitLibrary() {
        // log.debug(
        // "Commit library in Row Factory. " + controller.getSelectedItem().getValue().getClass().hashCode() );
        UnlockAndCommitLibraryDialogController uldc = UnlockAndCommitLibraryDialogController.init();
        uldc.showAndWait( "" );
        String remarks = uldc.getCommitRemarks();
        TaskRequested task = uldc.getTask();
        switch (task) {
            case Cancel:
                break;
            case UnlockOnly:
                new UnlockLibraryTask( controller.getSelectedItem().getValue(), false, remarks,
                    new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
                break;
            case CommitAndUnlock:
                new UnlockLibraryTask( controller.getSelectedItem().getValue(), true, remarks,
                    new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
                break;
            case CommitOnly:
                new CommitLibraryTask( controller.getSelectedItem().getValue(), remarks,
                    new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
                break;
        }
    }
}
