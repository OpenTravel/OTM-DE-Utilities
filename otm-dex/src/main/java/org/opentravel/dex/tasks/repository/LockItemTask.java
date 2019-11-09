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

package org.opentravel.dex.tasks.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.events.DexRepositoryItemReplacedEvent;
import org.opentravel.dex.tasks.DexTask;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * A JavaFX task for locking repository items (6/17/2019 - removed from menu -- this may no longer be used)
 * 
 * @see LockLibraryTask
 * @author dmh
 *
 */
public class LockItemTask extends DexTaskBase<RepositoryItem> implements DexTask {
    private static Log log = LogFactory.getLog( LockItemTask.class );

    private DexStatusController statusController;
    private OtmProject proj = null;
    private OtmLibrary library = null;

    private RepositoryItem repoItem;

    private OtmModelManager modelManager;

    private DexIncludedController<?> eventController;

    /**
     * Create a lock repository item task.
     * 
     * @param taskData - an repository item to lock.
     * @param handler - results handler
     * @param controller - status controller that can post message and progress indicator
     * @param eventController - controller to publish repository item replaced event
     * @param modelManager - model manager that holds projects that could contain the library in this repository item
     */
    public LockItemTask(RepositoryItem taskData, TaskResultHandlerI handler, DexStatusController controller,
        DexIncludedController<?> eventController, OtmModelManager modelManager) {
        super( taskData, handler, controller );
        if (taskData == null)
            return;

        this.statusController = controller;
        this.repoItem = taskData;
        this.modelManager = modelManager;
        this.eventController = eventController;

        // Try to find the actual modeled library. A modeled library will be created by opening a project.
        library = modelManager.get( taskData.getNamespace() + "/" + taskData.getLibraryName() );
        // See if there is an open project to manage this item and use it
        if (library != null) {
            proj = modelManager.getManagingProject( library );
        }

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Locking: " );
        msgBuilder.append( taskData.getLibraryName() );
        updateMessage( msgBuilder.toString() );
    }

    @Override
    public void doIT() throws RepositoryException {
        log.debug( "Locking repository item: " + repoItem.hashCode() );
        OtmModelManager mgr = modelManager;
        if (mgr == null)
            return;

        if (proj != null) {
            // repoItem is a copy made by java/fx concurrency model (I think). It has a different hashcode than
            log.debug( "Locking with project item: " + proj.getProjectItem( library.getTL() ).hashCode() );

            proj.getTL().getProjectManager().lock( proj.getProjectItem( library.getTL() ) );

            // RepositoryItem newRI = taskData.getRepository().getRepositoryItem(taskData.getBaseNamespace(),
            // taskData.getFilename(), taskData.getVersion());
            // if (newRI != null && repoItem != newRI) {
            // // repoItem is now stale--and held in a list by the NamespacesDAO
            // // throw event so ns-library view is rebuilt on task complete.
            // log.debug("Ready to replace" + repoItem.hashCode() + " with " + newRI.hashCode());
            // throwRepoItemReplacedEvent(repoItem, newRI);
            // log.debug(newRI.getLibraryName() + " locked by " + newRI.getLockedByUser());
            // }
        } else if (repoItem != null) {
            repoItem.getRepository().lock( repoItem );
        }
    }

    /**
     * Inform application that a repository item has changed. May be needed when locking an item since the items are
     * held in other controller's DAOs.
     * 
     * @param oldItem
     * @param newItem
     */
    private void throwRepoItemReplacedEvent(RepositoryItem oldItem, RepositoryItem newItem) {
        eventController.publishEvent( new DexRepositoryItemReplacedEvent( this, oldItem, newItem ) );
    }

}
