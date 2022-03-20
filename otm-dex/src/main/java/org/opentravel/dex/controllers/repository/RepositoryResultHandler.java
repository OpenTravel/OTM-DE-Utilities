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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static Logger log = LogManager.getLogger( RepositoryResultHandler.class );
    private static final String TITLE_ERROR = "Repository Error";
    private static final String TITLE_OK = "Repository Results";
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
        String data = "";
        String title = TITLE_OK;
        if (event != null) {
            if (event.getEventType() != WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                title = TITLE_ERROR;
                log.warn( "Event in result handler - Worker task did not succeed." );
            }
            if (event.getSource() instanceof Task) {
                Task<?> src = (Task<?>) event.getSource();
                data = src.getMessage();
                log.debug( "Source reports: " + data );
            }
            if (event.getTarget() instanceof Task && ((Task<?>) event.getTarget()).getValue() instanceof String) {
                data += (String) ((Task<?>) event.getTarget()).getValue();
                log.debug( event.getTarget().getClass().getSimpleName() + " task complete. " );
            }
            // This is run in the application thread
            // if (Platform.isFxApplicationThread())
            // log.debug( "In application thread." );
            // Post a dialog if the task has string value
            if (!data.isEmpty()) {
                DialogBoxContoller dbc = DialogBoxContoller.init();
                if (dbc != null)
                    dbc.show( title, (String) data );
            }
        }
        mainController.refresh();
    }
}
