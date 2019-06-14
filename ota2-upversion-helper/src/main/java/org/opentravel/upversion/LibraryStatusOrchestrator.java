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

package org.opentravel.upversion;

import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Orchestrates the promotion and demotion of OTM libraries from one status to another.
 */
public class LibraryStatusOrchestrator {

    public enum StatusAction {
        PROMOTE, DEMOTE
    }

    private static final Logger log = LoggerFactory.getLogger( LibraryStatusOrchestrator.class );

    private RepositoryManager repositoryManager;
    private List<RepositoryItem> libraryVersions;
    private TLLibraryStatus fromStatus = TLLibraryStatus.DRAFT;
    private StatusAction statusAction;
    private ProgressMonitor monitor;

    /**
     * Assigns the repository manager instance to use during processing. If not assigned, the default instance will be
     * used.
     *
     * @param repositoryManager the field value to assign
     * @return LibraryStatusOrchestrator
     */
    public LibraryStatusOrchestrator setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
        return this;
    }

    /**
     * Assigns the list of repository items for all of the libraries to be promoted or demoted.
     *
     * @param libraryVersions the list of repository items to be promoted or demoted
     * @return LibraryStatusOrchestrator
     */
    public LibraryStatusOrchestrator setLibraryVersions(List<RepositoryItem> libraryVersions) {
        this.libraryVersions = libraryVersions;
        return this;
    }

    /**
     * Assigns the status filter critera for libraries to be promoted or demoted. Only libraries with the specified
     * status will be affected by the 'updateStatus()' operation.
     *
     * @param fromStatus the field value to assign
     * @return LibraryStatusOrchestrator
     */
    public LibraryStatusOrchestrator setFromStatus(TLLibraryStatus fromStatus) {
        this.fromStatus = fromStatus;
        return this;
    }

    /**
     * Assigns the action (promote or demote) that should be performed by the 'updateStatus()' operation.
     * 
     * @param statusAction the action to be performed
     * @return LibraryStatusOrchestrator
     */
    public LibraryStatusOrchestrator setStatusAction(StatusAction statusAction) {
        this.statusAction = statusAction;
        return this;
    }

    /**
     * Assigns the progress monitor that will report on task percent-complete.
     *
     * @param monitor progress monitor that will report on task percent-complete (may be null)
     * @return LibraryStatusOrchestrator
     */
    public LibraryStatusOrchestrator setProgressMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    /**
     * Returns the end-state status to which all affected libraries will be assigned after their statuses have been
     * updated. If the 'fromStatus' and 'statusAction' values are null or invalid, this method will return null.
     * 
     * @return TLLibraryStatus
     */
    public TLLibraryStatus getToStatus() {
        TLLibraryStatus toStatus = null;

        if ((statusAction != null) && (fromStatus != null)) {
            if (statusAction == StatusAction.PROMOTE) {
                toStatus = fromStatus.nextStatus();

            } else {
                toStatus = fromStatus.previousStatus();
            }
        }
        return toStatus;
    }

    /**
     * Returns the number of libraries from the original list that will be affected by the 'updateStatus()' operation.
     * 
     * @return int
     */
    public int getAffectedLibraryCount() {
        return getAffectedLibraries().size();
    }

    /**
     * Returns the total number of libraries from the original list.
     * 
     * @return int
     */
    public int getTotalLibraryCount() {
        return (libraryVersions == null) ? 0 : libraryVersions.size();
    }

    /**
     * Updates the status of all affected libraries based on the selection criteria specified by the caller. If one or
     * more libraries cannot be processed, this method will return false (true = all updates successful).
     * 
     * @return boolean
     * @throws RepositoryException thrown if any of the configuration settings are invalid
     */
    public boolean updateStatus() throws RepositoryException {
        List<RepositoryItem> affectedItems = getAffectedLibraries();
        boolean successInd = true;

        validateStatusUpdates( affectedItems );

        if (monitor != null) {
            monitor.taskStarted( affectedItems.size() );
        }

        // Process the status updates for all affected libraries
        for (RepositoryItem item : affectedItems) {
            try {
                Repository itemRepo = item.getRepository();

                if (itemRepo instanceof RemoteRepository) {
                    ((RemoteRepository) itemRepo).downloadContent( item, true );
                }

                if (statusAction == StatusAction.PROMOTE) {
                    logMessage( "Promoting Library: %s", item.getFilename() );
                    repositoryManager.promote( item );

                } else {
                    logMessage( "Demoting Library: %s", item.getFilename() );
                    repositoryManager.demote( item );
                }

            } catch (RepositoryException e) {
                log.warn( "Error updating status for library: " + item.getFilename(), e );
                successInd = false;

            } finally {
                if (monitor != null) {
                    monitor.progress( 1 );
                }
            }
        }

        if (monitor != null) {
            monitor.taskCompleted();
        }

        return successInd;
    }

    /**
     * Verify that all settings are valid before processing the requested status updates.
     * 
     * @param affectedItems the list of affected repository items whose statuses are to be changed
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     */
    private void validateStatusUpdates(List<RepositoryItem> affectedItems) throws RepositoryException {
        Set<String> repositoryIds = new HashSet<>();

        if (fromStatus == null) {
            throw new RepositoryException( "The fromStatus value cannot be null." );
        }
        if (statusAction == null) {
            throw new RepositoryException( "The statusAction value cannot be null." );

        } else {
            TLLibraryStatus toStatus = getToStatus();

            if (toStatus == null) {
                throw new RepositoryException( "The action '" + statusAction
                    + "' is not valid for libraries with a status of '" + fromStatus + "'." );
            }
        }
        if (affectedItems.isEmpty()) {
            throw new RepositoryException( "No libraries found that match the selection criteria." );
        }
        for (RepositoryItem oldItem : affectedItems) {
            repositoryIds.add( oldItem.getRepository().getId() );
        }
        if (repositoryIds.size() > 1) {
            throw new RepositoryException( "All library versions must originate from the same repository." );
        }
    }

    /**
     * Returns the list of libraries that should be processed by the 'updateStatus()' operation.
     * 
     * @return List&lt;RepositoryItem&gt;
     */
    private List<RepositoryItem> getAffectedLibraries() {
        List<RepositoryItem> affectedItems = new ArrayList<>();

        if (libraryVersions != null) {
            for (RepositoryItem item : libraryVersions) {
                if (item.getStatus() == fromStatus) {
                    affectedItems.add( item );
                }
            }
        }
        return affectedItems;
    }

    /**
     * Logs a message using the format and parameters provided.
     * 
     * @param messageFormat the message format string
     * @param messageParams the message parameters
     */
    private void logMessage(String messageFormat, Object... messageParams) {
        if (log.isInfoEnabled()) {
            log.info( String.format( messageFormat, messageParams ) );
        }
    }

}
