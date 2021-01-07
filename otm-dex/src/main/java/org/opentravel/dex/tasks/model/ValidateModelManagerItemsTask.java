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
import org.opentravel.dex.tasks.DexTaskSingleton;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A JavaFX task for locking repository items
 * 
 * @author dmh
 *
 */
// FIXME - will get concurrent modification error if the model is closed before validation is finsihed
//
public class ValidateModelManagerItemsTask extends DexTaskBase<OtmModelManager> implements DexTaskSingleton {
    private static Log log = LogFactory.getLog( ValidateModelManagerItemsTask.class );

    /**
     * Create a lock repository item task.
     * 
     * @param taskData - the model manager
     * @param handler - results handler
     * @param status - a status controller that can post message and progress indicator
     */
    public ValidateModelManagerItemsTask(OtmModelManager taskData, TaskResultHandlerI handler,
        DexStatusController status) {
        super( taskData, handler, status );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Validating model." );
        // msgBuilder.append(taskData.getName());
        // updateMessage(msgBuilder.toString());
    }

    @Override
    public void doIT() throws RepositoryException {
        // Work from private copy since other tasks could change the collection
        Collection<OtmLibraryMember> members = new ArrayList<>( taskData.getMembers() );
        log.debug( "Starting to validate " + members.size() + " model members." );
        members.forEach( m -> m.isValid( true ) );
    }

    /**
     * Static version of validation logic
     * 
     * @param manager
     */
    public static void runValidator(OtmModelManager manager) {
        Collection<OtmLibraryMember> members = new ArrayList<>( manager.getMembers() );
        members.forEach( m -> m.isValid( true ) );
    }
}
