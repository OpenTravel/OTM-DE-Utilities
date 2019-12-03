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

package org.opentravel.dex.controllers.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.schemacompiler.repository.EntitySearchResult;
import org.opentravel.schemacompiler.repository.LibrarySearchResult;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositorySearchResult;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * Data Access Object (DAO) for containing search results.
 * 
 * @author dmh
 *
 */
public class SearchResultsDAO implements DexDAO<String> {
    private static Log log = LogFactory.getLog( SearchResultsDAO.class );

    private Repository repository;
    //
    // private String decoration = "";

    private List<SearchResultItemDAO> results = null;

    private List<EntitySearchResult> entityResults = null;
    private List<LibrarySearchResult> libraryResults = null;


    public SearchResultsDAO(Repository repo, List<RepositorySearchResult> repoResults) {
        this.repository = repo;

        this.entityResults = new ArrayList<>();
        this.libraryResults = new ArrayList<>();
        repoResults.forEach( r -> {
            if (r instanceof EntitySearchResult)
                entityResults.add( (EntitySearchResult) r );
            else if (r instanceof LibrarySearchResult)
                libraryResults.add( (LibrarySearchResult) r );
        } );
    }


    // public SearchResultsDAO(Repository repo) {
    // this.setRepository( repo );
    // results = new ArrayList<>();
    // }


    // /**
    // * @return the namespace without base path
    // */
    // public List<SearchResultItemDAO> get() {
    // return results;
    // }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return null;
    }

    // public List<RepositoryItem> getLatestItems() {
    // return latestItems;
    // }

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    @Override
    public String getValue() {
        return "";
    }
    //
    // public ImageView getIcon() {
    // return images.getView(element.getIconType());
    // }
    //

    public void createTreeItems(TreeItem<SearchResultItemDAO> parent) {
        this.results = new ArrayList<>();
        SearchResultItemDAO dao = null;
        TreeItem<SearchResultItemDAO> categoryItem = null;

        dao = new SearchResultItemDAO( "Libraries" );
        if (!libraryResults.isEmpty()) {
            categoryItem = createTreeItem( dao, parent );
            for (LibrarySearchResult result : libraryResults) {
                dao = new SearchResultItemDAO( result );
                results.add( dao );
                createTreeItem( dao, categoryItem );
            }
        }
        dao = new SearchResultItemDAO( "Entities" );
        categoryItem = createTreeItem( dao, parent );
        for (EntitySearchResult result : entityResults) {
            dao = new SearchResultItemDAO( result );
            results.add( dao );
            createTreeItem( dao, categoryItem );
        }
    }

    public TreeItem<SearchResultItemDAO> createTreeItem(SearchResultItemDAO result,
        TreeItem<SearchResultItemDAO> parent) {
        TreeItem<SearchResultItemDAO> item = new TreeItem<>( result );
        if (parent != null)
            parent.getChildren().add( item );
        return item;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Used by tree item for displayed value.
     */
    @Override
    public String toString() {
        return "TODO - search results";
    }
}
