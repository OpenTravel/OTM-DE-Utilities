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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.schemacompiler.repository.EntitySearchResult;
import org.opentravel.schemacompiler.repository.LibrarySearchResult;
import org.opentravel.schemacompiler.repository.RepositorySearchResult;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

/**
 * Data Access Object (DAO) for containing individual search result items.
 * 
 * @author dmh
 *
 */
public class SearchResultItemDAO implements DexDAO<String> {
    // private static Logger log = LogManager.getLogger( SearchResultItemDAO.class );

    protected String resultString;
    private RepositorySearchResult result = null;

    /**
     * Create a label row with no other content
     * 
     * @param label
     */
    public SearchResultItemDAO(String label) {
        resultString = label;
    }

    //
    // TODO create a ModelSearchResult object and constructor to use is
    //
    public SearchResultItemDAO(EntitySearchResult result) {
        this.result = result;
        StringBuilder rs = new StringBuilder();
        rs.append( result.getEntityName() );
        rs.append( " " + result.getEntityType() );
        if (result.getRepositoryItem() != null) {
            rs.append( " " + result.getRepositoryItem().getBaseNamespace() );
            rs.append( " " + result.getRepositoryItem().getLibraryName() );
            rs.append( " " + result.getRepositoryItem().getVersion() );
        }
        resultString = rs.toString();
    }

    public SearchResultItemDAO(LibrarySearchResult result) {
        this.result = result;
        StringBuilder rs = new StringBuilder();
        if (result.getRepositoryItem() != null) {
            rs.append( " " + result.getRepositoryItem().getBaseNamespace() );
            rs.append( " " + result.getRepositoryItem().getLibraryName() );
            rs.append( " " + result.getRepositoryItem().getVersion() );
        }
        resultString = rs.toString();
    }

    public StringProperty entityProperty() {
        if (result instanceof EntitySearchResult)
            return new ReadOnlyStringWrapper( ((EntitySearchResult) result).getEntityName() );
        else if (result instanceof RepositorySearchResult)
            return new ReadOnlyStringWrapper( "" );
        else
            return new ReadOnlyStringWrapper( resultString );
    }

    public StringProperty typeProperty() {
        if (result instanceof EntitySearchResult)
            return new ReadOnlyStringWrapper( ((EntitySearchResult) result).getEntityType().getSimpleName() );
        else
            return new ReadOnlyStringWrapper( "" );
    }

    public StringProperty baseNsProperty() {
        if (result instanceof RepositorySearchResult)
            return new ReadOnlyStringWrapper( result.getRepositoryItem().getBaseNamespace() );
        else
            return new ReadOnlyStringWrapper( "" );
    }

    public StringProperty libraryProperty() {
        if (result instanceof RepositorySearchResult)
            return new ReadOnlyStringWrapper( result.getRepositoryItem().getLibraryName() );
        else
            return new ReadOnlyStringWrapper( "" );
    }

    public StringProperty versionProperty() {
        if (result instanceof RepositorySearchResult)
            return new ReadOnlyStringWrapper( result.getRepositoryItem().getVersion() );
        else
            return new ReadOnlyStringWrapper( "" );
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return null;
    }

    @Override
    public String getValue() {
        return resultString;
    }
    //
    // public ImageView getIcon() {
    // return images.getView(element.getIconType());
    // }
    //

    public StringProperty get() {
        return resultProperty();
    }

    public StringProperty resultProperty() {
        return new ReadOnlyStringWrapper( "Property: " + resultString );
    }

    /**
     * Used by tree item for displayed value.
     */
    @Override
    public String toString() {
        return resultString;
    }
}
