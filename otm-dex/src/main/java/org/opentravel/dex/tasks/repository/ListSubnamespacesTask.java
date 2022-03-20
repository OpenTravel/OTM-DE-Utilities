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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.repository.NamespacesDAO;
import org.opentravel.dex.tasks.DexTask;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A JavaFX task for listing all sub-namespaces of a namespace root in a repository
 * 
 * @author dmh
 *
 */
public class ListSubnamespacesTask extends DexTaskBase<NamespacesDAO> implements DexTask {
    private static Logger log = LogManager.getLogger( ListSubnamespacesTask.class );

    // Map indexed by the full path of each namespace found
    // Must be sorted to assure parent can be found when processed.
    private SortedMap<String,NamespacesDAO> namespaceMap = new TreeMap<>();

    /**
     * Create a lock repository item task.
     * 
     * @param taskData
     * @param progressProperty
     * @param statusProperty
     * @param handler
     */
    public ListSubnamespacesTask(NamespacesDAO taskData, TaskResultHandlerI handler,
        DexStatusController statusController) {
        super( taskData, handler, statusController );

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Getting namespace: " );
        msgBuilder.append( taskData.getFullPath() );
        updateMessage( msgBuilder.toString() );
    }

    /**
     * Get the sorted list of namespaces (full path) and the associated NamespacesDAO
     * 
     * @return
     */
    public SortedMap<String,NamespacesDAO> getMap() {
        return namespaceMap;
    }

    /**
     * Creates map of all descendant namespaces of the parent namespace. Does add the parent to the map.
     */
    @Override
    public void doIT() throws RepositoryException {
        namespaceMap.put( taskData.getFullPath(), taskData );
        get( taskData );
    }

    private void get(NamespacesDAO parentDAO) throws RepositoryException {
        NamespacesDAO nsData = null;
        for (String childNS : parentDAO.getRepository().listNamespaceChildren( parentDAO.getFullPath() )) {
            nsData = new NamespacesDAO( childNS, parentDAO.getFullPath(), parentDAO.getRepository() );
            namespaceMap.put( nsData.getFullPath(), nsData );
            try {
                get( nsData ); // recurse
            } catch (RepositoryException e) {
                log.error( "Repository Error: " + e.getLocalizedMessage() );
            }
        }
    }
}
