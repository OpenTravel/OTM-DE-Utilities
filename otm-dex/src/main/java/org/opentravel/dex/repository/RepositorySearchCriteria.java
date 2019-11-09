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

package org.opentravel.dex.repository;

import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Object for repository search terms and criteria.
 * 
 * @author dmh
 *
 */
public class RepositorySearchCriteria {
    private static final Logger LOGGER = LoggerFactory.getLogger( RepositorySearchCriteria.class );

    private String query;
    private Repository repository;
    private OtmObject subject;

    public OtmObject getSubject() {
        return subject;
    }

    public void setSubject(OtmObject subject) {
        this.subject = subject;
    }

    private boolean latestVersionsOnly = false;
    private boolean lockedOnly = false;

    // TLLibraryStatus includeStatus = null; // Draft, Review, Final, Obsolete
    private TLLibraryStatus includeStatus = TLLibraryStatus.DRAFT; // Draft, Review, Final, Obsolete

    // RepositoryItemType itemType = null; // .otm or .otr
    private RepositoryItemType itemType = RepositoryItemType.LIBRARY; // .otm or .otr


    public RepositorySearchCriteria(Repository repository, String query) {
        this.repository = repository;
        this.query = query;
    }

    public RepositorySearchCriteria(Repository repository, OtmObject subject) {
        this.repository = repository;
        this.subject = subject;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isLatestVersionsOnly() {
        return latestVersionsOnly;
    }

    public void setLatestVersionsOnly(boolean latestVersionsOnly) {
        this.latestVersionsOnly = latestVersionsOnly;
    }

    public boolean isLockedOnly() {
        return lockedOnly;
    }

    public void setLockedOnly(boolean lockedOnly) {
        this.lockedOnly = lockedOnly;
    }

    public TLLibraryStatus getIncludeStatus() {
        return includeStatus;
    }

    public void setIncludeStatus(TLLibraryStatus includeStatus) {
        this.includeStatus = includeStatus;
    }

    public RepositoryItemType getItemType() {
        return itemType;
    }

    public void setItemType(RepositoryItemType itemType) {
        this.itemType = itemType;
    }

    public Repository getRepository() {
        return repository;
    }

}
