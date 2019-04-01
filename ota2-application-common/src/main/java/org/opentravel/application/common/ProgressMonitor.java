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

package org.opentravel.application.common;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;

/**
 * Class that can be used by background processes to can track and report the progress of a long-running task.
 */
public class ProgressMonitor {

    private ProgressIndicator progressInd;
    private long totalWork;
    private long cumulativeWork;

    /**
     * Constructor that supplies the <code>ProgressIndicator</code> that will provide the visual updates of task
     * progress.
     * 
     * @param progressInd the progress indicator control
     */
    public ProgressMonitor(ProgressIndicator progressInd) {
        this.progressInd = progressInd;
    }

    /**
     * Called by the long-running process when work has begun.
     * 
     * @param totalWork the total number of work units expected during execution of the long-running task
     */
    public void taskStarted(long totalWork) {
        this.totalWork = totalWork;
    }

    /**
     * Call by the long-running process when some number of work units have been completed. The number reported to this
     * method represents the work units reported since the last progress report (not the cumulative total number of
     * units reported so far).
     * 
     * @param workProgress the incremental number of work units completed
     */
    public void progress(int workProgress) {
        this.cumulativeWork += workProgress;
        updateProgressIndicator();
    }

    /**
     * Called by the long-running process when work has been completed.
     */
    public void taskCompleted() {
        this.cumulativeWork = totalWork;
        updateProgressIndicator();
    }

    /**
     * Updates the visual state of the progress indicator with the current percent-complete value.
     */
    private void updateProgressIndicator() {
        Platform.runLater( () -> progressInd.setProgress( ((double) cumulativeWork) / ((double) totalWork) ) );
    }

}
