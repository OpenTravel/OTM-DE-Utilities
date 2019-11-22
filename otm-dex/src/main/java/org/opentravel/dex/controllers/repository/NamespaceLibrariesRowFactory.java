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

package org.opentravel.dex.controllers.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexProjectHandler;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.UnlockLibraryDialogContoller;
import org.opentravel.dex.controllers.popup.WebViewDialogController;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.tasks.repository.UnlockItemTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

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
/**
 * TreeTableRow is an IndexedCell, but rarely needs to be used by developers creating TreeTableView instances. The only
 * time TreeTableRow is likely to be encountered at all by a developer is if they wish to create a custom rowFactory
 * that replaces an entire row of a TreeTableView.
 * 
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeTableRow.html
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class NamespaceLibrariesRowFactory extends TreeTableRow<RepoItemDAO> {
    private static Log log = LogFactory.getLog( NamespaceLibrariesRowFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );

    private NamespaceLibrariesTreeTableController controller;
    private final ContextMenu contextMenu = new ContextMenu();

    // MenuItem lockLibrary;
    MenuItem unlockLibrary;
    MenuItem promoteLibrary;
    MenuItem addToProject;
    MenuItem browser;
    MenuItem refresh = new MenuItem( "Refresh" );

    private DexMainController mainController;

    public NamespaceLibrariesRowFactory(NamespaceLibrariesTreeTableController controller) {
        this.controller = controller;
        mainController = controller.getMainController();

        // Create Context menu
        // lockLibrary = new MenuItem("Lock");
        unlockLibrary = new MenuItem( "Unlock" );
        promoteLibrary = new MenuItem( "Promote (Future)" );
        addToProject = new MenuItem( "Add To Project" );
        browser = new MenuItem( "Preview in Browser" );
        contextMenu.getItems().addAll( unlockLibrary, promoteLibrary, addToProject, browser, refresh );
        // contextMenu.getItems().addAll(lockLibrary, unlockLibrary, promoteLibrary);
        setContextMenu( contextMenu );

        // Create action for events
        // lockLibrary.setOnAction((e) -> lockLibraryEventHandler());
        unlockLibrary.setOnAction( (e) -> unlockLibraryEventHandler() );
        promoteLibrary.setOnAction( this::promoteLibraryEventHandler );
        addToProject.setOnAction( this::addToProject );
        browser.setOnAction( this::previewInBrowser );
        refresh.setOnAction( e -> refreshView() );

        // // Set editable style listener (css class)
        treeItemProperty().addListener( (obs, oldTreeItem, newTreeItem) -> setCSSClass( this, newTreeItem ) );
    }

    private void previewInBrowser(ActionEvent e) {
        if (controller != null && controller.getSelectedItem() != null) {
            WebViewDialogController wvdc = WebViewDialogController.init();
            wvdc.show( controller.getSelectedItem().getRepositoryURL() );
        }
    }

    private void refreshView() {
        controller.refresh();
    }

    private void addToProject(ActionEvent e) {
        log.debug( "Add to project in Row Factory. " );
        RepositoryManager rm = mainController.getRepositoryManager();
        OtmModelManager mm = mainController.getModelManager();

        RepositoryItem repoItem = null;
        if (controller != null && controller.getSelectedItem() != null)
            repoItem = controller.getSelectedItem().getValue();
        if (repoItem == null)
            return;

        // Find out which project the want to add the repository item (library) to.
        OtmProject oProject = new DexProjectHandler().selectOneProject( mm.getUserProjects() );
        if (oProject != null)
            try {
                ProjectItem pi = mm.getProjectManager().addManagedProjectItem( repoItem, oProject.getTL() );
                mm.addProjects();
                mm.add( pi.getContent() );
                controller.fireEvent( new DexModelChangeEvent( mm ) );
            } catch (LibraryLoaderException | RepositoryException e1) {
                log.error( "Error opening repo item. " + e1.getLocalizedMessage() );
            }
    }

    // /**
    // * Add a new member to the tree
    // *
    // */
    // private void lockLibraryEventHandler() {
    // log.debug("Lock in Row Factory. " + controller.getSelectedItem().getValue().hashCode());
    // if (controller != null && controller.getSelectedItem() != null)
    // new LockItemTask(controller.getSelectedItem().getValue(), new RepositoryResultHandler(mainController),
    // mainController.getStatusController(), controller, mainController.getModelManager()).go();
    // }

    private void unlockLibraryEventHandler() {
        log.debug( "Unlock in Row Factory.   " + controller.getSelectedItem().getValue().getClass().hashCode() );
        UnlockLibraryDialogContoller uldc = UnlockLibraryDialogContoller.init();
        uldc.showAndWait( "" );
        boolean commitWIP = uldc.getCommitState();
        String remarks = uldc.getCommitRemarks();

        new UnlockItemTask( controller.getSelectedItem().getValue(), commitWIP, remarks,
            new RepositoryResultHandler( mainController ), mainController.getStatusController() ).go();
    }

    private void promoteLibraryEventHandler(ActionEvent t) {
        log.debug( "TODO - implement Promote in Row Factory." );
    }

    /**
     * 
     * @return true if user can write the namespace
     */
    // TODO - how to find out if the user has WRITE permission?
    private boolean userCanWrite() {
        return true; //
    }

    /**
     * @param tc
     * @param newTreeItem
     * @return
     */
    // TODO - use style class for warning and error
    private void setCSSClass(TreeTableRow<RepoItemDAO> tc, TreeItem<RepoItemDAO> newTreeItem) {
        // TODO - how to determine if the user is the LockedBy user?
        //
        if (newTreeItem != null) {
            // lockLibrary.setDisable(true);
            unlockLibrary.setDisable( true );
            promoteLibrary.setDisable( true );

            RepoItemDAO repoItem = newTreeItem.getValue();
            RepositoryItem item = newTreeItem.getValue().getValue();
            if (userCanWrite()) {
                String user = item.getLockedByUser();
                if (user != null && !user.isEmpty()) {
                    // Make unlock inactive
                    // lockLibrary.setDisable(true);
                    unlockLibrary.setDisable( false );
                } else {
                    // lockLibrary.setDisable(false);
                    unlockLibrary.setDisable( true );
                }
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
