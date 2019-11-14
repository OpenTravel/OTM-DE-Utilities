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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

/**
 * Data Access Object for repository item commit data used in library history table.
 * 
 * @author dmh
 *
 */
public class RepoItemCommitDAO implements DexDAO<RepositoryItemCommit> {
    private static final Logger LOGGER = LoggerFactory.getLogger( RepoItemCommitDAO.class );

    private RepositoryItemCommit item;

    public RepoItemCommitDAO(RepositoryItemCommit item) {
        this.item = item;
    }

    public StringProperty numberProperty() {
        return new ReadOnlyStringWrapper( Integer.toString( item.getCommitNumber() ) );
    }

    public StringProperty effectiveProperty() {
        return new ReadOnlyStringWrapper( item.getEffectiveOn().toString() );
    }

    public StringProperty userProperty() {
        return new ReadOnlyStringWrapper( item.getUser() );
    }

    public StringProperty remarksProperty() {
        return new ReadOnlyStringWrapper( item.getRemarks() );
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return null;
    }

    @Override
    public RepositoryItemCommit getValue() {
        return item;
    }
}
