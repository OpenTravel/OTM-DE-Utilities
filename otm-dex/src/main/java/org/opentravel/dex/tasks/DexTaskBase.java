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

package org.opentravel.dex.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 * OTM-DE-JavaFX task base class.
 * 
 * @author dmh
 *
 */
public abstract class DexTaskBase<T> extends Task<String> implements DexTask {
    public static Logger log = LogManager.getLogger( DexTaskBase.class );

    protected T taskData;

    protected String startMsg = "Begin: ";
    private Double progress = -0.25;
    private Double progressMax = 1.0;
    private StringBuilder errorBuilder = null;
    protected StringBuilder msgBuilder = null;
    private DexStatusController statusController = null;
    protected DialogBoxContoller dialogBoxController = null;

    private Exception errorException = null;

    public DexTaskBase(T taskData, TaskResultHandlerI handler, DoubleProperty progressProperty,
        StringProperty statusProperty) {
        this( taskData, handler, progressProperty, statusProperty, null );
    }

    /**
     * Create a task complete with result handler, double progress value and status
     * 
     * @param taskData - ALL data needed to execute the task
     * @param handler - handler to receive completion message. Must have controller for accessing the stage.
     * @param progressProperty - optional - progress bar or indicator progress property
     * @param statusProperty - optional - a label or stringProperty for messages from the task
     * @param statusController - Optional, Tracks and shows progress on how many tasks are running
     * 
     */
    public DexTaskBase(T taskData, TaskResultHandlerI handler, DoubleProperty progressProperty,
        StringProperty statusProperty, DexStatusController statusController) {
        this( taskData, handler, statusController );

        // Bind the passed progress bar/indicator and status properties to this task's properties.
        if (progressProperty != null)
            progressProperty.bind( this.progressProperty() );
        if (statusProperty != null)
            statusProperty.bind( this.messageProperty() );

        // // Set the result handler
        // if (handler != null) {
        // setOnSucceeded( handler::handleTaskComplete );
        // setOnFailed( handler::handleTaskComplete );
        // }
        //
        // // Track how many tasks are running
        // this.statusController = statusController;
    }

    /**
     * Start a task with a handler for results and use the status controller.
     * 
     * @param taskData
     * @param handler can be null
     * @param statusController can be null
     */
    public DexTaskBase(T taskData, TaskResultHandlerI handler, DexStatusController statusController) {
        this( taskData );

        // Set the result handler
        if (handler != null) {
            setOnSucceeded( handler::handleTaskComplete );
            setOnFailed( handler::handleTaskComplete );
        }

        // Track how many tasks are running
        this.statusController = statusController;
    }

    public DexTaskBase(T taskData) {
        this.taskData = taskData;

        msgBuilder = new StringBuilder( startMsg );

        updateMessage( msgBuilder.toString() );
        updateProgress( progress, progressMax );
    }

    /**
     * Execute this task in a background thread. Suitable for use in GUI thread. Creates thread, sets as daemon thread
     * to all JVM to exit if thread hangs, then starts the thread.
     * 
     */
    public void go() {
        if (statusController != null)
            statusController.start( this );
        Thread lt = new Thread( this );
        if (Platform.isFxApplicationThread())
            dialogBoxController = DialogBoxContoller.init(); // Prepare a dialog box if task needs it
        lt.setDaemon( true );
        lt.start();
    }

    /**
     * The actual task written as if it was going to run in the GUI thread.
     * <p>
     * On error, throw an exception containing the string description to be shown to the user.
     */
    public abstract void doIT() throws Exception;

    /**
     * Call the doIT() method and then handle result.
     */
    @Override
    protected String call() throws Exception {
        // log.debug("Starting Task.");
        String result = null; // Null result implies success
        if (taskData != null)
            try {
                doIT();
            } catch (Exception e) {
                errorException = e;

                errorBuilder = new StringBuilder( "Error in " + getClass().getSimpleName() + "\n" );
                errorBuilder = errorBuilder.append( " Type: " + e.getClass().getSimpleName() + "\n" );
                if (e.getCause() != null)
                    errorBuilder = errorBuilder.append( " Cause: " + e.getCause() + "\n" );
                errorBuilder = errorBuilder.append( " Message: " + e.getLocalizedMessage() + "\n" );
                if (!e.getMessage().equals( e.getLocalizedMessage() ))
                    errorBuilder = errorBuilder.append( " Message: " + e.getMessage() + "\n" );

                result = errorBuilder.toString(); // Signal business error via result
                if (result == null)
                    result = "Unknown exception performing Dex Task";

                log.warn( "Dex Task Exception - updating task message with: " + result );
                updateMessage( result );

                // Close the "please wait" dialog if shown
                if (dialogBoxController != null)
                    dialogBoxController.close();
                failed();
            }
        updateMessage( "Done." );
        updateProgress( progressMax, progressMax );
        // log.debug(" Task done. ");
        return result;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage( "Done!" );
        if (statusController != null)
            statusController.finish( this );

    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage( "Cancelled!" );
        if (statusController != null)
            statusController.finish( this );
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage( "Failed!" + getMessage() );
        if (statusController != null)
            statusController.finish( this );
        // TEST - use global dbc
        // if (dialogBoxController == null)
        // dialogBoxController = DialogBoxContoller.init();
        // dialogBoxController.show( "Failed", errorBuilder != null ? errorBuilder.toString() : "" );
    }

    public String getErrorMsg() {
        return errorBuilder != null ? errorBuilder.toString() : null;
    }


    public Exception getErrorException() {
        return errorException;
    }
}
