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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.tasks.TaskResultHandlerI;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

/**
 * An handler for the results of repository tasks. When successful (null or empty message) the parent is refreshed. On
 * error, a dialog is displayed.
 * 
 * @author dmh
 *
 */
public class RepositoryResultHandler implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( RepositoryResultHandler.class );
    private static final String TITLE = "Repository Error";
    private DexMainController mainController;

    /**
     * When task is complete, post dialog box with any warnings or errors and refresh parent controller.
     * 
     * @param parentController
     */
    public RepositoryResultHandler(DexMainController parentController) {
        this.mainController = parentController;
    }

    // FIXME
    // Value can be null if the task has not been updated. Message is set in a background thread.
    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        if (event != null && event.getTarget() instanceof Task) {
            Object data = ((Task<?>) event.getTarget()).getValue();
            // Post a waring dialog if the task has string value
            log.debug( event.getTarget().getClass().getSimpleName() + " task complete. " );
            if (data instanceof String && (!((String) data).isEmpty())) {
                DialogBoxContoller dbc = DialogBoxContoller.init();
                if (dbc != null)
                    dbc.show( TITLE, (String) data );
            }
            mainController.refresh();
        } else {
            log.warn( "Invalid event in result handler." );
        }
    }
}
