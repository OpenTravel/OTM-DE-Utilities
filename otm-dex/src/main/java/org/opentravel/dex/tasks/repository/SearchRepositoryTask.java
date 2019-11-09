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
import org.opentravel.dex.repository.RepositorySearchCriteria;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.repository.EntitySearchResult;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositorySearchResult;

import java.util.List;
import java.util.Map;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

/**
 * A JavaFX task for searching for repository items
 * 
 * @author dmh
 *
 */
public class SearchRepositoryTask extends DexTaskBase<RepositorySearchCriteria> {
    private static Log log = LogFactory.getLog( SearchRepositoryTask.class );

    private List<RepositorySearchResult> fullTextResults = null;
    private List<EntitySearchResult> entityResults = null;
    // private Map<String,RepositoryItem> filterMap;

    public List<EntitySearchResult> getEntityResults() {
        return entityResults;
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

    @Deprecated
    public SearchRepositoryTask(RepositorySearchCriteria taskData, TaskResultHandlerI handler,
        DoubleProperty progressProperty, StringProperty statusProperty) {
        super( taskData, handler, progressProperty, statusProperty );

        // Replace start message from super-type.
        // msgBuilder = new StringBuilder("Locking: ");
        // msgBuilder.append(taskData.getLibraryName());
        // updateMessage(msgBuilder.toString());
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
        // // TLLibraryStatus includeStatus = null; // Draft, Review, Final, Obsolete
        // TLLibraryStatus includeStatus = TLLibraryStatus.DRAFT; // Draft, Review, Final, Obsolete
        // // RepositoryItemType itemType = null; // .otm or .otr
        // RepositoryItemType itemType = RepositoryItemType.LIBRARY; // .otm or .otr

        Repository repo = taskData.getRepository();
        List<RepositorySearchResult> found;

        if (repo instanceof RemoteRepository && taskData.getSubject() != null) {
            RemoteRepository rr = (RemoteRepository) repo;
            NamedEntity entity = (NamedEntity) taskData.getSubject().getTL();
            boolean includeIndirect = true;
            // RepositoryItem item = null;
            entityResults = rr.getEntityWhereExtended( entity );
            entityResults.addAll( rr.getEntityWhereUsed( entity, includeIndirect ) );
            // rr.getItemWhereUsed( item, includeIndirect );
            log.debug( "Found " + entityResults.size() + " entities." );
        } else {
            // Run full-text search
            fullTextResults = repo.search( taskData.getQuery(), taskData.getIncludeStatus(),
                taskData.isLatestVersionsOnly(), taskData.getItemType() );
            log.debug( "Found " + fullTextResults.size() + " items in repo." );
        }
        // Without itemType set, list contains EntitySearchResult(s) and LibrarySearchResult(s)
        // Library results contain a repositoryItem
        // Entity contains: object (bo, core, choice...), object type, repositoryItem

        //
        // Package up a map of namespaces (repoItem.baseNamespace() : repoItem) as filter selector
        // Use keys in namespace tree
        // use entryset for repo items in ns-libraries tree
        // Throw away entity entries
        // filterMap = new HashMap<>();
        // for (RepositorySearchResult result : found) {
        // if (result instanceof LibrarySearchResult) {
        // RepositoryItem ri = ((LibrarySearchResult) result).getRepositoryItem();
        // if (ri != null)
        // filterMap.put( ri.getBaseNamespace(), ri );
        // }
        // }

        // TODO -
        // Merge into repositorySelectionController???
        // Add filter for object names

        // TODO
        // List<RepositoryItem> locked = taskData.getLockedItems();
        // Clear search
    }

    public List<RepositorySearchResult> getFullTextResults() {
        return fullTextResults;
    }

    /**
     * Get the map of namespaces and repository items that should be included in displayed trees.
     * 
     * @return
     */
    public Map<String,RepositoryItem> getFilterMap() {
        return null;
    }
}
