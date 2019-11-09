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
import org.opentravel.dex.repository.NamespacesDAO;
import org.opentravel.dex.tasks.DexTask;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.util.List;

/**
 * A DEX JavaFX task retrieving repository items for a namespace.
 * 
 * @author dmh
 *
 */
public class GetRepositoryItemsTask extends DexTaskBase<NamespacesDAO> implements DexTask {
    private static Log log = LogFactory.getLog( GetRepositoryItemsTask.class );

    private List<RepositoryItem> allItems = null;
    private List<RepositoryItem> latestItems = null;
    private String permission = "unknown";

    public List<RepositoryItem> getAllItems() {
        return allItems;
    }

    public List<RepositoryItem> getLatestItems() {
        return latestItems;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public void doIT() throws RepositoryException {
        // indicates the latest library status to include in the results (null = all statuses)
        TLLibraryStatus includeStatus = null;
        allItems = taskData.getRepository().listItems( taskData.getFullPath(), includeStatus, false );
        latestItems = taskData.getRepository().listItems( taskData.getFullPath(), includeStatus, true );
        //
        permission = taskData.getRepository().getUserAuthorization( taskData.getFullPath() ).toString();

    }

    public GetRepositoryItemsTask(NamespacesDAO taskData, TaskResultHandlerI handler,
        DexStatusController statusController) {
        super( taskData, handler, statusController );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Getting items for: " );
        msgBuilder.append( taskData.getFullPath() );
        updateMessage( msgBuilder.toString() );
    }

}
