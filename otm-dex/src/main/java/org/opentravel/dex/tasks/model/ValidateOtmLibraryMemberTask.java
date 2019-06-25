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

package org.opentravel.dex.tasks.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.schemacompiler.repository.RepositoryException;

/**
 * A JavaFX task for locking repository items
 * 
 * @author dmh
 *
 */
public class ValidateOtmLibraryMemberTask extends DexTaskBase<OtmLibrary> {
    private static Log log = LogFactory.getLog( ValidateOtmLibraryMemberTask.class );

    /**
     * Create a lock repository item task.
     * 
     * @param taskData - an repository item to lock
     * @param handler - results handler
     * @param status - a status controller that can post message and progress indicator
     */
    public ValidateOtmLibraryMemberTask(OtmLibrary taskData, TaskResultHandlerI handler, DexStatusController status) {
        super( taskData, handler, status );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Validating: " );
        msgBuilder.append( taskData.getName() );
        updateMessage( msgBuilder.toString() );
    }

    @Override
    public void doIT() throws RepositoryException {
        taskData.validate();
    }

}
