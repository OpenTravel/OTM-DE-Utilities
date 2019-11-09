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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Interface for all Otm-DE FX tab controllers.
 * <p>
 * Tab controllers simply pass the included controllers to the parent.
 * 
 * @author dmh
 *
 */
public interface DexTabController {

    /**
     * Check FXML injected fields and throw illegal state exceptions if not found.
     */
    public void checkNodes();

    /**
     * Does this tab have a stand alone dialog defined? If so, return its title. If not, return null.
     */
    public String getDialogTitle();

    public void launchWindow(ActionEvent e);

    /**
     * Used by FXML when controller is loaded.
     */
    @FXML
    public void initialize();

    /**
     * Add included controllers to parent.
     */
    public void configure(DexMainController parent);

}
