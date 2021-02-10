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
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A task for computing whereUsed for all members.
 * 
 * @author dmh
 *
 */
public class TypeResolverTask extends DexTaskBase<OtmModelManager> implements DexTaskSingleton {
    private static Log log = LogFactory.getLog( TypeResolverTask.class );

    /**
     * 
     * A task for computing whereUsed for all members.
     * 
     * @param taskData - a model manager with members to resolve types
     * @param handler - results handler
     * @param status - a status controller that can post message and progress indicator
     */
    public TypeResolverTask(OtmModelManager taskData, TaskResultHandlerI handler, DexStatusController status) {
        super( taskData, handler, status );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Resolving assigned types in the model." );
        updateMessage( msgBuilder.toString() );
    }

    // Make sure only one thread is resolving types at a time
    // To Do - create dispatcher that eliminates multiple simultaneous requests
    @Override
    public synchronized void doIT() {
        log.debug( "Starting Type resolver task." );

        // Resolve contextual facet owners.
        Collection<OtmLibraryMember> cfs = taskData.getMembersContextualFacets();
        cfs.forEach( TypeResolverTask::getWhereContributed );

        // Create local copy because other tasks may update
        Collection<OtmLibraryMember> members = new ArrayList<>( taskData.getMembers() );
        // For each member in the model, force a computation of where used.
        members.forEach( m -> ((OtmLibraryMemberBase<?>) m).getWhereUsed( true ) );

        log.debug( "Type resolver task ran against " + members.size() + " model members." );
    }

    /**
     * A static version of the task.
     * 
     * @param mgr
     */
    public static void runResolver(OtmModelManager mgr) {
        // Resolve contextual facet owners.
        Collection<OtmLibraryMember> cfs = mgr.getMembersContextualFacets();
        cfs.forEach( TypeResolverTask::getWhereContributed );

        Collection<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
        // For each member in the model, force a computation of where used.
        members.forEach( m -> ((OtmLibraryMemberBase<?>) m).getWhereUsed( true ) );
    }

    private static void getWhereContributed(OtmLibraryMember member) {
        if (member instanceof OtmContextualFacet)
            ((OtmContextualFacet) member).getWhereContributed();
    }
}
