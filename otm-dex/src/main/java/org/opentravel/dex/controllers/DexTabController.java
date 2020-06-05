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

    // /**
    // * @deprecated - pass in a view group id
    // */
    // @Deprecated
    // public void configure(DexMainController mainController);

    /**
     * Add included controllers to main controller. Sub-types add the controllers in the tab to the included controllers
     * list, then call super.config() which will
     * {@link DexMainController#addIncludedController(DexIncludedController, int)} add the controllers to the main
     * controller
     * 
     * @param mc
     * @param viewGroupId is an identifier assigned by main controller used for controls such as lock.
     */
    void configure(DexMainController mc, int viewGroupId);

    /**
     * Does this tab have a stand alone dialog defined? If so, return its title. If not, return null.
     */
    public String getDialogTitle();

    /**
     * 
     * @return the group id value assigned to this controller by the main controller.
     */
    public int getViewGroupId();

    /**
     * Used by FXML when controller is loaded.
     */
    @FXML
    public void initialize();

    /**
     * Launch the stand-alone window that matches this tab's contents. If not supported, no-op.
     * 
     * @param e action event whose source is the menu item to be disabled and enabled on window close
     */
    public void launchWindow(ActionEvent e);


}
