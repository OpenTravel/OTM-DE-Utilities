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
import org.opentravel.common.DexProjectException;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.application.Platform;

/**
 * A JavaFX task for Managing Otm Libraries in a repository
 * 
 * @author dmh
 *
 */
public class ManageLibraryTask extends DexTaskBase<OtmLibrary> {
    private static Log log = LogFactory.getLog( ManageLibraryTask.class );

    private static String errorMsg;
    public static final String LOCAL_REPO = "Local";
    private static final String VETO =
        "org.opentravel.schemacompiler.TLLibrary.namespace.INVALID_NAMESPACE_FOR_VERSION_SCHEME";
    private static final String[] VETOKEYS = {VETO};

    private OtmLibrary library = null;
    private Repository repository = null;

    /**
     * In spite of java-doc, getVersionIdentifier will supply default version scheme if not on URL
     * <p>
     * VersionScheme vs = VersionSchemeFactory.getInstance().getVersionScheme( lib.getTL().getVersionScheme() );
     * <p>
     * vId = vs.getVersionIdentifier( lib.getTL().getNamespace() );
     * 
     * @return null string if valid, validation findings otherwise
     */
    private static String checkUrl(OtmLibrary lib) {
        ValidationFindings rFindings = null;
        if (lib != null) {
            lib.validate();
            rFindings = ValidationUtils.getRelevantFindings( VETOKEYS, lib.getFindings() );
            // log.debug( ValidationUtils.getMessagesAsString( lib.getFindings() ) );
        }
        return ValidationUtils.hasErrors( rFindings ) ? ValidationUtils.getMessagesAsString( rFindings ) : null;
    }

    public static String getReason(OtmLibrary library) {
        if (errorMsg == null)
            isEnabled( library );
        return errorMsg;
    }

    /**
     * 
     * @param lib
     * @return false if task should be disabled in menus
     */
    public static boolean isEnabled(OtmLibrary lib) {
        errorMsg = null;
        if (lib == null)
            errorMsg = "Null library.";
        else if (!(lib instanceof OtmLocalLibrary))
            errorMsg = "Not an unmanaged library.";
        else if (!(lib.getTL() instanceof TLLibrary))
            errorMsg = "This type of library can't be versioned.";
        else if (lib.getState() != RepositoryItemState.UNMANAGED)
            errorMsg = "State is already managed. " + lib;
        else if (lib.getModelManager().getOtmProjectManager() == null)
            errorMsg = "Missing project manager needed to publish library.";
        return errorMsg == null;
    }

    /**
     * Create a lock library task.
     * 
     * @param repoId - string known to the repository manager that identifies a specific repository
     * @param taskData - a library to manage.
     * @param handler - results handler
     * @param mainController - DexMainController <b>must</b> not be null
     * @throws DexTaskException
     */
    public ManageLibraryTask(String repoId, OtmLibrary taskData, TaskResultHandlerI handler,
        DexMainController mainController) {
        super( taskData, handler, mainController.getStatusController() );

        if (taskData == null)
            throw new IllegalArgumentException( "Missing library to manage." );
        this.library = taskData;

        this.repository = getSelectedRepository( repoId, mainController.getRepositoryManager() );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Versioning: " );
        msgBuilder.append( library.getName() );
        updateMessage( msgBuilder.toString() );
    }

    /**
     * @see org.opentravel.dex.tasks.DexTaskBase#doIT()
     */
    @Override
    public void doIT() throws DexTaskException {
        // log.debug( "Manage library task: " + library );

        // Re-check isEnabled()
        if (!isEnabled( library ))
            throw new DexTaskException( errorMsg );

        // Check conditions that are not checked for in isEnabled()
        String relevantFindingMessages = checkUrl( library );
        if (relevantFindingMessages != null)
            throw new DexTaskException(
                "Invalid Namespace. Namespace must end with version (e.g. xxx/v1). \n " + relevantFindingMessages );

        if (dialogBoxController != null)
            Platform.runLater( () -> dialogBoxController.show( "Manage Library Task", "Please wait." ) );

        // Manage the library in the repository. PM and Cast checked in isEnabled()
        OtmProjectManager otmPM = library.getModelManager().getOtmProjectManager();
        try {
            otmPM.publish( (OtmLocalLibrary) library, repository );
        } catch (DexProjectException e) {
            if (dialogBoxController != null)
                Platform.runLater( () -> dialogBoxController.close() );
            throw new DexTaskException( e );
        }

        library.refresh();
        if (dialogBoxController != null)
            Platform.runLater( () -> dialogBoxController.close() );
    }

    /**
     * @throws RepositoryException
     */
    public Repository getSelectedRepository(String rid, RepositoryManager repositoryManager) {
        Repository repo = null;

        if (rid != null && repositoryManager != null)
            if (!rid.equals( LOCAL_REPO ))
                // Use selected repository
                repo = repositoryManager.getRepository( rid );
            else
                try {
                    repo = RepositoryManager.getDefault();
                } catch (RepositoryException e) {
                    repo = null;
                }
        return repo;
    }
}
