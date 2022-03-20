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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * A Dex/JavaFX task for unlocking repository items
 * 
 * @author dmh
 *
 */
public class UnlockItemTask extends DexTaskBase<RepositoryItem> {
    private static Logger log = LogManager.getLogger( UnlockItemTask.class );

    boolean commitWIP = true;
    String remarks = "testing";

    public UnlockItemTask(RepositoryItem repoItem, boolean commitWIP, String remarks, TaskResultHandlerI handler,
        DexStatusController status) {
        super( repoItem, handler, status );
        this.commitWIP = commitWIP;
        this.remarks = remarks;

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Unlocking: " );
        msgBuilder.append( repoItem.getLibraryName() );
        updateMessage( msgBuilder.toString() );
    }

    @Override
    public void doIT() throws RepositoryException {
        log.debug( "Unlocking " + taskData.getClass().hashCode() );
        taskData.getRepository().unlock( taskData, commitWIP, remarks );
    }
}
