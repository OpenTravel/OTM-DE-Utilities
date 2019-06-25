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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryAvailabilityChecker;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.io.File;
import java.util.List;

/**
 * Handle requests for repository services.
 * 
 * @author dmh
 *
 */
public class DexRepositoryHandler {
    private static Log log = LogFactory.getLog( DexRepositoryHandler.class );

    private RepositoryManager repositoryManager;
    private RepositoryAvailabilityChecker availabilityChecker;
    private boolean repoStatus;

    public DexRepositoryHandler() {
        System.out.println( "Repository Controller initialized." );
        // // Set up repository access
        try {
            repositoryManager = RepositoryManager.getDefault();
            availabilityChecker = RepositoryAvailabilityChecker.getInstance( repositoryManager );
            repoStatus = availabilityChecker.pingAllRepositories( true );
        } catch (RepositoryException e) {
            log.error( "Error initializing repository: " + e.getLocalizedMessage() );
        }
    }

    public List<RemoteRepository> getRepositories() {
        return repositoryManager.listRemoteRepositories();
    }

    /**
     * FUTURE
     * 
     * @return
     */
    public String[] getProjects() {
        String[] empty = {};
        File projectDir = repositoryManager.getProjectsFolder();
        if (projectDir.list() == null)
            return empty;
        return projectDir.list();
    }

    /**
     * @return
     * @throws RepositoryException
     */
    public Repository getLocalRepository() throws RepositoryException {
        return repositoryManager.getDefault();
    }

    public void lock(RepositoryItem ri) {

    }

    public void unlock(RepositoryItem ri) {

    }
}
