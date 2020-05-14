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

/**
 * OTM-DE-JavaFX task interface.
 * 
 * @author dmh
 *
 */
public interface DexTask {

    /**
     * Go. Execute this task in a background thread.
     * <p>
     * Suitable for use in GUI thread. Creates thread, sets as daemon thread to all JVM to exit if thread hangs, then
     * starts the thread. Calls the task, adds it to the list of running tasks in the status controller and posts status
     * messages. Post errors if the task throws an error.
     */
    public void go();

    /**
     * Do the task in the current thread. Primarily used for testing.
     * <p>
     * This method implements the actual task written as if it was going to run in the GUI thread. On error
     * implementations must throw an exception containing the string description to be shown to the user.
     * 
     */
    public abstract void doIT() throws Exception;

    /**
     * Get the error message returned from the task. This will have already been displayed in a dialog box.
     * 
     * @return
     */
    public String getErrorMsg();

    /**
     * Get the actual exception thrown by the task.
     * 
     * @return exception thrown or null
     */
    public Exception getErrorException();
}
