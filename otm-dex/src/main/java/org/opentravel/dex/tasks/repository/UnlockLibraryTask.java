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
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;

/**
 * A Dex/JavaFX task for unlocking libraries via their project item
 * 
 * @author dmh
 *
 */
public class UnlockLibraryTask extends DexTaskBase<OtmLibrary> {
    private static Log log = LogFactory.getLog( UnlockLibraryTask.class );

    boolean commitWIP = true;
    String remarks = "testing";

    public UnlockLibraryTask(OtmLibrary taskData, boolean commitWIP, String remarks, TaskResultHandlerI handler,
        DexStatusController status) {
        super( taskData, handler, status );
        this.commitWIP = commitWIP;
        this.remarks = remarks;

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Unlocking: " );
        msgBuilder.append( taskData.getName() );
        updateMessage( msgBuilder.toString() );
    }

    @Override
    public void doIT() throws RepositoryException {
        OtmLibrary lib = taskData;
        if (lib != null) {
            ProjectManager tlPM = lib.getTLProjectManager();
            if (tlPM != null) {
                ProjectItem item = tlPM.getProjectItem( lib.getTL() );
                if (item != null) {
                    tlPM.unlock( item, commitWIP, remarks );
                    lib.refresh();
                }
            }
        }

        // OtmProject managingProject = taskData.getManagingProject();
        // if (managingProject != null) {
        // Project managingTLProject = managingProject.getTL();
        // ProjectItem pi = managingProject.getProjectItem( taskData.getTL() );
        // if (managingTLProject != null && pi != null) {
        // ProjectManager projectManager = managingTLProject.getProjectManager();
        // if (projectManager != null)
        // projectManager.unlock( pi, commitWIP, remarks );
        // // projectManager.commit( pi, remarks );
        // }
        // }
    }
}
