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

package org.opentravel.dex.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.DexTaskSingleton;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Manage the status bar containing a label and progress indicator.
 * 
 * @author dmh
 *
 */
public class DexStatusController extends DexIncludedControllerBase<String> {
    private static Logger log = LogManager.getLogger( DexStatusController.class );

    List<DexTaskBase<?>> runningTasks;

    // FXML inject
    @FXML
    private ProgressIndicator statusProgress;
    @FXML
    private Label statusLabel;
    @FXML
    private Label taskCount;

    @Override
    public void checkNodes() {
        if (!(statusProgress instanceof ProgressIndicator))
            throw new IllegalStateException( "Progress indicator not injected by FXML." );
        if (!(statusLabel instanceof Label))
            throw new IllegalStateException( "Status label not injected by FXML." );
        if (!(taskCount instanceof Label))
            throw new IllegalStateException( "Task count not injected by FXML." );
        // log.debug( "FXML Nodes checked OK." );
    }

    public DexStatusController() {
        // log.debug( "Starting constructor." );
    }

    @Override
    @FXML
    public void initialize() {
        // log.debug( "Status Controller initialized." );

        if (runningTasks == null)
            runningTasks = new ArrayList<>();
    }

    public void postProgress(double percent) {
        if (statusProgress != null)
            updateProgress( percent );
    }

    public void postStatus(String status) {
        String displayText = status;
        if (status != null && status.length() > 80)
            displayText = displayText.substring( 0, 80 );// don't overflow the status bar area
        if (statusLabel != null)
            if (Platform.isFxApplicationThread())
                statusLabel.setText( displayText );
            else
                Platform.runLater( () -> postStatus( status ) );
    }

    public void postStatus(int count, String status) {
        String displayText = status;
        if (status != null && status.length() > 80)
            displayText = displayText.substring( 0, 80 );// don't overflow the status bar area
        if (statusLabel != null)
            if (Platform.isFxApplicationThread()) {
                statusLabel.setText( displayText );
                taskCount.setText( String.valueOf( count ) );
            } else
                Platform.runLater( () -> postStatus( count, status ) );
    }

    /**
     * @param dexTaskBase
     */
    public void start(DexTaskBase<?> task) {
        if (task instanceof DexTaskSingleton)
            cancelIfRunning( task );

        runningTasks.add( task );
        update();
        postStatus( runningTasks.size(), "Running: " + task.getMessage() );
        // postStatus("Running " + runningTasks.size() + " tasks. Current task: " + task.getMessage());
    }

    private void cancelIfRunning(DexTaskBase<?> task) {
        List<DexTaskBase<?>> toCancel = new ArrayList<>();
        for (DexTaskBase<?> t : runningTasks)
            if (t.getClass() == task.getClass()) {
                log.debug( "Already running. Canceling old task: " + task.getClass().getSimpleName() );
                toCancel.add( t );
            }
        for (DexTaskBase<?> t : toCancel) {
            t.cancel();
            finish( t );
        }
    }

    /**
     * Remove the task from list of running tasks.
     * 
     * @param dexTaskBase
     */
    public void finish(DexTaskBase<?> task) {
        runningTasks.remove( task );
        update();
    }

    private void update() {
        if (Platform.isFxApplicationThread()) {
            postStatus( "Running " + runningTasks.size() + " tasks." );
            if (runningTasks.isEmpty()) {
                updateProgress( 1F );
                // taskProgress.set(1.0);
                postStatus( 0, "Done." );
            } else {
                updateProgress( -1.0 );
                // Must be in application thread to get message from task
                postStatus( runningTasks.size(),
                    "Running: " + runningTasks.get( runningTasks.size() - 1 ).getMessage() );
            }
        } else {
            Platform.runLater( this::update );
        }
    }

    private void updateProgress(double value) {
        if (Platform.isFxApplicationThread())
            // taskProgress.set(value);
            statusProgress.progressProperty().set( value );
        else
            Platform.runLater( () -> updateProgress( value ) );

    }

    /**
     * @return the number of running tasks \
     */
    public int getQueueSize() {
        return runningTasks.size();
    }

}
