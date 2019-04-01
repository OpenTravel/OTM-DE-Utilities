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

package org.opentravel.release.undo;

/**
 * Listener that is notified by the <code>UndoManager</code> when the status of the 'canUndo()' or 'canRedo()' statuses
 * have changed.
 */
@FunctionalInterface
public interface UndoStatusListener {

    /**
     * Called when the status of the 'canUndo()' or 'canRedo()' status has changed on the given
     * <code>UndoManager</code>.
     * 
     * @param manager the manager whose status has changed
     */
    public void undoStatusChanged(UndoManager manager);

}
