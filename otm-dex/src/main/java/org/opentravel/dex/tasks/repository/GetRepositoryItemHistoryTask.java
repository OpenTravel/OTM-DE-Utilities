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
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.DexTask;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;

/**
 * A DEX JavaFX task retrieving a repository item's history.
 * 
 * @author dmh
 *
 */
public class GetRepositoryItemHistoryTask extends DexTaskBase<RepositoryItem> implements DexTask {
    private static Log log = LogFactory.getLog( GetRepositoryItemHistoryTask.class );

    RepositoryItemHistory history = null;

    public RepositoryItemHistory getHistory() {
        return history;
    }

    @Override
    public void doIT() throws RepositoryException {
        history = taskData.getRepository().getHistory( taskData );
    }

    public GetRepositoryItemHistoryTask(RepositoryItem taskData, TaskResultHandlerI handler,
        DexStatusController statusController) {
        super( taskData, handler, statusController );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Getting history for: " );
        msgBuilder.append( taskData.getLibraryName() );
        updateMessage( msgBuilder.toString() );
    }

}
