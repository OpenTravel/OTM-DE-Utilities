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

package org.opentravel.dex.controllers.popup;

import org.opentravel.dex.controllers.DexController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;

/**
 * Abstract interface for all Otm-DE FX view controllers.
 * 
 * @author dmh
 *
 */
public interface DexPopupController extends DexController {

    /**
     * Display this pop-up dialog to the user and post the message if supported. The GUI will not force wait for it to
     * be closed.
     * 
     * @param message
     */
    public void show(String message);

    /**
     * Shows this pop-up dialog and waits for it to be hidden (closed) before returning to the caller.
     * 
     * @param message
     */
    public Results showAndWait(String message);

    /**
     * Set the title for the pop-up dialog. Set and override in sub-types to assure the same title is always used.
     * 
     * @param title
     */
    void setTitle(String title);

}
