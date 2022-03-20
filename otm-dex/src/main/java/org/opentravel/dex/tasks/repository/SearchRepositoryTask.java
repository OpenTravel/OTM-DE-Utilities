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
import org.opentravel.dex.controllers.repository.RepositorySearchCriteria;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositorySearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFX task for searching for repository items
 * 
 * @author dmh
 *
 */
public class SearchRepositoryTask extends DexTaskBase<RepositorySearchCriteria> {
    private static Logger log = LogManager.getLogger( SearchRepositoryTask.class );

    private List<RepositorySearchResult> repoResults = null;

    public List<RepositorySearchResult> getResults() {
        return repoResults;
    }

    public RepositorySearchCriteria getCriteria() {
        return taskData;
    }


    /**
     * Create a lock repository item task.
     * 
     * @param taskData
     * @param handler - results handler
     * @param status - a status controller that can post message and progress indicator
     */
    public SearchRepositoryTask(RepositorySearchCriteria taskData, TaskResultHandlerI handler,
        DexStatusController statusController) {
        super( taskData, handler, statusController );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Searching repository: " );
        msgBuilder.append( taskData.getQuery() );
        updateMessage( msgBuilder.toString() );
    }


    /**
     * Searches the contents of the repository using the free-text keywords.
     * <p>
     * When latestVersionsOnly selected and when multiple versions of a library match the query, only the latest version
     * will be returned.
     * <p>
     * If status is selected, only versions with the specified status or later will be considered during the search.
     */
    @Override
    public void doIT() throws RepositoryException {
        Repository repo = taskData.getRepository();

        if (repo instanceof RemoteRepository && taskData.getSubject() != null) {
            RemoteRepository rr = (RemoteRepository) repo;
            NamedEntity entity = (NamedEntity) taskData.getSubject().getTL();
            boolean includeIndirect = true;

            repoResults = new ArrayList<>();
            rr.getEntityWhereExtended( entity ).forEach( e -> repoResults.add( e ) );
            repoResults.addAll( rr.getEntityWhereUsed( entity, includeIndirect ) );
            // Returns list of repo items
            // RepositoryItem item = null;
            // rr.getItemWhereUsed( item, includeIndirect );
            log.debug( "Found " + repoResults.size() + " entities." );
        } else {
            // Run full-text search
            // Result list contains both entity and library result items
            repoResults = repo.search( taskData.getQuery(), taskData.getIncludeStatus(),
                taskData.isLatestVersionsOnly(), taskData.getItemType() );
            log.debug( "Found " + repoResults.size() + " items in repo." );
        }
    }
}
