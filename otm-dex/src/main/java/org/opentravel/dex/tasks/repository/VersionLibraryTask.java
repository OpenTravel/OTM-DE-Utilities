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

import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.MinorVersionHelper;

import javafx.application.Platform;

/**
 * A JavaFX task for Promoting Otm Libraries (Under Review, Final, Obsolete)
 * 
 * @author dmh
 *
 */
public class VersionLibraryTask extends DexTaskBase<OtmLibrary> {
    // private static Log log = LogFactory.getLog( VersionLibraryTask.class );

    public enum VersionType {
        MAJOR, MINOR, PATCH;
    }

    // private DexIncludedController<?> eventController;
    private OtmProject proj = null;
    private OtmLibrary library = null;
    private VersionType type = null;
    private static String errorMsg;

    /**
     * Create a lock library task.
     * 
     * @param taskData - an repository item to lock.
     * @param handler - results handler
     * @param statusController - status controller that can post message and progress indicator
     * @param eventController - controller to publish repository item replaced event
     * @param modelManager - model manager that holds projects that could contain the library in this repository item
     */
    public VersionLibraryTask(VersionType type, OtmLibrary taskData, TaskResultHandlerI handler,
        DexStatusController statusController, DexIncludedController<?> eventController, OtmModelManager modelManager) {
        super( taskData, handler, statusController );
        if (taskData == null)
            return;

        this.library = taskData;
        // this.eventController = eventController;

        if (modelManager != null)
            proj = modelManager.getManagingProject( library );
        this.type = type;

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Versioning: " );
        msgBuilder.append( library.getName() );
        updateMessage( msgBuilder.toString() );
    }

    public static boolean isEnabled(OtmLibrary lib) {
        errorMsg = null;
        if (lib == null)
            errorMsg = "Null library.";
        else {
            ProjectItem pi = lib.getProjectItem();
            if (!(pi instanceof RepositoryItem))
                errorMsg = "Missing project item on " + lib;
            else {
                if (pi.getRepository() == null)
                    errorMsg = "Missing repository on " + lib;
            }

            if (!(lib.getTL() instanceof TLLibrary))
                errorMsg = "This type of library can't be versioned.";
            else if (lib.getState() != RepositoryItemState.MANAGED_UNLOCKED)
                errorMsg = "State is not managed and unlocked " + lib;
            else if (lib.getStatus() != TLLibraryStatus.FINAL)
                errorMsg = "Status is not final. " + lib;
            else if (!lib.isValid()) {
                errorMsg = "Library is not valid " + lib;
                // TODO - refactor this into ValidationUtils
                for (String msg : lib.getFindings().getValidationMessages( FindingType.ERROR,
                    FindingMessageFormat.DEFAULT ))
                    errorMsg += msg;
            }
        }
        // log.debug( "Versioning: IsEnabled? " + errorMsg == null );
        return errorMsg == null;
    }

    public static String getReason(OtmLibrary library) {
        if (errorMsg == null)
            isEnabled( library );
        return errorMsg;
    }

    @Override
    public void doIT() throws DexTaskException {
        // public void doIT() throws RepositoryException, VersionSchemeException, ValidationException,
        // LibrarySaveException,
        // PublishWithLocalDependenciesException, LibraryLoaderException {

        if (isEnabled( library ) && proj != null && type != null) {
            // log.debug( type + "Version with project item: " + proj.getProjectItem( library.getTL() ) );
            if (dialogBoxController != null)
                Platform.runLater( () -> dialogBoxController.show( "Version Library Task", "Please wait." ) );
            try {
                // Create a version in local files
                TLLibrary tlNewLibrary = null;
                switch (type) {
                    case MAJOR:
                        MajorVersionHelper vh = new MajorVersionHelper( proj.getTL() );
                        tlNewLibrary = vh.createNewMajorVersion( (TLLibrary) library.getTL() );
                        break;
                    case MINOR:
                        // Minor - must create from latest minor not the patch.
                        MinorVersionHelper minorVH = new MinorVersionHelper( proj.getTL() );
                        tlNewLibrary = minorVH.createNewMinorVersion( (TLLibrary) library.getTL() );
                        break;
                    case PATCH:
                        // UNTESTED
                        // PatchVersionHelper patchVH = new PatchVersionHelper( proj.getTL() );
                        // tlNewLibrary = patchVH.createNewPatchVersion( (TLLibrary) library.getTL() );
                        break;
                }

                if (tlNewLibrary != null) {
                    // Manage the library in the repository
                    ProjectManager pm = proj.getTL().getProjectManager();
                    Repository repo = library.getProjectItem().getRepository();
                    ProjectItem item = proj.getProjectItem( tlNewLibrary );
                    pm.publish( item, repo );

                    // Add to project and model
                    proj.getTL().getProjectManager().addManagedProjectItem( item, proj.getTL() );
                    library.getModelManager().addProjects();
                    // Add projects will refresh libraries and end task handler will refresh main controller
                }
                // log.debug( "Version library task complete. " );
            } catch (Exception e) {
                // Close "please wait" dialog if shown
                if (dialogBoxController != null)
                    Platform.runLater( () -> dialogBoxController.close() );
                throw new DexTaskException( e );
            }
            if (dialogBoxController != null)
                Platform.runLater( () -> dialogBoxController.close() );
        }
    }

}
